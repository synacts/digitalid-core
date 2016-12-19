package net.digitalid.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.threading.NamedThreadFactory;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.packet.Request;

/**
 * A listener accepts incoming {@link Request requests} and lets them handle by {@link Worker workers}.
 */
@Immutable
public class Listener extends Thread {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Stores the server socket to accept incoming requests.
     */
    private final @Nonnull ServerSocket serverSocket;
    
    /**
     * The thread pool executor runs the {@link Worker workers} that handle the incoming {@link Request requests}.
     */
    private final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(32), NamedThreadFactory.with("Worker"), new ThreadPoolExecutor.AbortPolicy());
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new listener that accepts incoming requests on the given port.
     */
    public Listener(int port) {
        super("Listener");
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (@Nonnull IOException exception) {
            throw new RuntimeException("The server could not bind to Digital ID's port (" + port + ").", exception); // TODO: InitializationException or something similar.
        }
    }
    
    /**
     * Accepts incoming requests and lets them handle by {@link Worker workers}.
     */
    @Override
    @PureWithSideEffects
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
    @PureWithSideEffects
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
