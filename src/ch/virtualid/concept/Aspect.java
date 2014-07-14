package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models {@link Aspect aspects} of {@link Concept concepts} that can be (@link Observer observed}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Aspect implements Immutable {
    
    /**
     * Stores the class of the concept to which this aspect belongs.
     */
    private final @Nonnull Class<? extends Concept> concept;
    
    /**
     * Stores the name of this aspect.
     */
    private final @Nonnull String name;
    
    /**
     * Creates a new aspect of the given concept with the given name.
     * 
     * @param concept the class of the concept to which this aspect belongs.
     * @param name the name of this aspect.
     */
    public Aspect(@Nonnull Class<? extends Concept> concept, @Nonnull String name) {
        this.concept = concept;
        this.name = name;
    }
    
    /**
     * Returns the class of the concept to which this aspect belongs.
     * 
     * @return the class of the concept to which this aspect belongs.
     */
    @Pure
    public @Nonnull Class<? extends Concept> getConcept() {
        return concept;
    }
    
    /**
     * Returns the name of this aspect.
     * 
     * @return the name of this aspect.
     */
    @Pure
    public @Nonnull String getName() {
        return name;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return concept.getName() + " " + name;
    }
    
}
