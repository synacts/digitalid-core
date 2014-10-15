package ch.virtualid.server;

import ch.virtualid.errors.InitializationError;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.packet.Request;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * A listener accepts incoming {@link Request requests} and lets them handle by {@link Worker workers}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Listener extends Thread {
    
    /**
     * Stores the logger of the listener.
     */
    private static final @Nonnull Logger logger = new Logger("Listener.log");
    
    /**
     * The server socket is bound to Virtual ID's {@link Server#PORT port}.
     */
    private final @Nonnull ServerSocket serverSocket;
    
    /**
     * The thread pool executor runs the {@link Worker workers} that handle the incoming {@link Request requests}.
     */
    private final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.AbortPolicy());
    
    /**
     * Creates a new listener that accepts incoming requests.
     */
    Listener() {
        try {
            serverSocket = new ServerSocket(Server.PORT);
        } catch (@Nonnull IOException exception) {
            throw new InitializationError("The server could not bind to Virtual ID's port (" + Server.PORT + ").", exception);
        }
    }

    /**
     * Accepts incoming requests and lets them handle by {@link Worker workers}.
     */
    @Override
    public void run() {
        while (true) {
            try {
                final @Nonnull Socket socket = serverSocket.accept();
                socket.setSoTimeout(10000);
                threadPoolExecutor.execute(new Worker(socket));
                logger.log(Level.INFORMATION, "Connection accepted from " + socket.getInetAddress());
            } catch (@Nonnull IOException exception) {
                logger.log(Level.WARNING, exception);
            }
        }
    }
    
    /**
     * Shuts down the listener after having handled all pending requests.
     */
    void shutDown() {
        try {
            serverSocket.close();
        } catch (@Nonnull IOException exception) {
            logger.log(Level.WARNING, exception);
        }
        threadPoolExecutor.shutdown();
    }
    
}
