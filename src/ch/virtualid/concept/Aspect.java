package ch.virtualid.concept;

import javax.annotation.Nonnull;

/**
 * This class models {@link Aspect aspects} of {@link Concept concepts} that can be (@link Observer observed}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Aspect {
    
    /**
     * Stores the class of the concept to which this aspect belongs.
     */
    private final @Nonnull Class<? extends Concept> concept;
    
    /**
     * Stores the name of this aspect in the given concept.
     */
    private final @Nonnull String name;
    
    /**
     * Creates a new aspect of the given concept with the given name.
     * 
     * @param concept the class of the concept to which this aspect belongs.
     * @param name the name of this aspect in the given concept.
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
    public @Nonnull Class<? extends Concept> getConcept() {
        return concept;
    }
    
    /**
     * Returns the name of this aspect in the given concept.
     * 
     * @return the name of this aspect in the given concept.
     */
    public @Nonnull String getName() {
        return name;
    }
    
    @Override
    public @Nonnull String toString() {
        return concept.getName();
    }
    
}
