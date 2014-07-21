package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models {@link Aspect aspects} of {@link Instance instances} that can be (@link Observer observed}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Aspect implements Immutable {
    
    /**
     * Stores the class to which this aspect belongs.
     */
    private final @Nonnull Class<? extends Instance> clazz;
    
    /**
     * Stores the name of this aspect.
     */
    private final @Nonnull String name;
    
    /**
     * Creates a new aspect of the given class with the given name.
     * 
     * @param clazz the class to which this aspect belongs.
     * @param name the name of this aspect.
     */
    public Aspect(@Nonnull Class<? extends Instance> clazz, @Nonnull String name) {
        this.clazz = clazz;
        this.name = name;
    }
    
    /**
     * Returns the class to which this aspect belongs.
     * 
     * @return the class to which this aspect belongs.
     */
    @Pure
    public @Nonnull Class<? extends Instance> getClazz() {
        return clazz;
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
        return clazz.getName() + " " + name;
    }
    
}
