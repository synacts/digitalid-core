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
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * The private key retriever retrieves the private key of a host at a given time.
 */
@Stateless
@Functional
public interface PrivateKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key of the given host at the given time.
     */
    @Pure
    public @Nonnull PrivateKey getPrivateKey(@Nonnull HostIdentifier host, @Nonnull Time time) throws RequestException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the private key retriever, which has to be provided by the host package.
     */
    public static final @Nonnull Configuration<PrivateKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the private key of the given host at the given time.
     */
    @Pure
    public static @Nonnull PrivateKey retrieve(@Nonnull HostIdentifier host, @Nonnull Time time) throws RequestException {
        return configuration.get().getPrivateKey(host, time);
    }
    
}
