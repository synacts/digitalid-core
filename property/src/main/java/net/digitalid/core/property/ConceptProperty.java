package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.property.Property;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.extensible.ExtensibleConceptProperty;
import net.digitalid.core.property.indexed.IndexedConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nullable.NullableConceptProperty;

/**
 * This interface is implemented by all {@link Property properties} that belong to a {@link Concept concept}.
 * 
 * @see NullableConceptProperty
 * @see NonNullableConceptProperty
 * @see ExtensibleConceptProperty
 * @see IndexedConceptProperty
 */
@Mutable
public interface ConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity> {
    
    /**
     * Returns the property info that contains the property and value converters, the required authorization and the value validator.
     */
    @Pure
    public @Nonnull ConceptPropertyInfo<V, C, E> getPropertyInfo();
    
    /**
     * Returns the concept to which this property belongs.
     */
    @Pure
    public @Nonnull C getConcept();
    
    /**
     * Returns the time of the last modification.
     */
    @Pure
    @NonCommitting
    public @Nonnull Time getTime() throws DatabaseException;
    
    /**
     * Resets the time and value of this property.
     */
    @NonCommitting
    public void reset() throws DatabaseException;
    
}
