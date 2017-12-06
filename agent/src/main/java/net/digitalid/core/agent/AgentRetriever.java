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
package net.digitalid.core.agent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.entity.NonHostEntity;

/**
 * The agent factory returns the agent for the given entity with the given key.
 */
@Stateless
@Functional
public interface AgentRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the agent for the given entity with the given key.
     */
    @Pure
    public @Nonnull Agent getAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException, RecoveryException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the agent factory, which has to be provided by the client agent package.
     */
    public static final @Nonnull Configuration<AgentRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Agent retrieve(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException, RecoveryException {
        return configuration.get().getAgent(entity, commitment);
    }
    
}
