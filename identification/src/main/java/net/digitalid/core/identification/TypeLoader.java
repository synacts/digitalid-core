package net.digitalid.core.identification;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.tuples.Quintet;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.Type;

/**
 * The type loader loads the attributes of a type.
 * 
 * TODO: Do we need a SemanticTypeLoader and a SyntacticTypeLoader?
 */
@Mutable
public interface TypeLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Loads the attributes of the given type.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Quintet<?, ?, ?, ?, ?> load(@Nonnull Type type) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the type loader, which has to be provided by another package.
     */
    public static final @Nonnull Configuration<TypeLoader> configuration = Configuration.withUnknownProvider();
    
}
