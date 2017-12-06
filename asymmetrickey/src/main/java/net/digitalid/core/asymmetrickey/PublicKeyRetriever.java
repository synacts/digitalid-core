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
package net.digitalid.core.asymmetrickey;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;

/**
 * The public key retriever retrieves the public key of a host at a given time.
 */
@Stateless
@Functional
public interface PublicKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public @Nonnull PublicKey getPublicKey(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the public key retriever, which has to be provided by the cache package.
     */
    public static final @Nonnull Configuration<PublicKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException {
        return configuration.get().getPublicKey(host, time);
    }
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull HostIdentifier host, @Nonnull Time time) throws ExternalException {
        return retrieve(host.resolve(), time);
    }
    
}
