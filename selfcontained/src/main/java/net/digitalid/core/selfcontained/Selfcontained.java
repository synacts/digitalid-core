package net.digitalid.core.selfcontained;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;

import net.digitalid.core.identification.identity.SemanticType;

/**
 *
 */
@TODO(task = "The selfcontained type should probably have no generic type parameter because that is the whole point of being self-contained.", date = "2016-10-12", author = Author.KASPAR_ETTER)
public abstract class Selfcontained<T> {
    
    @Pure
    public abstract @Nonnull SemanticType getSemanticType();
    
    @Pure
    public abstract @Nonnull T getObject();
    
}
