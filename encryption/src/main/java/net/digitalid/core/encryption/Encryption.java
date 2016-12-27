package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.SymmetricKey;

/**
 * This class encrypts the wrapped object for encoding and decrypts it for decoding.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Encryption<@Unspecifiable TYPE> {
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * The time at which the object has been or will be encrypted.
     * This information is required to retrieve the appropriate
     * {@link PublicKey public} and {@link PrivateKey private key}
     * from the host's key chain.
     */
    @Pure
    public abstract @Nonnull Time getTime();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient for which the object has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getRecipient();
    
    /* -------------------------------------------------- Symmetric Key -------------------------------------------------- */
    
    /**
     * Returns the symmetric key that has been or will be used to encrypt the object.
     */
    @Pure
    public abstract @Nonnull SymmetricKey getSymmetricKey();
    
    /* -------------------------------------------------- Initialization Vector -------------------------------------------------- */
    
    /**
     * Returns the initialization vector of the symmetric encryption scheme (AES).
     */
    @Pure
    public abstract @Nonnull InitializationVector getInitializationVector();
    
    /* -------------------------------------------------- Encrypted Object -------------------------------------------------- */
    
    /**
     * Returns the object which has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull TYPE getObject();
    
}
