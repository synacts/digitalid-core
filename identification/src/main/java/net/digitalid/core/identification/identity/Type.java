package net.digitalid.core.identification.identity;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 */
@Mutable
public abstract class Type extends RelocatableIdentity implements InternalNonHostIdentity {
    
    /* -------------------------------------------------- Loaded -------------------------------------------------- */
    
    private boolean loaded = false;
    
    /**
     * Returns whether the type declaration is loaded.
     */
    @Pure
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Sets the type declaration to being loaded.
     * 
     * @ensure isLoaded() : "The type declaration has to be loaded.";
     */
    @Impure
    void setLoaded() {
        loaded = true;
    }
    
    /**
     * Loads the type declaration from the cache or the network.
     * Lazy loading is necessary for recursive type declarations.
     * 
     * @require isNotLoaded() : "The type declaration may not be loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has to be loaded.";
     */
    @Impure
    @NonCommitting
    abstract void load() throws ExternalException;
    
    /**
     * Ensures that the type declaration is loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    @Impure
    @NonCommitting
    public void ensureLoaded() throws ExternalException {
        if (!loaded) { load(); }
    }
    
}
