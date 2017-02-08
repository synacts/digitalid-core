package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.SymmetricKey;

/**
 * This class encrypts the wrapped object as a response for encoding and decrypts it for decoding.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ResponseEncryption<@Unspecifiable OBJECT> extends Encryption<OBJECT> {
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable HostIdentifier getRecipient() {
        return null;
    }
    
    /* -------------------------------------------------- Symmetric Key -------------------------------------------------- */
    
    /**
     * Returns the symmetric key that has been or will be used to encrypt the object.
     */
    @Pure
    @Provided
    public abstract @Nonnull SymmetricKey getSymmetricKey();
    
    /* -------------------------------------------------- Initialization Vector -------------------------------------------------- */
    
    /**
     * Returns the initialization vector of the symmetric encryption scheme (AES).
     */
    @Pure
    @Default("net.digitalid.core.symmetrickey.InitializationVectorBuilder.build()")
    public abstract @Nonnull InitializationVector getInitializationVector();
    
}
