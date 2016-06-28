package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.Type;

/**
 * This interface models internal non-host identifiers.
 */
@Immutable
@GenerateSubclass
// TODO: @GenerateConverter
public interface InternalNonHostIdentifier extends InternalIdentifier, NonHostIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string is a valid non-host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && string.contains("@");
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a non-host identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull InternalNonHostIdentifier with(@Nonnull @Valid String string) {
        return new InternalNonHostIdentifierSubclass(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public default @Nonnull InternalNonHostIdentity getIdentity() throws ExternalException {
        final @Nonnull InternalNonHostIdentity identity = IdentifierResolver.configuration.get().resolve(this).castTo(InternalNonHostIdentity.class);
        // If the returned identity is a type, its fields need to be loaded from the type's attributes.
        if (identity instanceof Type) { ((Type) identity).ensureLoaded(); }
        return identity;
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull HostIdentifier getHostIdentifier() {
        return HostIdentifier.with(getString().substring(getString().indexOf("@") + 1));
    }
    
    /* -------------------------------------------------- String with Dot -------------------------------------------------- */
    
    /**
     * Returns the string of this identifier with a leading dot or @.
     * This is useful for dynamically creating subtypes of existing types.
     */
    @Pure
    @Deprecated // TODO: Maybe no longer necessary if almost all types are generated implicitly.
    public default @Nonnull String getStringWithLeadingDot() {
        final @Nonnull String string = getString();
        return (string.startsWith("@") ? "" : ".") + string;
    }
    
}