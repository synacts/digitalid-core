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
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.synchronizer.ActionModule;
import net.digitalid.core.synchronizer.RequestAudit;
import net.digitalid.core.synchronizer.ResponseAudit;
import net.digitalid.core.wrappers.ClientSignatureWrapper;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

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
            @Nullable InternalIdentifier subject = null;
            @Nullable InternalIdentifier signer = null;
            @Nullable PacketError error = null;
            @Nullable Service service = null;
            @Nullable RequestAudit requestAudit = null;
            @Nonnull StringBuilder methods = new StringBuilder("Request");
            
            @Nullable Request request = null;
            @Nonnull Response response;
            try {
                Database.lock();
                try {
                    request = new Request(socket.getInputStream());
                    Logger.log(Level.VERBOSE, "Worker", "Request decoded in " + new Time().subtract(start).getValue() + " ms.");
                    
                    final @Nonnull Method reference = request.getMethod(0);
                    final @Nonnull SignatureWrapper signature = reference.getSignatureNotNull();
                    
                    service = reference.getService();
                    subject = reference.getSubject();
                    if (signature instanceof HostSignatureWrapper) signer = ((HostSignatureWrapper) signature).getSigner();
                    else if (signature instanceof ClientSignatureWrapper) signer = subject;
                    else if (signature instanceof CredentialsSignatureWrapper) signer = ((CredentialsSignatureWrapper) signature).getCredentials().getNotNull(0).getIssuer().getAddress();
                    
                    requestAudit = request.getAudit();
                    final @Nullable Agent agent = requestAudit != null && service.equals(CoreService.SERVICE) ? signature.getAgentCheckedAndRestricted(reference.getNonHostAccount(), null) : null;
                    
                    final int size = request.getSize();
                    final @Nonnull FreezableList<Reply> replies = new FreezableArrayList<>(size);
                    final @Nonnull FreezableList<PacketException> exceptions = new FreezableArrayList<>(size);
                    
                    for (int i = 0; i < size; i++) {
                        replies.add(null);
                        exceptions.add(null);
                        final @Nonnull Time methodStart = new Time();
                        final @Nonnull Method method = request.getMethod(i);
                        
                        if (i == 0) {
                            methods = new StringBuilder(method.getClass().getSimpleName());
                        } else {
                            if (i + 1 == size) methods.append(" and ");
                            else methods.append(", ");
                            methods.append(method.getClass().getSimpleName());
                        }
                        
                        try {
                            replies.set(i, method.executeOnHost());
                            if (method instanceof Action) ActionModule.audit((Action) method);
                            Database.commit();
                        } catch (@Nonnull SQLException exception) {
                            exceptions.set(i, new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception));
                            Database.rollback();
                        } catch (@Nonnull PacketException exception) {
                            exceptions.set(i, exception);
                            Database.rollback();
                        }
                        
                        final @Nonnull Time methodEnd = new Time();
                        Logger.log(Level.DEBUGGING, "Worker", method.getClass().getSimpleName() + " handled in " + methodEnd.subtract(methodStart).getValue() + " ms.");
                    }
                    
                    final @Nullable ResponseAudit responseAudit;
                    if (requestAudit != null) {
                        final @Nonnull Time auditStart = new Time();
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
                            final @Nonnull Credential credential = signature.toCredentialsSignatureWrapper().getCredentials().getNotNull(0);
                            permissions = credential.getPermissions();
                            restrictions = credential.getRestrictions();
                            if (permissions == null || restrictions == null) throw new PacketException(PacketError.AUTHORIZATION, "If an audit is requested, neither the permissions nor the restrictions may be null.");
                        }
                        responseAudit = ActionModule.getAudit(reference.getNonHostAccount(), service, requestAudit.getLastTime(), permissions, restrictions, agent);
                        Database.commit();
                        final @Nonnull Time auditEnd = new Time();
                        Logger.log(Level.DEBUGGING, "Worker", "Audit retrieved in " + auditEnd.subtract(auditStart).getValue() + " ms.");
                    } else {
                        responseAudit = null;
                    }
                    
                    response = new Response(request, replies.freeze(), exceptions.freeze(), responseAudit);
                } catch (@Nonnull SQLException exception) {
                    throw new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception);
                } catch (@Nonnull IOException exception) {
                    throw new PacketException(PacketError.EXTERNAL, "An IOException occurred.", exception);
                } catch (@Nonnull ExternalException exception) {
                    throw new PacketException(PacketError.EXTERNAL, "An ExternalException occurred.", exception);
                }
            } catch (@Nonnull PacketException exception) {
                response = new Response(request, exception.isRemote() ? new PacketException(PacketError.EXTERNAL, "An external error occurred.", exception) : exception);
                error = exception.getError();
                Database.rollback();
            } finally {
                Database.unlock();
            }
            
            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
            response.write(socket.getOutputStream());
            
            Logger.log(Level.INFORMATION, "Worker", methods + (requestAudit != null ? " with audit" : "") + (service != null ? " of the " + service.getName() : "") + (subject != null ? " to " + subject : "") + (signer != null ? " by " + signer : "") + " handled in " + start.ago().getValue() + " ms" + (error != null ? " with the error " + error.getName() : "") + ".");
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
