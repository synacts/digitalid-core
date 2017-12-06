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
package net.digitalid.core.resolution.predecessor;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.NonHostIdentity;

/**
 * This interface provides read-only access to {@link FreezablePredecessors predecessors} and should <em>never</em> be cast away.
 * 
 * @see FreezablePredecessors
 */
// TODO: @GenerateConverter
@ReadOnly(FreezablePredecessors.class)
public interface ReadOnlyPredecessors extends ReadOnlyList<Predecessor> {

    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezablePredecessors clone();
    
    /**
     * Returns the identities of the predecessors that are mapped.
     */
    @Pure
    @NonCommitting
    public @Nonnull @Frozen @NonNullableElements ReadOnlyList<NonHostIdentity> getIdentities() throws ExternalException;
    
    // TODO
    
    /**
     * Sets these values as the predecessors of the given identifier.
     * Only commit the transaction if the predecessors have been verified.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param reply the reply stating that the given identifier has these predecessors.
     */
//    @Pure
//    @NonCommitting
//    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply<?> reply) throws DatabaseException;
    
}
