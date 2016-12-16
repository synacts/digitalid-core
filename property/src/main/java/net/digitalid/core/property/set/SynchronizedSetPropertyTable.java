package net.digitalid.core.property.set;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.set.PersistentSetPropertyEntry;
import net.digitalid.database.property.set.PersistentSetPropertyEntryConverter;
import net.digitalid.database.property.set.PersistentSetPropertyTable;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedPropertyTable;

/**
 * The synchronized set property table stores the {@link PersistentSetPropertyEntry set property entries}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface SynchronizedSetPropertyTable<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, VALUE, PROVIDED_FOR_VALUE> extends PersistentSetPropertyTable<CoreSite<?>, CONCEPT, VALUE, PROVIDED_FOR_VALUE>, SynchronizedPropertyTable<ENTITY, KEY, CONCEPT, PersistentSetPropertyEntry<CONCEPT, VALUE>, SetPropertyRequiredAuthorization<ENTITY, KEY, CONCEPT, VALUE>> {
    
    /* -------------------------------------------------- Entry Converter -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Is it really necessary to override this method manually?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    @Derive("net.digitalid.database.property.set.PersistentSetPropertyEntryConverterBuilder.<CoreSite<?>, CONCEPT, VALUE, PROVIDED_FOR_VALUE>withName(getFullNameWithUnderlines()).withPropertyTable(this).build()")
    public @Nonnull PersistentSetPropertyEntryConverter<CoreSite<?>, CONCEPT, VALUE, PROVIDED_FOR_VALUE> getEntryConverter();
    
}
