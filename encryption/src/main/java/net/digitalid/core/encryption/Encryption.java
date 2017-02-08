package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This class encrypts the wrapped object for encoding and decrypts it for decoding.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Encryption<@Unspecifiable OBJECT> {
    
    /* -------------------------------------------------- Encrypted Object -------------------------------------------------- */
    
    /**
     * Returns the object which has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull OBJECT getObject();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient for which the object has been or will be encrypted or null for responses.
     */
    @Pure
    public abstract @Nullable HostIdentifier getRecipient();
    
}
