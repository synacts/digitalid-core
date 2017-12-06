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
package net.digitalid.core.entity.factories;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.annotations.IsHost;

/**
 * The host factory returns the host with the given host identifier.
 */
@Stateless
@Functional
public interface HostFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the host with the given identifier.
     * 
     * @throws RequestException if there is no host with the given identifier on this server.
     */
    @Pure
    public @Nonnull @IsHost CoreUnit getHost(@Nonnull HostIdentifier identifier) throws RequestException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the account factory, which has to be provided by the host package.
     */
    public static final @Nonnull Configuration<HostFactory> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Returns the host with the given identifier.
     * 
     * @throws RequestException if there is no host with the given identifier on this server.
     */
    @Pure
    @NonCommitting
    public static @Nonnull @IsHost CoreUnit create(@Nonnull HostIdentifier identifier) throws RequestException {
        return configuration.get().getHost(identifier);
    }
    
}
