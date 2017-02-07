package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.Positive;
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
@TODO(task = "Remove most getters from this class and create the subtypes RequestEncryption and ResponseEncryption.", date = "2017-02-07", author = Author.KASPAR_ETTER)
public abstract class Encryption<@Unspecifiable TYPE> {
    
    /* -------------------------------------------------- Encrypted Object -------------------------------------------------- */
    
    /**
     * Returns the object which has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull TYPE getObject();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient for which the object has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getRecipient();
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * The time at which the object has been or will be encrypted.
     * This information is required to retrieve the appropriate
     * {@link PublicKey public} and {@link PrivateKey private key}
     * from the host's key chain.
     */
    @Pure
    @Default("net.digitalid.database.auxiliary.TimeBuilder.build()")
    public abstract @Nonnull @Positive Time getTime();
    
    /* -------------------------------------------------- Symmetric Key -------------------------------------------------- */
    
    /**
     * Returns the symmetric key that has been or will be used to encrypt the object.
     */
    @Pure
    @Default("net.digitalid.core.symmetrickey.SymmetricKeyBuilder.build()")
    public abstract @Nonnull SymmetricKey getSymmetricKey();
    
    /* -------------------------------------------------- Initialization Vector -------------------------------------------------- */
    
    /**
     * Returns the initialization vector of the symmetric encryption scheme (AES).
     */
    @Pure
    @Default("net.digitalid.core.symmetrickey.InitializationVectorBuilder.build()")
    public abstract @Nonnull InitializationVector getInitializationVector();
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    /**
     * Returns the public key of the recipient.
     */
    @Pure
    @Derive("net.digitalid.core.asymmetrickey.PublicKeyRetriever.retrieve(recipient, time)")
    public abstract @Nonnull PublicKey getPublicKey();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    @TODO(task = "Remove this constructor as soon as we can declare exceptions in derive expressions.", date = "2017-01-28", author = Author.KASPAR_ETTER)
    protected Encryption() throws ExternalException {}
    
}
