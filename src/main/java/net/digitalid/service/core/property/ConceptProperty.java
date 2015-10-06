package net.digitalid.core.property;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.database.annotations.Locked;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.property.extensible.ExtensibleConceptProperty;
import net.digitalid.core.property.indexed.IndexedConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nullable.NullableConceptProperty;

/**
 * This interface is implemented by all {@link ReadOnlyProperty properties} that belong to a {@link Concept concept}.
 * 
 * @see NullableConceptProperty
 * @see NonNullableConceptProperty
 * @see ExtensibleConceptProperty
 * @see IndexedConceptProperty
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface ConceptProperty<C extends Concept<C, ?, ?>> {
    
    /**
     * Returns the concept of this property.
     * 
     * @return the concept of this property.
     */
    @Pure
    public @Nonnull C getConcept();
    
    /**
     * Returns the table which stores the time and value of this property.
     * 
     * @return the table which stores the time and value of this property.
     */
    @Pure
    public @Nonnull ConceptPropertyTable<?, C, ?> getTable();
    
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
