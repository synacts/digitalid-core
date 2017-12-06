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
package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This interface models a digital identity, which can change identifiers and hosts.
 * Note that identity objects are not necessarily unique (e.g. after identities have been merged).
 * TODO: Explain the above properties better.
 * 
 * TODO: The equality and hash code of identities should only depend on their key (so that types are sound hash keys even after relocation).
 * 
 * @see NonHostIdentity
 * @see InternalIdentity
 */
@Mutable
@TODO(task = "Let it extend Subject to get the right foreign key constraints?", date = "2017-01-24", author = Author.KASPAR_ETTER)
public interface Identity extends RootInterface {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the internal number that represents this identity.
     * The key remains the same after relocation but changes after merging.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Returns the current address of this identity.
     */
    @Pure
    @NonRepresentative
    public @Nonnull Identifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Returns the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    // TODO: I'm still uncertain how we want to handle this in the future but tend to caching only in single-access mode and each time database lookups in multi-access mode.
    
//    /**
//     * Returns whether this identity has been merged and updates the internal number and the identifier.
//     * 
//     * @param exception the exception to be rethrown if this identity has not been merged.
//     */
//    @Pure
//    @NonCommitting
//    public boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException;
    
}
