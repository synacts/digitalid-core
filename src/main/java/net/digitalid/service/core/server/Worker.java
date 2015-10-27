package net.digitalid.service.core.server;

import net.digitalid.service.core.block.wrappers.ClientSignatureWrapper;
import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;

import net.digitalid.service.core.action.synchronizer.ActionModule;
import net.digitalid.service.core.action.synchronizer.RequestAudit;
import net.digitalid.service.core.action.synchronizer.ResponseAudit;
import net.digitalid.service.core.dataservice.Service;
import net.digitalid.service.core.cryptography.credential.Credential;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalMethod;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.packet.Request;
import net.digitalid.service.core.packet.Response;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.logger.Log;

/**
 * A worker processes incoming requests asynchronously.
 * 
 * @see Listener
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
            final @Nonnull Time start = Time.getCurrent();
            @Nullable InternalIdentifier subject = null;
            @Nullable InternalIdentifier signer = null;
            @Nullable PacketErrorCode error = null;
            @Nullable Service service = null;
            @Nullable RequestAudit requestAudit = null;
            @Nonnull StringBuilder methods = new StringBuilder("Request");
            
            @Nullable Request request = null;
            @Nonnull Response response;
            try {
                Database.lock();
                try {
                    request = new Request(socket.getInputStream());
                    Log.verbose("Request decoded in " + Time.getCurrent().subtract(start).getValue() + " ms.");
                    
                    final @Nonnull Method reference = request.getMethod(0);
                    final @Nonnull SignatureWrapper signature = reference.getSignatureNotNull();
                    
                    service = reference.getService();
                    subject = reference.getSubject();
                    if (signature instanceof HostSignatureWrapper) signer = ((HostSignatureWrapper) signature).getSigner();
                    else if (signature instanceof ClientSignatureWrapper) signer = subject;
                    else if (signature instanceof CredentialsSignatureWrapper) signer = ((CredentialsSignatureWrapper) signature).getCredentials().getNonNullable(0).getIssuer().getAddress();
                    
                    requestAudit = request.getAudit();
                    final @Nullable Agent agent = requestAudit != null && service.equals(CoreService.SERVICE) ? signature.getAgentCheckedAndRestricted(reference.getNonHostAccount(), null) : null;
                    
                    final int size = request.getSize();
                    final @Nonnull FreezableList<Reply> replies = new FreezableArrayList<>(size);
                    final @Nonnull FreezableList<PacketException> exceptions = new FreezableArrayList<>(size);
                    
                    for (int i = 0; i < size; i++) {
                        replies.add(null);
                        exceptions.add(null);
                        final @Nonnull Time methodStart = Time.getCurrent();
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
                            exceptions.set(i, new PacketException(PacketErrorCode.INTERNAL, "An SQLException occurred.", exception));
                            Database.rollback();
                        } catch (@Nonnull PacketException exception) {
                            exceptions.set(i, exception);
                            Database.rollback();
                        }
                        
                        final @Nonnull Time methodEnd = Time.getCurrent();
                        Log.debugging(method.getClass().getSimpleName() + " handled in " + methodEnd.subtract(methodStart).getValue() + " ms.");
                    }
                    
                    final @Nullable ResponseAudit responseAudit;
                    if (requestAudit != null) {
                        final @Nonnull Time auditStart = Time.getCurrent();
                        if (!(reference instanceof InternalMethod)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "An audit may only be requested by internal methods.");
                        final @Nullable ReadOnlyAgentPermissions permissions;
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
                            final @Nonnull Credential credential = signature.toCredentialsSignatureWrapper().getCredentials().getNonNullable(0);
                            permissions = credential.getPermissions();
                            restrictions = credential.getRestrictions();
                            if (permissions == null || restrictions == null) throw new PacketException(PacketErrorCode.AUTHORIZATION, "If an audit is requested, neither the permissions nor the restrictions may be null.");
                        }
                        responseAudit = ActionModule.getAudit(reference.getNonHostAccount(), service, requestAudit.getLastTime(), permissions, restrictions, agent);
                        Database.commit();
                        final @Nonnull Time auditEnd = Time.getCurrent();
                        Log.debugging("Audit retrieved in " + auditEnd.subtract(auditStart).getValue() + " ms.");
                    } else {
                        responseAudit = null;
                    }
                    
                    response = new Response(request, replies.freeze(), exceptions.freeze(), responseAudit);
                } catch (@Nonnull SQLException exception) {
                    throw new PacketException(PacketErrorCode.INTERNAL, "An SQLException occurred.", exception);
                } catch (@Nonnull IOException exception) {
                    throw new PacketException(PacketErrorCode.EXTERNAL, "An IOException occurred.", exception);
                } catch (@Nonnull ExternalException exception) {
                    throw new PacketException(PacketErrorCode.EXTERNAL, "An ExternalException occurred.", exception);
                }
            } catch (@Nonnull PacketException exception) {
                response = new Response(request, exception.isRemote() ? new PacketException(PacketErrorCode.EXTERNAL, "An external error occurred.", exception) : exception);
                error = exception.getError();
                Database.rollback();
            } finally {
                Database.unlock();
            }
            
            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
            response.write(socket.getOutputStream());
            
            Log.information(methods + (requestAudit != null ? " with audit" : "") + (service != null ? " of the " + service.getName() : "") + (subject != null ? " to " + subject : "") + (signer != null ? " by " + signer : "") + " handled in " + start.ago().getValue() + " ms" + (error != null ? " with the error " + error.getName() : "") + ".");
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            Log.warning("Could not send a response.", exception);
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (@Nonnull IOException exception) {
                Log.warning("Could not close the socket.", exception);
            }
        }
        
    }
    
}
