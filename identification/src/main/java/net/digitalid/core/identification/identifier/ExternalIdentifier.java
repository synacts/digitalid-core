package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.Person;

/**
 * This interface models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 */
@Immutable
@GenerateConverter
public interface ExternalIdentifier extends NonHostIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return Identifier.isConforming(string) && string.contains(":");
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        final int index = string.indexOf(':');
        if (index < 1) { return false; }
        final @Nonnull String scheme = string.substring(0, index);
        switch (scheme) {
            case "email": return EmailIdentifier.isValid(string);
            case "mobile": return MobileIdentifier.isValid(string);
            default: return false;
        }
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a new external identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull ExternalIdentifier with(@Nonnull @Valid String string) {
        final @Nonnull String scheme = string.substring(0, string.indexOf(':'));
        switch (scheme) {
            case "email": return EmailIdentifier.with(string);
            case "mobile": return MobileIdentifier.with(string);
            default: throw UnexpectedValueException.with("scheme", scheme);
        }
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person getIdentity() throws ExternalException;
    
}
