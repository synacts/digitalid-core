package net.digitalid.core.property.value;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.value.PersistentValuePropertyEntry;
import net.digitalid.database.property.value.PersistentValuePropertyEntryConverter;
import net.digitalid.database.property.value.PersistentValuePropertyTable;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedPropertyTable;

/**
 * The synchronized value property table stores the {@link PersistentValuePropertyEntry value property entries}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface SynchronizedValuePropertyTable<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, VALUE, PROVIDED_FOR_VALUE> extends PersistentValuePropertyTable<CoreSite<?>, CONCEPT, VALUE, PROVIDED_FOR_VALUE>, SynchronizedPropertyTable<ENTITY, KEY, CONCEPT, PersistentValuePropertyEntry<CONCEPT, VALUE>, ValuePropertyRequiredAuthorization<ENTITY, KEY, CONCEPT, VALUE>> {
    
    /* -------------------------------------------------- Entry Converter -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Is it really necessary to override this method manually?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    @Derive("net.digitalid.database.property.value.PersistentValuePropertyEntryConverterBuilder.withPropertyTable(this).build()")
    public @Nonnull PersistentValuePropertyEntryConverter<CoreSite<?>, CONCEPT, VALUE, PROVIDED_FOR_VALUE> getEntryConverter();
    
}
