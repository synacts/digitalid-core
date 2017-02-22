package net.digitalid.core.subject.utility;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.subject.CoreSubject;

/**
 * This class is an alternative to {@link Void} to comply with non-nullable parameters and return values.
 * An example of this is the {@link CoreSubject#getKey()} method and you can use {@link None} if a {@link CoreSubject} has no key.
 */
@Stateless
@GenerateConverter
public final class None {
    
    /**
     * Creates a new none.
     */
    private None() {}
    
    /**
     * Stores the only instance of this class.
     */
    public static final @Nonnull None INSTANCE = new None();
    
    /**
     * Returns the only instance of this class.
     */
    @Pure
    @Recover
    public static @Nonnull None getInstance() {
        return INSTANCE;
    }
    
}
