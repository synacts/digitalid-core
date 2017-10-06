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
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.account.OpenAccount;
import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionBuilder;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.EncryptionBuilder;
import net.digitalid.core.encryption.RequestEncryption;
import net.digitalid.core.encryption.ResponseEncryptionBuilder;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.handler.reply.instances.EmptyReplyBuilder;
import net.digitalid.core.handler.reply.instances.RequestExceptionReplyBuilder;
import net.digitalid.core.host.Host;
import net.digitalid.core.host.account.Account;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.RequestConverter;
import net.digitalid.core.packet.Response;
import net.digitalid.core.packet.ResponseBuilder;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureBuilder;
import net.digitalid.core.signature.host.HostSignatureCreator;

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
    public void run() {
        try {
            Log.debugging("Received a request from $.", getSocket().getInetAddress());
            
            final @Nonnull Time start = TimeBuilder.build();
            
            @Nullable Encryption<Signature<Compression<Pack>>> encryptedMethod = null;
            @Nullable Signature<Compression<Pack>> signedMethod = null;
            
            @Nullable Method<?> method = null;
            @Nullable Reply<?> reply = null;
            
            try {
                try {
                    final @Nonnull Pack pack = Pack.loadFrom(getSocket());
                    final @Nonnull Request request = pack.unpack(RequestConverter.INSTANCE, null);
                    
                    encryptedMethod = request.getEncryption();
                    final @Nullable HostIdentifier recipient = encryptedMethod.getRecipient();
                    if (recipient == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.RECIPIENT).withMessage("The recipient may not be null.").build(); }
                    final @Nonnull Host host = Host.of(recipient);
                    
                    signedMethod = encryptedMethod.getObject();
                    final @Nonnull InternalIdentifier subject;
                    if (signedMethod.getObject().getObject().getType().equals(OpenAccount.TYPE)) { subject = recipient; } else { subject = signedMethod.getSubject(); }
                    final @Nonnull Account account = Account.with(host, subject.resolve());
                    
                    method = MethodIndex.get(signedMethod, account);
                    reply = method.executeOnHost();
                    
                    Database.commit();
                } catch (@Nonnull InternalException exception) {
                    throw RequestExceptionBuilder.withCode(RequestErrorCode.INTERNAL).withMessage("An internal problem occurred.").withCause(exception).build();
                } catch (@Nonnull ExternalException exception) {
                    throw RequestExceptionBuilder.withCode(RequestErrorCode.EXTERNAL).withMessage("An external problem occurred.").withCause(exception).build();
                }
            } catch (@Nonnull RequestException exception) {
                Database.rollback();
                Log.warning("A request error occurred:", exception);
                reply = RequestExceptionReplyBuilder.withRequestException(exception.isDecoded() ? RequestExceptionBuilder.withCode(RequestErrorCode.REQUEST).withMessage("Another server responded with a request error.").withCause(exception).build() : exception).build();
            }
            
            if (reply == null) { reply = EmptyReplyBuilder.build(); }
            final @Nonnull Compression<Pack> compressedReply = CompressionBuilder.withObject(reply.pack()).build();
            
            // The reply.pack() statement maps the semantic type of the reply converter, which results in a concurrent update if the client unpacks the response with the same database. The following commit prevents this. However, it is a suboptimal fix for this problem.
            try { Database.commit(); } catch (@Nonnull DatabaseException exception) { Database.rollback(); }
            
            final @Nonnull Signature<Compression<Pack>> signedReply;
            if (encryptedMethod != null && signedMethod != null) {
                signedReply = HostSignatureCreator.sign(compressedReply, CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).about(signedMethod.getSubject()).as(encryptedMethod.getRecipient());
            } else {
                signedReply = SignatureBuilder.withObjectConverter(CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).withObject(compressedReply).withSubject(HostIdentifier.DIGITALID).build();
            }
            
            final @Nonnull Encryption<Signature<Compression<Pack>>> encryptedReply;
            if (encryptedMethod instanceof RequestEncryption) {
                encryptedReply = ResponseEncryptionBuilder.withObject(signedReply).withSymmetricKey(((RequestEncryption) encryptedMethod).getSymmetricKey()).build();
            } else {
                encryptedReply = EncryptionBuilder.withObject(signedReply).build();
            }
            
            final @Nonnull Response response = ResponseBuilder.withEncryption(encryptedReply).build();
            response.pack().storeTo(getSocket());
            
            Log.information(method + " handled in " + start.ago().getValue() + " ms.");
        } catch (@Nonnull NetworkException exception) {
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
