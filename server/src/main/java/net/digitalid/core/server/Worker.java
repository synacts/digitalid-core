package net.digitalid.core.server;

import java.io.IOException;
import java.net.Socket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.audit.RequestAudit;
import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionBuilder;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.EncryptionBuilder;
import net.digitalid.core.encryption.RequestEncryption;
import net.digitalid.core.encryption.ResponseEncryptionBuilder;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.RequestConverter;
import net.digitalid.core.packet.Response;
import net.digitalid.core.packet.ResponseBuilder;
import net.digitalid.core.service.Service;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.host.HostSignatureBuilder;

/**
 * A worker processes incoming requests asynchronously.
 * 
 * @see Listener
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Worker implements Runnable {
    
    /**
     * Returns the socket which this worker is connected to.
     */
    @Pure
    protected abstract @Nonnull Socket getSocket();
    
    /**
     * Asynchronous method to handle the incoming request.
     */
    @Override
    @Committing
    @PureWithSideEffects
//    @SuppressWarnings("ThrowableResultIgnored")
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
                final @Nonnull Pack pack = Pack.loadFrom(getSocket());
                request = pack.unpack(RequestConverter.INSTANCE, null);
                final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = request.getEncryption();
                final @Nonnull Signature<Compression<Pack>> signature = encryption.getObject();
                final @Nonnull Method<?> method = MethodIndex.get(signature);
                final @Nullable Reply<?> reply = method.executeOnHost();
                if (reply != null) {
                    final @Nonnull Compression<Pack> compressedResponse = CompressionBuilder.withObject(reply.pack()).build();
                    final @Nonnull Signature<Compression<Pack>> signedResponse = HostSignatureBuilder.withObject(compressedResponse).withSubject(signature.getSubject()).withSigner(encryption.getRecipient()).build();
                    final @Nonnull Encryption<Signature<Compression<Pack>>> encryptedResponse;
                    if (encryption instanceof RequestEncryption) {
                        encryptedResponse = ResponseEncryptionBuilder.withObject(signedResponse).withSymmetricKey(((RequestEncryption) encryption).getSymmetricKey()).build();
                    } else {
                        encryptedResponse = EncryptionBuilder.withObject(signedResponse).build();
                    }
                    response = ResponseBuilder.withEncryption(encryptedResponse).build();
                    Database.instance.get().commit();
                    response.pack().storeTo(getSocket());
                } else {
                    throw new UnsupportedOperationException("We should also send a response in case the reply is null.");
                }
            } catch (@Nonnull ExternalException exception) {
                Database.instance.get().rollback();
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
                if (!getSocket().isClosed()) { getSocket().close(); }
            } catch (@Nonnull IOException exception) {
                Log.warning("Could not close the socket.", exception);
            }
        }
        
    }
    
}
