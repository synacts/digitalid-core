package net.digitalid.core.cryptography.compression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
