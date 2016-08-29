package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;

import net.digitalid.core.identification.identity.SemanticType;

/**
 *
 */
public abstract class Selfcontained<T> {
    
    @Pure
    public abstract @Nonnull SemanticType getSemanticType();
    
    @Pure
    public abstract @Nonnull T getObject();
    
}
