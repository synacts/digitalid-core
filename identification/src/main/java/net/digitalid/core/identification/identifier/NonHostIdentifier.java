package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.NonHostIdentity;

/**
 * This interface models non-host identifiers.
 * 
 * @see InternalNonHostIdentifier
 * @see ExternalIdentifier
 */
@Immutable
@GenerateConverter
public interface NonHostIdentifier extends Identifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string is a valid non-host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String address) {
        return InternalIdentifier.isValid(address) && address.contains("@");
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull NonHostIdentifier with(@Nonnull @Valid String address) {
        return address.contains(":") ? ExternalIdentifier.with(address) : InternalNonHostIdentifier.with(address);
    } 
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity resolve() throws ExternalException;
    
}
