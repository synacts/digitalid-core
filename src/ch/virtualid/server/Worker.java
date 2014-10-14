package ch.virtualid.server;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import static ch.virtualid.io.Level.INFORMATION;
import static ch.virtualid.io.Level.WARNING;
import ch.virtualid.io.Logger;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.Request;
import ch.virtualid.packet.Response;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The worker is responsible for handling incoming requests asynchronously.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
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
            final @Nonnull Time start = new Time();
            @Nullable Identifier subject = null;
            @Nullable PacketError error = null;
            
            @Nullable Request request = null;
            @Nonnull Response response;
            try {
                try {
                    request = new Request(socket.getInputStream());
                    final @Nonnull SemanticType service = request.getService();
                    
                    final int size = request.getSize();
                    final @Nonnull FreezableList<Reply> replies = new FreezableArrayList<Reply>(size);
                    final @Nonnull FreezableList<PacketException> exceptions = new FreezableArrayList<PacketException>(size);
                    
                    Database.getConnection().commit();
                    for (int i = 0; i < size; i++) {
                        try {
                            final @Nonnull Method method = request.getMethod(i);
                            replies.set(i, method.executeOnHost());
                            if (method instanceof Action) {
                                // TODO: Audit the executed method if it is an action.
                            }
                            Database.getConnection().commit();
                        } catch (@Nonnull SQLException exception) {
                            exceptions.set(i, new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception));
                            Database.getConnection().rollback();
                        } catch (@Nonnull PacketException exception) {
                            exceptions.set(i, exception);
                            Database.getConnection().rollback();
                        }
                    }
                    
                    final @Nullable Audit audit;
                    if (request.getAudit() != null) {
                        audit = new Audit(start); // TODO: Retrieve the audit of the given service.
                    } else {
                        audit = null;
                    }
                    
                    response = new Response(request, replies.freeze(), exceptions.freeze(), audit);
                } catch (@Nonnull SQLException exception) {
                    Database.getConnection().rollback();
                    throw new PacketException(PacketError.INTERNAL, "An SQLException occurred.", exception);
                } catch (@Nonnull IOException exception) {
                    throw new PacketException(PacketError.EXTERNAL, "An IOException occurred.", exception);
                } catch (@Nonnull ExternalException exception) {
                    throw new PacketException(PacketError.EXTERNAL, "An ExternalException occurred.", exception);
                }
            } catch (@Nonnull PacketException exception) {
                response = new Response(request, exception);
                error = exception.getError();
            }
            
            // The database transaction is intentionally committed before returning the response so that slow or malicious clients cannot block the database.
            response.write(socket.getOutputStream());
            
            final @Nonnull Time end = new Time();
            logger.log(INFORMATION, "Request from '" + socket.getInetAddress() + "' handled in " + end.subtract(start).getValue() + " ms" + (subject != null ? " about " + subject : "") + (error != null ? " with error " + error : "") + ".");
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            logger.log(WARNING, exception);
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (@Nonnull IOException exception) {
                logger.log(WARNING, exception);
            }
        }
        
    }
    
}
