package net.digitalid.core.server;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadonlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.database.Database;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.InternalMethod;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.synchronizer.ActionModule;
import net.digitalid.core.synchronizer.RequestAudit;
import net.digitalid.core.synchronizer.ResponseAudit;

/**
 * A worker processes incoming requests asynchronously.
 * 
 * @see Listener
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Worker implements Runnable {
    
    /**
     * Stores the socket which this worker is connected to.
     */
    private final @Nonnull Socket socket;
    
    /**
     * Creates a new worker with the given socket.
     * 
     * @param socket the socket which this worker is connected to.
     */
    public Worker(@Nonnull Socket socket) {
        this.socket = socket;
    }
    
    /**
     * Asynchronous method to handle the incoming request.
     */
    @Override
    @Committing
    public void run() {
        try {
            final @Nonnull Time start = new Time();
            @Nullable Identifier subject = null;
            @Nullable PacketError error = null;
            
            @Nullable Request request = null;
            @Nonnull Response response;
            try {
                Database.lock();
                try {
                    request = new Request(socket.getInputStream());
                    final @Nonnull Method reference = request.getMethod(0);
                    final @Nonnull Service service = reference.getService();
                    final @Nullable RequestAudit requestAudit = request.getAudit();
                    final @Nullable Agent agent = requestAudit != null && service.equals(CoreService.SERVICE) ? reference.getSignatureNotNull().getAgentCheckedAndRestricted(reference.getNonHostAccount(), null) : null;
                    
                    final int size = request.getSize();
                    final @Nonnull FreezableList<Reply> replies = new FreezableArrayList<>(size);
                    final @Nonnull FreezableList<PacketException> exceptions = new FreezableArrayList<>(size);
                    
                    for (int i = 0; i < size; i++) {
                        replies.add(null);
                        exceptions.add(null);
                        try {
                            final @Nonnull Method method = request.getMethod(i);
//                            System.out.println("- " + method.getClass().getSimpleName()); System.out.flush(); // TODO: Remove eventually.
                            replies.set(i, method.executeOnHost());
                            if (method instanceof Action) ActionModule.audit((Action) method);
                            Database.commit();
                        } catch (@Nonnull SQLException exception) {
                            exception.printStackTrace(); // TODO: Remove eventually.
                            exceptions.set(i, new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception));
                            Database.rollback();
                        } catch (@Nonnull PacketException exception) {
                            if (exception.getError() != PacketError.IDENTIFIER) exception.printStackTrace(); // TODO: Remove eventually.
                            exceptions.set(i, exception);
                            Database.rollback();
                        }
                    }
                    
                    final @Nullable ResponseAudit responseAudit;
                    if (requestAudit != null) {
                        if (!(reference instanceof InternalMethod)) throw new PacketException(PacketError.AUTHORIZATION, "An audit may only be requested by internal methods.");
                        final @Nullable ReadonlyAgentPermissions permissions;
                        @Nullable Restrictions restrictions;
                        if (service.equals(CoreService.SERVICE)) {
                            assert agent != null : "See above.";
                            permissions = agent.getPermissions();
                            try {
                                restrictions = agent.getRestrictions();
                            } catch (@Nonnull SQLException exception) {
                                restrictions = Restrictions.MIN;
                            }
                        } else {
                            final @Nonnull Credential credential = reference.getSignatureNotNull().toCredentialsSignatureWrapper().getCredentials().getNotNull(0);
                            permissions = credential.getPermissions();
                            restrictions = credential.getRestrictions();
                            if (permissions == null || restrictions == null) throw new PacketException(PacketError.AUTHORIZATION, "If an audit is requested, neither the permissions nor the restrictions may be null.");
                        }
                        responseAudit = ActionModule.getAudit(reference.getNonHostAccount(), service, requestAudit.getLastTime(), permissions, restrictions, agent);
                        Database.commit();
                    } else {
                        responseAudit = null;
                    }
                    
                    response = new Response(request, replies.freeze(), exceptions.freeze(), responseAudit);
                } catch (@Nonnull SQLException exception) {
                    exception.printStackTrace(); // TODO: Remove eventually.
                    throw new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception);
                } catch (@Nonnull IOException exception) {
                    exception.printStackTrace(); // TODO: Remove eventually.
                    throw new PacketException(PacketError.EXTERNAL, "An IOException occurred.", exception);
                } catch (@Nonnull ExternalException exception) {
                    exception.printStackTrace(); // TODO: Remove eventually.
                    throw new PacketException(PacketError.EXTERNAL, "An ExternalException occurred.", exception);
                }
            } catch (@Nonnull PacketException exception) {
                exception.printStackTrace(); // TODO: Remove eventually.
                response = new Response(request, exception.isRemote() ? new PacketException(PacketError.EXTERNAL, "An external error occurred.", exception) : exception);
                error = exception.getError();
                Database.rollback();
            } finally {
                Database.unlock();
            }
            
            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
            response.write(socket.getOutputStream());
            
            final @Nonnull Time end = new Time();
            Logger.log(Level.INFORMATION, "Worker", "Request from '" + socket.getInetAddress() + "' handled in " + end.subtract(start).getValue() + " ms" + (subject != null ? " about " + subject : "") + (error != null ? " with error " + error : "") + ".");
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            Logger.log(Level.WARNING, "Worker", "Could not send a response.", exception);
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (@Nonnull IOException exception) {
                Logger.log(Level.WARNING, "Worker", "Could not close the socket.", exception);
            }
        }
        
    }
    
}
