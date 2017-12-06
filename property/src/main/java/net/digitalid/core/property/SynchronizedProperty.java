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
package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.property.Observer;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.PersistentPropertyEntry;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.map.WritableSynchronizedMapProperty;
import net.digitalid.core.property.set.WritableSynchronizedSetProperty;
import net.digitalid.core.property.value.WritableSynchronizedValueProperty;
import net.digitalid.core.subject.CoreSubject;

/**
 * A synchronized property belongs to a {@link CoreSubject core subject} and synchronizes across {@link Unit units}.
 * 
 * @see WritableSynchronizedMapProperty
 * @see WritableSynchronizedSetProperty
 * @see WritableSynchronizedValueProperty
 */
@Mutable
@ThreadSafe
public interface SynchronizedProperty<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable ENTRY extends PersistentPropertyEntry<SUBJECT>, @Unspecifiable OBSERVER extends Observer> extends PersistentProperty<SUBJECT, OBSERVER> {
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    /**
     * Returns the property table that contains the property name, subject module and entry converter.
     */
    @Pure
    public @Nonnull SynchronizedPropertyTable<ENTITY, KEY, SUBJECT, ENTRY, ?> getTable();
    
}
