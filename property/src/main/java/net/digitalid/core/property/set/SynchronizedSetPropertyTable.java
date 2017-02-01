package net.digitalid.core.property.set;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
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

import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedPropertyTable;
import net.digitalid.core.subject.CoreSubject;

/**
 * The synchronized set property table stores the {@link PersistentSetPropertyEntry set property entries}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface SynchronizedSetPropertyTable<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable VALUE, @Specifiable PROVIDED_FOR_VALUE> extends PersistentSetPropertyTable<CoreUnit, SUBJECT, VALUE, PROVIDED_FOR_VALUE>, SynchronizedPropertyTable<ENTITY, KEY, SUBJECT, PersistentSetPropertyEntry<SUBJECT, VALUE>, VALUE> {
    
    /* -------------------------------------------------- Entry Converter -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Is it really necessary to override this method manually?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    @Derive("net.digitalid.database.property.set.PersistentSetPropertyEntryConverterBuilder.withPropertyTable(this).build()")
    public @Nonnull PersistentSetPropertyEntryConverter<CoreUnit, SUBJECT, VALUE, PROVIDED_FOR_VALUE> getEntryConverter();
    
}
