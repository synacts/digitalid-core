package net.digitalid.core.property.map;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.map.PersistentMapPropertyEntry;
import net.digitalid.database.property.map.PersistentMapPropertyEntryConverter;
import net.digitalid.database.property.map.PersistentMapPropertyTable;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedPropertyTable;

/**
 * The synchronized map property table stores the {@link PersistentMapPropertyEntry map property entries}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface SynchronizedMapPropertyTable<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> extends PersistentMapPropertyTable<CoreSite<?>, CONCEPT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE>, SynchronizedPropertyTable<ENTITY, KEY, CONCEPT, PersistentMapPropertyEntry<CONCEPT, MAP_KEY, MAP_VALUE>, MAP_KEY> {
    
    /* -------------------------------------------------- Entry Converter -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Is it really necessary to override this method manually?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    @Derive("net.digitalid.database.property.map.PersistentMapPropertyEntryConverterBuilder.withPropertyTable(this).build()")
    public @Nonnull PersistentMapPropertyEntryConverter<CoreSite<?>, CONCEPT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> getEntryConverter();
    
}
