package ch.virtualid.server;

import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import static ch.virtualid.io.Level.ERROR;
import static ch.virtualid.io.Level.INFORMATION;
import static ch.virtualid.io.Level.WARNING;
import ch.virtualid.io.Logger;
import ch.virtualid.packet.Packet;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The worker class is responsible for handling incoming requests asynchronously.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.6
 */
public final class Worker implements Runnable {
    
    /**
     * Stores the logger of the worker.
     */
    private static final @Nonnull Logger logger = new Logger("Worker.log");
    
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
    public void run() {
        try {
            long start = System.currentTimeMillis();
            String type = "";
            String identifier = "";
            String error = "";
            
            @Nullable SymmetricKey symmetricKey = null;
            
            Packet response;
            try {
                @Nonnull Packet request;
                try { request = new Packet(SelfcontainedWrapper.read(socket.getInputStream())); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.PACKET, exception); }
                symmetricKey = request.getEncryption().getSymmetricKey();
                
                Host host = Server.getHost(request.getEncryption().getRecipient());
                SignatureWrapper signature = request.getSignatures();
                identifier = signature.getIdentifier();
                boolean mapped = Mapper.isMapped(identifier);
                long vid = mapped ? Mapper.getVid(identifier) : 0l;
                
                SelfcontainedWrapper content = request.getContents();
                if (!Mapper.exists(content.getIdentifier())) throw new PacketException(PacketError.REQUEST);
                type = " " + content.getIdentifier();
                long requestType = Mapper.getVid(content.getIdentifier());
                Handler handler = Handler.get(requestType);
                Block element = content.getElement();
                
                // TODO: Only in case of the core service!        
                @Nullable Identifier subject = signature.getSubject();
                assert subject != null : "See the class invariant.";
                if (!subject.getHostIdentifier().equals(encryption.getRecipient())) throw new PacketException(PacketError.SIGNATURE, new InvalidEncodingException("..."));
                
                // TODO: Time constraints and replay detection?
                
                // Internal requests have to be signed by a client.
                if (handler.isInternal() && signature.getClient() == null) throw new PacketException(PacketError.SIGNATURE);
                
                // If signed by a client, the identifier in the request islong to the given host.
                if (signature.getClient() != null && !NonHostIdentifier.getHost(identifier).equalsIgnoreCase(host.getIdentifier())) throw new PacketException(PacketError.IDENTIFIER);
                
                // If the identifier is not mapped, the request is to open a new account and the other way round.
                if (mapped == (requestType == Vid.ACCOUNT_OPEN_REQUEST)) throw new PacketException(PacketError.IDENTIFIER);
                
                // The identifier is the address of the VID, otherwise the request is treated as a category retrieval indicating the predecessors and the successor.
                if (mapped && !Mapper.isAddress(identifier)) requestType = Vid.CATEGORY_GET_REQUEST;
                
                // Requests to non-host VIDs have to be encrypted.
                if (!Identifier.isHost(identifier) && !request.getEncryption().isEncrypted()) throw new PacketException(PacketError.ENCRYPTION);
                
                // Hosts accept only attribute and category requests for themselves.
                if (Identifier.isHost(identifier) && requestType != Vid.ATTRIBUTE_GET_REQUEST && requestType != Vid.CATEGORY_GET_REQUEST) throw new PacketException(PacketError.REQUEST);
                
                // If there was an internal problem like a database inconsistency, try to execute the request a second time.
                for (int attempt = 0; true; attempt++) {
                    try (@Nonnull Connection connection = Database.getConnection()) {
                        content = new SelfcontainedWrapper(Mapper.getIdentifier(handler.getResponseType()), handler.handle(connection, host, vid, element, signature));
                        
                        // TODO: Auditing.
                        
                        connection.commit();
                        break;
                    } catch (InvalidEncodingException exception) {
                        throw new PacketException(PacketError.REQUEST, exception);
                    } catch (PacketException exception) {
                        throw exception;
                    } catch (SQLException exception) { // TODO: Throw a different packet error.
                        logger.log(ERROR, "An internal error occurred.", exception);
                        if (attempt > 0) throw new PacketException(PacketError.INTERNAL, exception);
                    }
                }
                
                // TODO: Depending on how the request was signed, append audit trail.
                
                response = new Packet(Arrays.asList(content), symmetricKey, identifier, null, host.getIdentifier());
            } catch (@Nonnull PacketException exception) {
                @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(NonHostIdentifier.PACKET_ERROR, new Int8Wrapper(exception.getError().getValue()));
                response = new Packet(content, symmetricKey);
                error = " with " + exception.getError();
            }
            
            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
            OutputStream outputStream = socket.getOutputStream();
            response.write(outputStream);
            
            long end = System.currentTimeMillis();
            identifier = (identifier.isEmpty() ? "" : " to " + identifier);
            logger.log(INFORMATION, "Request" + type + identifier + " from '" + socket.getInetAddress().toString() + "' handled in " + (end - start) + " ms" + error + ".");
            
        } catch (@Nonnull IOException | FailedEncodingException exception) {
            logger.log(WARNING, "The worker could not send a response.", exception);
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException exception) {
                logger.log(WARNING, "The worker could not close the socket.", exception);
            }
        }
        
    }
    
}
