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
package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.subject.Subject;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.subject.annotations.GenerateCoreSubjectModule;
import net.digitalid.core.unit.CoreUnit;

/**
 * This class models a core subject in the {@link Database database}.
 * A core subject always belongs to an {@link Entity entity}.
 * 
 * @param <ENTITY> either {@link Entity} for a general core subject or {@link NonHostEntity} for a core subject that exists only for non-hosts.
 *            (The type has to be a supertype of {@link NonHostEntity}, which cannot be declared in Java, unfortunately!)
 * @param <KEY> the type of the key which identifies an instance among all instances of a core subject at the same entity.
 */
@Immutable
public abstract class CoreSubject<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY> extends Subject<CoreUnit> {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this core subject belongs.
     */
    @Pure
    @Provided
    public abstract @Nonnull ENTITY getEntity();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the key which identifies this core subject.
     */
    @Pure
    public abstract @Nonnull KEY getKey();
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CoreUnit getUnit() {
        return getEntity().getUnit();
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Generates and returns the {@link CoreSubjectModule} required to store synchronized properties.
     */
    @Pure
    @Override
    @GenerateCoreSubjectModule
    public abstract @Nonnull CoreSubjectModule<ENTITY, KEY, ?> module();
    
}
