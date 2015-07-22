package net.digitalid.core.property.replaceable.nullable;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.property.ValueValidator;
import net.digitalid.core.property.replaceable.nonnullable.NonNullableReplaceableConceptPropertyTable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class NullableReplaceableConceptProperty<V, C extends Concept> extends NullableReplaceableProperty<V> implements ConceptProperty<C> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the concept to which this property belongs.
     */
    private final @Nonnull C concept;
    
    @Pure
    @Override
    public @Nonnull C getConcept() {
        return concept;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    // TODO: Introduce a reference to the database module/methods to load and change the value (or make this generic as well; usually either a column within a row or aggregating several rows)
    private final @Nonnull NonNullableReplaceableConceptPropertyTable<V> table; // TODO: Change to nullable table.
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private NullableReplaceableConceptProperty(@Nonnull ValueValidator<? super V> validator, @Nonnull C concept, @Nonnull NonNullableReplaceableConceptPropertyTable<V> table) {
        super(validator);
        
        this.concept = concept;
        this.table = table;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Loading –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    // TODO: Make the value loadable.
    
    // TODO: Introduce a reference to the database module/methods to load and change the value (or make this generic as well; usually either a column within a row or aggregating several rows)
    
    // with loading, resetting and synchronizing [resetting checks whether there are any observers and if there are, reloads the values and issues a notification only in case of a change]
    
    // TODO: Give the @OnlyForActions method only package-level visibility. (This annotation might no longer be necessary afterwards.)
    
    // TODO: Also store the time of the last modification.
    
    @Override
    public Time getTime() {
        throw new UnsupportedOperationException("getTimeOfLastModification in NullableReplaceableConceptProperty is not supported yet.");
    }
    
}
