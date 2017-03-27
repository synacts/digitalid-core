package net.digitalid.core.identification. identifier;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.InternalIdentity;

/**
 * This interface models internal identifiers.
 * 
 * @see HostIdentifier
 * @see InternalNonHostIdentifier
 */
@Immutable
@GenerateConverter
public abstract class InternalIdentifier extends RootClass implements Identifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid internal identifiers have to match.
     */
    public static final @Nonnull Pattern PATTERN = Pattern.compile("(?:(?:[a-z0-9]+(?:[._-][a-z0-9]+)*)?@)?[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     */
    @Pure
    public static boolean isConforming(@Nonnull String string) {
        return Identifier.isConforming(string) && PATTERN.matcher(string).matches();
    }
    
    /**
     * Returns whether the given string is a valid internal identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains("@") ? InternalNonHostIdentifier.isValid(string) : HostIdentifier.isValid(string);
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a new internal identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull InternalIdentifier with(@Nonnull @Valid String string) {
        return string.contains("@") ? InternalNonHostIdentifier.with(string) : HostIdentifier.with(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull InternalIdentity resolve() throws ExternalException;
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether an identity with this internal identifier exists.
     * If any exceptions occur, this method returns false.
     */
    @Pure
    @NonCommitting
    public boolean exists() {
        try {
            IdentifierResolver.configuration.get().resolve(this);
            return true;
        } catch (@Nonnull ExternalException exception) {
            return false;
        }
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    /**
     * Returns the host part of this internal identifier.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull HostIdentifier getHostIdentifier();
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return getString();
    }
    
}
