package net.digitalid.core.property;

import javax.annotation.Nonnull;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface ConceptProperty {
    
    public @Nonnull Concept getConcept();
    
    /**
     * Returns the time of the last modification.
     * 
     * @return the time of the last modification.
     */
    public @Nonnull Time getTime();
    
}
