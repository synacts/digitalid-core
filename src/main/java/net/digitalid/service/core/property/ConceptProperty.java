package net.digitalid.service.core.property;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.property.extensible.ExtensibleConceptProperty;
import net.digitalid.service.core.property.indexed.IndexedConceptProperty;
import net.digitalid.service.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.service.core.property.nullable.NullableConceptProperty;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This interface is implemented by all {@link ReadOnlyProperty properties} that belong to a {@link Concept concept}.
 * 
 * @see NullableConceptProperty
 * @see NonNullableConceptProperty
 * @see ExtensibleConceptProperty
 * @see IndexedConceptProperty
 */
public interface ConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity<E>> {
    
    /**
     * Returns the property factory that contains the required information.
     * 
     * @return the property factory that contains the required information.
     */
    @Pure
    public @Nonnull ConceptPropertyFactory<V, C, E> getPropertyFactory();
    
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
    public @Nonnull Time getTime() throws SQLException;
    
    /**
     * Resets the time and value of this property.
     */
    @Locked
    @NonCommitting
    public void reset() throws SQLException;
    
}
