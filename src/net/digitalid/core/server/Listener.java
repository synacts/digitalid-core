package net.digitalid.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.packet.Request;
import net.digitalid.core.thread.NamedThreadFactory;

/**
 * A listener accepts incoming {@link Request requests} and lets them handle by {@link Worker workers}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Listener extends Thread {
    
    /**
     * Stores the server socket to accept incoming requests.
     */
    private final @Nonnull ServerSocket serverSocket;
    
    /**
     * The thread pool executor runs the {@link Worker workers} that handle the incoming {@link Request requests}.
     */
    private final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(32), new NamedThreadFactory("Worker"), new ThreadPoolExecutor.AbortPolicy());
    
    /**
     * Creates a new listener that accepts incoming requests on the given port.
     * 
     * @param port the port number on which incoming requests are accepted.
     */
    Listener(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (@Nonnull IOException exception) {
            throw new InitializationError("The server could not bind to Digital ID's port (" + Server.PORT + ").", exception);
        }
    }
    
    /**
     * Accepts incoming requests and lets them handle by {@link Worker workers}.
     */
    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                final @Nonnull Socket socket = serverSocket.accept();
                socket.setSoTimeout(1000000); // TODO: Remove two zeroes!
                try {
                    threadPoolExecutor.execute(new Worker(socket));
                    Logger.log(Level.VERBOSE, "Listener", "Connection accepted from '" + socket.getInetAddress().toString().substring(1) + "'.");
                } catch (@Nonnull RejectedExecutionException exception) {
                    Logger.log(Level.WARNING, "Listener", "Could not add a new worker.", exception);
                    socket.close();
                }
            } catch (@Nonnull IOException exception) {
                if (!serverSocket.isClosed()) Logger.log(Level.WARNING, "Listener", "Could not accept or close a socket.", exception);
            }
        }
    }
    
    /**
     * Shuts down the listener after having handled all pending requests.
     */
    void shutDown() {
        try {
            serverSocket.close();
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (@Nonnull IOException | InterruptedException exception) {
            Logger.log(Level.WARNING, "Listener", "Could not shut down the listener.", exception);
        }
    }
    
}
