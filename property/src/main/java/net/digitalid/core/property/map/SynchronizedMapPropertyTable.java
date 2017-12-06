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
package net.digitalid.core.property.map;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.map.PersistentMapPropertyEntry;
import net.digitalid.database.property.map.PersistentMapPropertyTable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedPropertyTable;
import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.unit.CoreUnit;

/**
 * The synchronized map property table stores the {@link PersistentMapPropertyEntry map property entries}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class SynchronizedMapPropertyTable<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable MAP_KEY, @Unspecifiable MAP_VALUE, @Specifiable PROVIDED_FOR_KEY, @Specifiable PROVIDED_FOR_VALUE> extends PersistentMapPropertyTable<CoreUnit, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> implements SynchronizedPropertyTable<ENTITY, KEY, SUBJECT, PersistentMapPropertyEntry<SUBJECT, MAP_KEY, MAP_VALUE>, MAP_KEY> {
    
    /* -------------------------------------------------- Action Converter -------------------------------------------------- */
    
    /**
     * Returns the action converter used to convert and recover the {@link MapPropertyInternalAction}.
     */
    @Pure
    @Derive("new MapPropertyInternalActionConverterSubclass<>(this)")
    abstract @Nonnull MapPropertyInternalActionConverter<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> getActionConverter();
    
}
