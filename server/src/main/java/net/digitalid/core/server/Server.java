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

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.console.Console;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Level;
import net.digitalid.utility.validation.annotations.file.existence.Existent;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.attribute.AttributeModuleInitializer;
import net.digitalid.core.cache.CacheModule;
import net.digitalid.core.clientagent.ClientAgentModuleInitializer;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.host.key.PrivateKeyChainLoader;
import net.digitalid.core.host.key.PublicKeyChainLoader;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.node.contact.ContactModuleInitializer;
import net.digitalid.core.node.context.ContextModuleInitializer;
import net.digitalid.core.packet.Request;

/**
 * The server runs the configured hosts.
 */
@Utility
public abstract class Server {
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE);
    
    /* -------------------------------------------------- Hosts -------------------------------------------------- */
    
    /**
     * Loads all hosts with cryptographic keys but without an exported tables file in the hosts directory.
     */
    @Impure
    @Committing
    @Initialize(target = Server.class, dependencies = {PrivateKeyChainLoader.class, PublicKeyChainLoader.class, CacheModule.class, AttributeModuleInitializer.class, ContextModuleInitializer.class, ContactModuleInitializer.class, ClientAgentModuleInitializer.class})
    public static void loadHosts() throws ConversionException {
        final @Nonnull FiniteIterable<@Nonnull @Existent File> configurationDirectoryFiles = Files.listNonHiddenFiles(Files.relativeToConfigurationDirectory("")).filter(File::isFile);
        final @Nonnull FiniteIterable<@Nonnull String> privateKeyFiles = configurationDirectoryFiles.map(File::getName).filter(name -> name.endsWith(".private.xdf"));
        privateKeyFiles.map(name -> name.substring(0, name.length() - 12)).filterNot(name -> Files.relativeToConfigurationDirectory(name + ".tables.xdf").exists()).map(HostIdentifier::with).doForEach(identifier -> HostBuilder.withIdentifier(identifier).build());
    }
    
    /* -------------------------------------------------- Services -------------------------------------------------- */
    
    /**
     * Loads all services with their code in the services directory.
     */
    @Impure
    @Committing
    public static void loadServices() {
        // TODO:
        
//        final @Nonnull File[] files = Directory.getServicesDirectory().listFiles();
//        for (final @Nonnull File file : files) {
//            if (file.isFile() && file.getName().endsWith(".jar")) {
//                try {
//                    Loader.loadJarFile(new JarFile(file));
//                } catch (@Nonnull IOException | ClassNotFoundException | SQLException exception) {
//                    throw InitializationError.get("Could not load the service in the file '" + file.getName() + "'.", exception);
//                }
//            }
//        }
    }
    
    /* -------------------------------------------------- Listener -------------------------------------------------- */
    
    // TODO: Move this section to the listener class?
    
    /**
     * References the thread that listens on the socket.
     */
    private static @Nullable Listener listener;
    
    /**
     * Starts the server with the configured hosts.
     */
    @Impure
    @Committing
    public static void start() throws IOException {
        listener = ListenerBuilder.build();
        listener.start();
        
//        try {
//            Cache.getPublicKeyChain(HostIdentity.DIGITALID);
//            Database.commit();
//        } catch (@Nonnull DatabaseException exception) {
//            throw InitializationError.get("Could not retrieve the public key chain of 'digitalid.net'.", exception);
//        }
    }
    
    /**
     * Stops the background threads of the server without shutting down (which is important for testing purposes).
     */
    @Impure
    public static void stop() {
        if (listener != null) {
            listener.shutDown();
        }
//        Client.stop();
    }
    
    /**
     * Shuts down the server after having handled all pending requests.
     */
    @Impure
    public static void shutDown() {
        Server.stop();
        System.exit(0);
    }
    
    /* -------------------------------------------------- Main Method -------------------------------------------------- */
    
    /**
     * The main method starts the server with the configured hosts and shows the console.
     * 
     * @param arguments the command line arguments indicating the hosts to be created when starting up.
     */
    @Impure
    @Committing
    public static void main(@Nonnull String[] arguments) {
        try {
            Configuration.initializeAllConfigurations();
            Database.commit();
            
            Console.writeLine();
            Console.writeLine("The library has been initialized successfully.");
            
            loadServices();
            Server.start();
            Console.writeLine("The server has been started and is now listening on port $.", Request.PORT.get());
            
            for (final @Nonnull String argument : arguments) {
                Console.writeLine();
                if (HostIdentifier.isValid(argument)) {
                    Console.writeLine("Creating a host with the identifier $, which can take several minutes.", argument);
                    try {
                        HostBuilder.withIdentifier(HostIdentifier.with(argument)).build();
                    } catch (@Nonnull ExternalException exception) {
                        Console.log(Level.FATAL, "Failed to create a new host with the identifier $.", exception, argument);
                        shutDown();
                    }
                } else {
                    Console.log(Level.FATAL, "$ is not a valid host identifier!", argument);
                    shutDown();
                }
            }
            
            Options.start();
        } catch (@Nonnull Throwable throwable) {
            Console.log(Level.FATAL, "The server crashed due to the following problem.", throwable);
            shutDown();
        }
    }
    
}
