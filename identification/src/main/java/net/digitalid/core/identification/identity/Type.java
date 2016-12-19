package net.digitalid.core.identification.identity;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 */
@Mutable
@GenerateConverter
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
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the identity of the given address.
     */
    @Pure
    @Recover // TODO: Split into @Recover(Representation.INTERNAL) and @Recover(Representation.EXTERNAL) or something similar.
    static @Nonnull Type with(long key, @Nonnull NonHostIdentifier address) {
        try {
            // TODO: The following cast should probably not throw an internal exception.
            return IdentifierResolver.resolve(address).castTo(Type.class);
        } catch (@Nonnull ExternalException exception) {
            // TODO: How to handle this?
            throw new RuntimeException(exception);
        }
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Remove this method once the key is no longer encoded for transmission.", date = "2016-12-19", author = Author.KASPAR_ETTER)
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || !(object instanceof Type)) {
            return false;
        }
        final @Nonnull Type that = (Type) object;
        boolean result = true;
        result = result && Objects.equals(this.getAddress(), that.getAddress());
        return result;
    }
    
    @Pure
    @Override
    @TODO(task = "Remove this method once the key is no longer encoded for transmission.", date = "2016-12-19", author = Author.KASPAR_ETTER)
    public int hashCode() {
        return getAddress().hashCode();
    }
    
}
