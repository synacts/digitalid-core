package net.digitalid.core.server;

import java.io.IOException;
import java.net.Socket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.audit.RequestAudit;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.Service;

/**
 * A worker processes incoming requests asynchronously.
 * 
 * @see Listener
 */
@Immutable
public class Worker implements Runnable {
    
    /**
     * Stores the socket which this worker is connected to.
     */
    private final @Nonnull Socket socket;
    
    /**
     * Creates a new worker with the given socket.
     * 
     * @param socket the socket which this worker is connected to.
     */
    @TODO(task = "Generate a subclass instead.", date = "2016-12-04", author = Author.KASPAR_ETTER)
    public Worker(@Nonnull Socket socket) {
        this.socket = socket;
    }
    
    /**
     * Asynchronous method to handle the incoming request.
     */
    @Override
    @Committing
    @PureWithSideEffects
    @SuppressWarnings("ThrowableResultIgnored")
    public void run() {
        try {
            final @Nonnull Time start = TimeBuilder.build();
            @Nullable InternalIdentifier subject = null;
            @Nullable InternalIdentifier signer = null;
            @Nullable RequestErrorCode error = null;
            @Nullable Service service = null;
            @Nullable RequestAudit requestAudit = null;
            @Nonnull StringBuilder methods = new StringBuilder("Request");
            
            @Nullable Request request = null;
            @Nonnull Response response;
            
            // TODO: Implement the real worker again.
            
            try {
                final @Nonnull Pack pack = Pack.loadFrom(socket.getInputStream());
                final @Nonnull Method<?> method = MethodIndex.get(pack, null);
                final @Nullable Reply<?> reply = method.executeOnHost();
                if (reply != null) { reply.pack().storeTo(socket.getOutputStream()); }
            } catch (@Nonnull ExternalException | IOException exception) {
                throw new RuntimeException(exception);
            }
            
//            try {
//                try {
//                    request = new Request(socket.getInputStream());
//                    Log.verbose("Request decoded in " + Time.getCurrent().subtract(start).getValue() + " ms.");
//                    
//                    final @Nonnull Method reference = request.getMethod(0);
//                    final @Nonnull SignatureWrapper signature = reference.getSignatureNotNull();
//                    
//                    service = reference.getService();
//                    subject = reference.getSubject();
//                    if (signature instanceof HostSignatureWrapper) { signer = ((HostSignatureWrapper) signature).getSigner(); }
//                    else if (signature instanceof ClientSignatureWrapper) { signer = subject; }
//                    else if (signature instanceof CredentialsSignatureWrapper) { signer = ((CredentialsSignatureWrapper) signature).getCredentials().getNonNullable(0).getIssuer().getAddress(); }
//                    
//                    requestAudit = request.getAudit();
//                    final @Nullable Agent agent = requestAudit != null && service.equals(CoreService.SERVICE) ? signature.getAgentCheckedAndRestricted(reference.getNonHostAccount(), null) : null;
//                    
//                    final int size = request.getSize();
//                    final @Nonnull FreezableList<Reply> replies = FreezableArrayList.getWithCapacity(size);
//                    final @Nonnull FreezableList<RequestException> exceptions = FreezableArrayList.getWithCapacity(size);
//                    
//                    for (int i = 0; i < size; i++) {
//                        replies.add(null);
//                        exceptions.add(null);
//                        final @Nonnull Time methodStart = Time.getCurrent();
//                        final @Nonnull Method method = request.getMethod(i);
//                        
//                        if (i == 0) {
//                            methods = new StringBuilder(method.getClass().getSimpleName());
//                        } else {
//                            if (i + 1 == size) { methods.append(" and "); }
//                            else { methods.append(", "); }
//                            methods.append(method.getClass().getSimpleName());
//                        }
//                        
//                        try {
//                            replies.set(i, method.executeOnHost());
//                            if (method instanceof Action) { ActionModule.audit((Action) method); }
//                            Database.commit();
//                        } catch (@Nonnull DatabaseException exception) {
//                            exceptions.set(i, RequestException.get(RequestErrorCode.DATABASE, "An SQLException occurred.", exception));
//                            Database.rollback();
//                        } catch (@Nonnull RequestException exception) {
//                            exceptions.set(i, exception);
//                            Database.rollback();
//                        }
//                        
//                        final @Nonnull Time methodEnd = Time.getCurrent();
//                        Log.debugging(method.getClass().getSimpleName() + " handled in " + methodEnd.subtract(methodStart).getValue() + " ms.");
//                    }
//                    
//                    final @Nullable ResponseAudit responseAudit;
//                    if (requestAudit != null) {
//                        final @Nonnull Time auditStart = Time.getCurrent();
//                        if (!(reference instanceof InternalMethod)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "An audit may only be requested by internal methods."); }
//                        final @Nullable ReadOnlyAgentPermissions permissions;
//                        @Nullable Restrictions restrictions;
//                        if (service.equals(CoreService.SERVICE)) {
//                            Require.that(agent != null).orThrow("See above.");
//                            permissions = agent.getPermissions();
//                            try {
//                                restrictions = agent.getRestrictions();
//                            } catch (@Nonnull SQLException exception) {
//                                restrictions = Restrictions.MIN;
//                            }
//                        } else {
//                            final @Nonnull Credential credential = signature.toCredentialsSignatureWrapper().getCredentials().getNonNullable(0);
//                            permissions = credential.getPermissions();
//                            restrictions = credential.getRestrictions();
//                            if (permissions == null || restrictions == null) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "If an audit is requested, neither the permissions nor the restrictions may be null."); }
//                        }
//                        responseAudit = ActionModule.getAudit(reference.getNonHostAccount(), service, requestAudit.getLastTime(), permissions, restrictions, agent);
//                        Database.commit();
//                        final @Nonnull Time auditEnd = Time.getCurrent();
//                        Log.debugging("Audit retrieved in " + auditEnd.subtract(auditStart).getValue() + " ms.");
//                    } else {
//                        responseAudit = null;
//                    }
//                    
//                    response = new Response(request, replies.freeze(), exceptions.freeze(), responseAudit);
//                } catch (@Nonnull DatabaseException exception) {
//                    throw RequestException.with(RequestErrorCode.DATABASE, "A database exception occurred.", exception);
//                } catch (@Nonnull NetworkException exception) {
//                    throw RequestException.with(RequestErrorCode.NETWORK, "A network exception occurred.", exception);
//                } catch (@Nonnull InternalException exception) {
//                    throw RequestException.with(RequestErrorCode.INTERNAL, "An internal exception occurred.", exception);
//                } catch (@Nonnull ExternalException exception) {
//                    throw RequestException.with(RequestErrorCode.EXTERNAL, "An external exception occurred.", exception);
//                }
//            } catch (@Nonnull RequestException exception) {
//                response = new Response(request, exception.isDecoded() ? RequestException.with(RequestErrorCode.REQUEST, "An external request error occurred.", exception) : exception);
//                error = exception.getCode();
//                Database.rollback();
//            }
//            
//            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
//            response.write(socket.getOutputStream());
            
            Log.information(methods + (requestAudit != null ? " with audit" : "") + (service != null ? " of the " + service.getTitle() : "") + (subject != null ? " to " + subject : "") + (signer != null ? " by " + signer : "") + " handled in " + start.ago().getValue() + " ms" + (error != null ? " with the error " + error.toString() : "") + ".");
        } catch (@Nonnull InternalException exception) {
            Log.warning("Could not send a response.", exception);
        } finally {
            try {
                if (!socket.isClosed()) { socket.close(); }
            } catch (@Nonnull IOException exception) {
                Log.warning("Could not close the socket.", exception);
            }
        }
        
    }
    
}
