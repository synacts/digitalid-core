package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.Identity;

/**
 * This interface models identifiers.
 * 
 * @see InternalIdentifier
 * @see NonHostIdentifier
 */
@Immutable
@GenerateConverter
public interface Identifier extends RootInterface {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     * This method is called by the validity checkers of the subtypes to prevent infinite recursion.
     */
    @Pure
    public static boolean isConforming(@Nonnull String string) {
        return string.length() < 64;
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     * This method delegates the validation to the subtypes.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains(":") ? ExternalIdentifier.isValid(string) : InternalIdentifier.isValid(string);
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns the string of this identifier.
     */
    @Pure
    public abstract @Nonnull @Valid String getString();
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a new identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull Identifier with(@Nonnull @Valid String string) {
        return string.contains(":") ? ExternalIdentifier.with(string) : InternalIdentifier.with(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    /**
     * Resolves this identifier to an identity.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Identity resolve() throws ExternalException;
    
}
