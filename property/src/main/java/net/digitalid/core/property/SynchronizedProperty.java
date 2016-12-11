package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.property.Property;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.PersistentPropertyEntry;
import net.digitalid.database.subject.site.Site;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.value.WritableSynchronizedValueProperty;

/**
 * A synchronized property belongs to a {@link Concept concept} and synchronizes across {@link Site sites}.
 * 
 * @see WritableSynchronizedValueProperty
 */
@Mutable
@ThreadSafe
public interface SynchronizedProperty<E extends Entity, K, C extends Concept<E, K>, N extends PersistentPropertyEntry<C>, O extends Property.Observer> extends PersistentProperty<C, N, O> {
    
    /* -------------------------------------------------- Concept -------------------------------------------------- */
    
    /**
     * Returns the concept to which this property belongs.
     */
    @Pure
    public @Nonnull C getConcept();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull C getSubject() {
        return getConcept();
    }
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SynchronizedPropertyTable<E, K, C, N, ?> getTable();
    
}
