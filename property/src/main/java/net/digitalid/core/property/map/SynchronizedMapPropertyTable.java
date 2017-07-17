package net.digitalid.core.property.map;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
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
public abstract class SynchronizedMapPropertyTable<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable MAP_KEY, @Unspecifiable MAP_VALUE, @Specifiable PROVIDED_FOR_KEY, @Specifiable PROVIDED_FOR_VALUE> extends PersistentMapPropertyTable<CoreUnit, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> implements SynchronizedPropertyTable<ENTITY, KEY, SUBJECT, PersistentMapPropertyEntry<SUBJECT, MAP_KEY, MAP_VALUE>, MAP_KEY> {}
