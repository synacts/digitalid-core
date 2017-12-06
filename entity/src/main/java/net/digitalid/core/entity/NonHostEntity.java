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
package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.unit.CoreUnit;

/**
 * This interface models a non-host entity.
 */
@Immutable
public interface NonHostEntity extends Entity {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull InternalNonHostIdentity getIdentity();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull NonHostEntity with(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        final @Nonnull Entity entity = Entity.with(unit, key);
        if (entity instanceof NonHostEntity) { return (NonHostEntity) entity; }
        else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " denotes a host identity.").build(); }
    }
    
}
