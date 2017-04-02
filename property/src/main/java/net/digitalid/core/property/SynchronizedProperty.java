package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.property.Observer;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.PersistentPropertyEntry;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.value.WritableSynchronizedValueProperty;
import net.digitalid.core.subject.CoreSubject;

/**
 * A synchronized property belongs to a {@link CoreSubject concept} and synchronizes across {@link Unit sites}.
 * 
 * @see WritableSynchronizedValueProperty
 */
@Mutable
@ThreadSafe
public interface SynchronizedProperty<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable ENTRY extends PersistentPropertyEntry<SUBJECT>, @Unspecifiable OBSERVER extends Observer> extends PersistentProperty<SUBJECT, ENTRY, OBSERVER> {
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SynchronizedPropertyTable<ENTITY, KEY, SUBJECT, ENTRY, ?> getTable();
    
}
