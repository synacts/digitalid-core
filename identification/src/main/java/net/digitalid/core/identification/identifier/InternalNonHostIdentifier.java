package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.Type;

/**
 * This interface models internal non-host identifiers.
 * 
 * (This type has to be a class because otherwise the static {@link #isValid(java.lang.String)} method would not be inherited by the generated subclass.)
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class InternalNonHostIdentifier extends RootClass implements InternalIdentifier, NonHostIdentifier {
    
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
    
    /**
     * Returns the non-host identifier of the given converter.
     */
    @Pure
    @TODO(task = "Use the fully qualified name to derive a suitable identifier.", date = "2016-12-20", author = Author.KASPAR_ETTER)
    public static @Nonnull InternalNonHostIdentifier of(@Nonnull Converter<?, ?> converter) {
        return with(converter.getTypeName().toLowerCase() + "@core.digitalid.net");
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull InternalNonHostIdentity resolve() throws ExternalException {
        final @Nonnull InternalNonHostIdentity identity = PseudoIdentifierResolver.resolveWithProvider(this).castTo(InternalNonHostIdentity.class);
        // If the returned identity is a type, its fields need to be loaded from the type's attributes.
        if (identity instanceof Type) { ((Type) identity).ensureLoaded(); }
        return identity;
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return HostIdentifier.with(getString().substring(getString().indexOf('@') + 1));
    }
    
    /* -------------------------------------------------- String with Dot -------------------------------------------------- */
    
    /**
     * Returns the string of this identifier with a leading dot or @.
     * This is useful for dynamically creating subtypes of existing types.
     */
    @Pure
    public @Nonnull String getStringWithLeadingDot() {
        final @Nonnull String string = getString();
        return (string.startsWith("@") ? "" : ".") + string;
    }
    
}
