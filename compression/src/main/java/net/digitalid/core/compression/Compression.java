package net.digitalid.core.compression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class Compression<T> {
    
    @Pure
    public abstract @Nonnull T getObject();
    
}
