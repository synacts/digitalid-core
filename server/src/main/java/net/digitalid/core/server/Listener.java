/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.threading.NamedThreadFactory;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.packet.Request;

/**
 * A listener accepts incoming {@link Request requests} and lets them handle by {@link Worker workers}.
 */
@Immutable
@GenerateBuilder
public class Listener extends Thread {
    
    /* -------------------------------------------------- Socket -------------------------------------------------- */
    
    /**
     * Stores the server socket to accept incoming requests.
     */
    private final @Nonnull ServerSocket serverSocket;
    
    /* -------------------------------------------------- Executor -------------------------------------------------- */
    
    /**
     * The thread pool executor runs the {@link Worker workers} that handle the incoming {@link Request requests}.
     */
    private final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(32), NamedThreadFactory.with("Worker"), new ThreadPoolExecutor.AbortPolicy());
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new listener that accepts incoming requests.
     */
    Listener() throws IOException {
        super("Listener");
        
        this.serverSocket = new ServerSocket(Request.PORT.get());
    }
    
    /* -------------------------------------------------- Running -------------------------------------------------- */
    
    /**
     * Accepts incoming requests and lets them handle by {@link Worker workers}.
     */
    @Override
    @PureWithSideEffects
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                final @Nonnull Socket socket = serverSocket.accept();
                socket.setSoTimeout(Request.TIMEOUT.get());
                try {
                    threadPoolExecutor.execute(WorkerBuilder.withSocket(socket).build());
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
    
    /* -------------------------------------------------- Shut Down -------------------------------------------------- */
    
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
