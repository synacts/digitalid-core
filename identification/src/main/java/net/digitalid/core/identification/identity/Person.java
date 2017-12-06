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

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 */
@Mutable
public abstract class Person extends RelocatableIdentity {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Sets the internal number that represents this person.
     */
    @Impure
    abstract void setKey(long key);
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    // TODO: Remove the following code after settling on a merging strategy.
    
//    @Override
//    @NonCommitting
//    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
//        final @Nullable InternalNonHostIdentifier successor = Successor.get(getAddress());
//        if (successor != null && successor.isMapped()) {
//            final @Nonnull InternalNonHostIdentity person = successor.getMappedIdentity();
//            setAddress(person.getAddress());
//            setKey(person.getKey());
//            return true;
//        } else {
//            Mapper.unmap(this);
//            throw exception;
//        }
//    }
    
}
