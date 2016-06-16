package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.property.extensible.ExtensibleConceptProperty;
import net.digitalid.core.property.indexed.IndexedConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nullable.NullableConceptProperty;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.entity.Entity;

/**
 * This interface is implemented by all {@link ReadOnlyProperty properties} that belong to a {@link Concept concept}.
 * 
 * @see NullableConceptProperty
 * @see NonNullableConceptProperty
 * @see ExtensibleConceptProperty
 * @see IndexedConceptProperty
 */
public interface ConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity> {
    
    /**
     * Returns the property setup that contains the property and value converters, the required authorization and the value validator.
     * 
     * @return the property setup that contains the property and value converters, the required authorization and the value validator.
     */
    @Pure
    public @Nonnull ConceptPropertySetup<V, C, E> getConceptPropertySetup();
    
    /**
     * Returns the concept to which this property belongs.
     * 
     * @return the concept to which this property belongs.
     */
    @Pure
    public @Nonnull C getConcept();
    
    /**
     * Returns the time of the last modification.
     * 
     * @return the time of the last modification.
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull Time getTime() throws DatabaseException;
    
    /**
     * Resets the time and value of this property.
     */
    @Locked
    @NonCommitting
    public void reset() throws DatabaseException;
    
}
