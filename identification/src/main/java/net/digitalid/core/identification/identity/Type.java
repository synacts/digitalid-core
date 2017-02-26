package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.method.Chainable;
import net.digitalid.utility.validation.annotations.method.Ensures;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.annotations.type.Loadable;
import net.digitalid.core.annotations.type.NonLoadedRecipient;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 */
@Mutable
public abstract class Type extends RelocatableIdentity implements InternalNonHostIdentity, Loadable {
    
    /* -------------------------------------------------- Loaded -------------------------------------------------- */
    
    /**
     * Loads the type declaration from the cache or the network.
     * Lazy loading is necessary for recursive type declarations.
     */
    @Impure
    @Chainable
    @NonCommitting
    @NonLoadedRecipient
    @Ensures(condition = "isLoaded()", message = "The type declaration has to be loaded.")
    abstract @Nonnull Type load() throws ExternalException;
    
    /**
     * Ensures that the type declaration is loaded.
     */
    @Impure
    @NonCommitting
    @Ensures(condition = "isLoaded()", message = "The type declaration has to be loaded.")
    public void ensureLoaded() throws ExternalException {
        if (!isLoaded()) { load(); }
    }
    
}
