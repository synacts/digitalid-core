package ch.virtualid.concept;

import javax.annotation.Nonnull;

/**
 * Implementing this interface allows a class to observe changes in {@link Concept concepts}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface Observer {
    
    /**
     * Notifies the object about a change in the given aspect of the given concept.
     * 
     * @param aspect the aspect that changed in the given concept.
     * @param concept the concept that reported a change in the given aspect.
     */
    public void notify(@Nonnull Aspect aspect, @Nonnull Concept concept);
    
}
