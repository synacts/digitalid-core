package net.digitalid.core.property.replaceable.nullable;

import javax.annotation.Nonnull;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;
import net.digitalid.core.property.ConceptProperty;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class NullableReplaceableConceptProperty<V extends Blockable & SQLizable> extends NullableReplaceableProperty<V> implements ConceptProperty {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private final @Nonnull Concept concept;
    
    protected NullableReplaceableConceptProperty(@Nonnull Concept concept) {
        this.concept = concept;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Loading –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    // TODO: Make the value loadable.
    
    // TODO: Introduce a reference to the database module/methods to load and change the value (or make this generic as well; usually either a column within a row or aggregating several rows)
    
    // with loading, resetting and synchronizing [resetting checks whether there are any observers and if there are, reloads the values and issues a notification only in case of a change]
    
    // TODO: Give the @OnlyForActions method only package-level visibility. (This annotation might no longer be necessary afterwards.)
    
    // TODO: Also store the time of the last modification.
    
    @Override
    public Concept getConcept() {
        throw new UnsupportedOperationException("getConcept in NullableReplaceableConceptProperty is not supported yet.");
    }
    
    @Override
    public Time getTime() {
        throw new UnsupportedOperationException("getTimeOfLastModification in NullableReplaceableConceptProperty is not supported yet.");
    }
    
}
