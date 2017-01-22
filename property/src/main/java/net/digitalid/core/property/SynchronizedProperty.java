package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.property.Observer;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.PersistentPropertyEntry;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.value.WritableSynchronizedValueProperty;

/**
 * A synchronized property belongs to a {@link CoreSubject concept} and synchronizes across {@link Unit sites}.
 * 
 * @see WritableSynchronizedValueProperty
 */
@Mutable
@ThreadSafe
public interface SynchronizedProperty<ENTITY extends Entity<?>, KEY, CONCEPT extends CoreSubject<ENTITY, KEY>, ENTRY extends PersistentPropertyEntry<CONCEPT>, OBSERVER extends Observer> extends PersistentProperty<CONCEPT, ENTRY, OBSERVER> {
    
    /* -------------------------------------------------- Concept -------------------------------------------------- */
    
    /**
     * Returns the concept to which this property belongs.
     */
    @Pure
    public @Nonnull CONCEPT getConcept();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull CONCEPT getSubject() {
        return getConcept();
    }
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SynchronizedPropertyTable<ENTITY, KEY, CONCEPT, ENTRY, ?> getTable();
    
}
