package net.digitalid.service.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import net.digitalid.service.core.packet.Request;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.system.thread.NamedThreadFactory;

/**
 * A listener accepts incoming {@link Request requests} and lets them handle by {@link Worker workers}.
 */
public final class Listener extends Thread {
    
    /**
     * Stores the server socket to accept incoming requests.
     */
    private final @Nonnull ServerSocket serverSocket;
    
    /**
     * The thread pool executor runs the {@link Worker workers} that handle the incoming {@link Request requests}.
     */
    private final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(32), NamedThreadFactory.get("Worker"), new ThreadPoolExecutor.AbortPolicy());
    
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
                    Log.verbose("Connection accepted from '" + socket.getInetAddress().toString().substring(1) + "'.");
                } catch (@Nonnull RejectedExecutionException exception) {
                    Log.warning("Could not add a new worker.", exception);
                    socket.close();
                }
            } catch (@Nonnull IOException exception) {
                if (!serverSocket.isClosed()) { Log.warning("Could not accept or close a socket.", exception); }
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
            Log.warning("Could not shut down the listener.", exception);
        }
    }
    
}
