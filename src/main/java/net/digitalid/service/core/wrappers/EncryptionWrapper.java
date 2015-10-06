package net.digitalid.core.wrappers;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.collections.annotations.freezable.Frozen;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.database.annotations.Locked;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.collections.freezable.FreezableArray;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.InitializationVector;
import net.digitalid.core.cryptography.PrivateKey;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.system.logger.Log;
import net.digitalid.core.server.Server;
import net.digitalid.core.factory.Storable;
import net.digitalid.collections.tuples.FreezablePair;
import net.digitalid.collections.tuples.ReadOnlyPair;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code encryption@core.digitalid.net}.
 * The structure of encrypted blocks is a tuple that consists of the time of the encryption, the receiving
 * host's identifier, the encrypted key, the initialization vector and the possibly encrypted element.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class EncryptionWrapper extends BlockBasedWrapper<EncryptionWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code encryption@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("encryption@core.digitalid.net").load(1);
    
    /**
     * Stores the semantic type {@code recipient.encryption@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType RECIPIENT = SemanticType.map("recipient.encryption@core.digitalid.net").load(HostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code key.encryption@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType KEY = SemanticType.map("key.encryption@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code implementation.encryption@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.map("implementation.encryption@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, RECIPIENT, KEY, InitializationVector.TYPE, SemanticType.UNKNOWN);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cache –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Caches the encrypted key for a given pair of public key and symmetric key.
     */
    private static final @Nonnull Map<ReadOnlyPair<PublicKey, SymmetricKey>, Block> encryptions = ConcurrentHashMap.get();
    
    /**
     * Encrypts the given symmetric key for the given public key.
     * 
     * @param publicKey the public key for which the given symmetric key is to be encrypted.
     * @param symmetricKey the symmetric key which is to be encrypted for the given public key.
     * 
     * @return a block containing the given symmetric key encrypted for the given public key.
     */
    private static @Nonnull Block encrypt(@Nonnull PublicKey publicKey, @Nonnull SymmetricKey symmetricKey) {
        final @Nonnull @Frozen ReadOnlyPair<PublicKey, SymmetricKey> pair = FreezablePair.get(publicKey, symmetricKey).freeze();
        @Nullable Block key = encryptions.get(pair);
        if (key == null) {
            final @Nonnull Time start = Time.getCurrent();
            key = Block.fromNonNullable(publicKey.getCompositeGroup().getElement(symmetricKey.getValue()).pow(publicKey.getE()), KEY);
            encryptions.put(pair, key);
            Log.verbose("Symmetric key encrypted in " + start.ago().getValue() + " ms.");
        }
        return key;
    }
    
    /**
     * Caches the symmetric key for a given pair of private key and encrypted key.
     */
    private static final @Nonnull Map<ReadOnlyPair<PrivateKey, Block>, SymmetricKey> decryptions = ConcurrentHashMap.get();
    
    /**
     * Decrypts the given key with the given private key.
     * 
     * @param privateKey the private key for which the given key is encrypted.
     * @param key the block containing the encrypted key for the given private key.
     * 
     * @return the symmetric key with the decrypted value of the given encrypted key.
     */
    private static @Nonnull SymmetricKey decrypt(@Nonnull PrivateKey privateKey, @Nonnull Block key) throws InvalidEncodingException {
        final @Nonnull @Frozen ReadOnlyPair<PrivateKey, Block> pair = FreezablePair.get(privateKey, key).freeze();
        @Nullable SymmetricKey symmetricKey = decryptions.get(pair);
        if (symmetricKey == null) {
            final @Nonnull Time start = Time.getCurrent();
            final @Nonnull BigInteger value = IntegerWrapper.decodeNonNullable(key);
            symmetricKey = SymmetricKey.get(privateKey.powD(value).getValue());
            decryptions.put(pair, symmetricKey);
            Log.verbose("Symmetric key decrypted in " + start.ago().getValue() + " ms.");
        }
        return symmetricKey;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Time –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the time of encryption.
     */
    private final @Nonnull Time time;
    
    /**
     * Returns the time of encryption.
     * 
     * @return the time of encryption.
     */
    @Pure
    public @Nonnull Time getTime() {
        return time;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Element –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the element of this wrapper.
     * 
     * @invariant element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(0)) : "The element is based on the parameter of the semantic type.";
     */
    private final @Nonnull Block element;
    
    /**
     * Returns the element of this wrapper.
     * 
     * @return the element of this wrapper.
     * 
     * @ensure element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(0)) : "The element is based on the parameter of the semantic type.";
     */
    @Pure
    public @Nonnull Block getElement() {
        return element;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Recipient –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the identifier of the host for which the element is encrypted or null if the recipient is not known.
     */
    private final @Nullable HostIdentifier recipient;
    
    /**
     * Returns the identifier of the host for which the element is encrypted or null if the recipient is not known.
     * If the recipient is not null, the encryption is part of a request. If the recipient is null, it is part of a response.
     * 
     * @return the identifier of the host for which the element is encrypted or null if the recipient is not known.
     */
    @Pure
    public @Nullable HostIdentifier getRecipient() {
        return recipient;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Symmetric Key –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the symmetric key that is used for the encryption or decryption of the element or null if no encryption is used.
     */
    private final @Nullable SymmetricKey symmetricKey;
    
    /**
     * Returns the symmetric key that is used for the encryption or decryption of the element or null if no encryption is used.
     * 
     * @return the symmetric key that is used for the encryption or decryption of the element or null if no encryption is used.
     */
    @Pure
    public @Nullable SymmetricKey getSymmetricKey() {
        return symmetricKey;
    }
    
    /**
     * Returns whether the element is encrypted.
     * 
     * @return whether the element is encrypted.
     */
    @Pure
    public boolean isEncrypted() {
        return symmetricKey != null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Initialization Vector –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the initialization vector that is used for the encryption of the element or null if no encryption is used.
     */
    private final @Nullable InitializationVector initializationVector;
    
    /**
     * Returns the initialization vector that is used for the encryption of the element or null if no encryption is used.
     * 
     * @return the initialization vector that is used for the encryption of the element or null if no encryption is used.
     */
    @Pure
    public @Nullable InitializationVector getInitializationVector() {
        return initializationVector;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Public Key –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the public key that is used for the encryption of the symmetric key or null if the block is decoded.
     */
    private final @Nullable PublicKey publicKey;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new encryption wrapper with the given type and element.
     * 
     * @param type the semantic type of the new encryption wrapper.
     * @param element the element of the new encryption wrapper.
     * @param recipient the identifier of the host for which the element is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key that is used for the encryption of the element or null if no encryption is used.
     * 
     * @require element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is based on the parameter of the given type.";
     */
    @Locked
    @NonCommitting
    private EncryptionWrapper(@Nonnull @Loaded @BasedOn("encryption@core.digitalid.net") SemanticType type, @Nonnull Block element, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        super(type);
        
        assert element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is based on the parameter of the given type.";
        
        this.time = Time.getCurrent();
        this.element = element;
        this.recipient = recipient;
        this.symmetricKey = symmetricKey;
        this.initializationVector = symmetricKey == null ? null : InitializationVector.getRandom();
        this.publicKey = (recipient == null || symmetricKey == null) ? null : Cache.getPublicKey(recipient, time);
    }
    
    /**
     * Creates a new encryption wrapper from the given block.
     * 
     * @param block the block that contains the encrypted element.
     * @param symmetricKey the symmetric key used for decryption or null if the element is encrypted for a host or not at all.
     */
    private EncryptionWrapper(@Nonnull @NonEncoding @BasedOn("encryption@core.digitalid.net") Block block, @Nullable SymmetricKey symmetricKey) throws InvalidEncodingException {
        super(block.getType());
        
        this.cache = Block.get(IMPLEMENTATION, block);
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(cache);
        
        this.time = new Time(tuple.getNonNullableElement(0));
        
        this.initializationVector = InitializationVector.FACTORY.decodeNullable(tuple.getNullableElement(3));
        final @Nullable Block encryptedKey = tuple.getNullableElement(2);
        
        if (tuple.isElementNull(1)) {
            // The encryption is part of a response.
            this.recipient = null;
            if (encryptedKey != null) throw new InvalidEncodingException("A response may not include an encrypted symmetric key.");
            if (initializationVector != null) {
                if (symmetricKey == null) throw new InvalidEncodingException("A symmetric key is needed in order to decrypt the response.");
                this.symmetricKey = symmetricKey;
            } else {
                // It can happen that a requester expects an encryption but the host is not able to decipher the symmetric key and thus cannot encrypt the response.
                this.symmetricKey = null;
            }
        } else {
            // The encryption is part of a request.
            this.recipient = IdentifierClass.create(tuple.getNonNullableElement(1)).toHostIdentifier();
            if (!Server.hasHost(recipient)) throw new InvalidEncodingException(recipient + " does not run on this server.");
            if (symmetricKey != null) throw new InvalidEncodingException("A response may not include a recipient.");
            if (encryptedKey != null) {
                if (initializationVector == null) throw new InvalidEncodingException("An initialization vector is needed to decrypt an element.");
                final @Nonnull PrivateKey privateKey = Server.getHost(recipient).getPrivateKeyChain().getKey(time);
                this.symmetricKey = decrypt(privateKey, encryptedKey);
            } else {
                if (initializationVector != null) throw new InvalidEncodingException("If a request is encrypted, a symmetric key has to be provided.");
                this.symmetricKey = null;
            }
        }
        
        final @Nonnull Block element = tuple.getNonNullableElement(4);
        final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
        final @Nullable SymmetricKey sk = this.symmetricKey;
        final @Nullable InitializationVector iv = this.initializationVector;
        if (sk != null && iv != null) {
            final @Nonnull Time start = Time.getCurrent();
            this.element = element.decrypt(parameter, sk, iv);
            Log.verbose("Element with " + element.getLength() + " bytes decrypted in " + start.ago().getValue() + " ms.");
        } else {
            this.element = element.setType(parameter);
        }
        
        this.publicKey = null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encrypts the given element with a new encryption wrapper.
     * 
     * @param type the semantic type of the new encryption wrapper.
     * @param element the element of the new encryption wrapper.
     * @param recipient the identifier of the host for which the element is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key that is used for the encryption of the element or null if no encryption is used.
     * 
     * @return a new encryption wrapper with the given parameters.
     * 
     * @require element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is based on the parameter of the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull <V extends Storable<V>> EncryptionWrapper encrypt(@Nonnull @Loaded @BasedOn("encryption@core.digitalid.net") SemanticType type, @Nonnull V element, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        return new EncryptionWrapper(type, Block.fromNonNullable(element), recipient, symmetricKey);
    }
    
    /**
     * Decrypts the given block with a new encryption wrapper. 
     * 
     * @param block the block that contains the encrypted element.
     * @param symmetricKey the symmetric key used for decryption or null if the element is encrypted for a host or not at all.
     * 
     * @return a new encryption wrapper with the given parameters.
     */
    @Pure
    public static @Nonnull EncryptionWrapper decrypt(@Nonnull @NonEncoding @BasedOn("encryption@core.digitalid.net") Block block, @Nullable SymmetricKey symmetricKey) throws InvalidEncodingException {
        return new EncryptionWrapper(block, symmetricKey);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encryption of the element.
     */
    private @Nullable @BasedOn("implementation.encryption@core.digitalid.net") Block cache;
    
    /**
     * Returns the cached encryption of the element.
     * 
     * @return the cached encryption of the element.
     */
    @Pure
    private @Nonnull Block getCache() {
        if (cache == null) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(5);
            elements.set(0, time.toBlock());
            elements.set(1, Block.<Identifier>fromNullable(recipient, RECIPIENT));
            
            if (recipient != null && symmetricKey != null) {
                assert publicKey != null : "The public key is not null because this method is only called for encoding a block.";
                elements.set(2, encrypt(publicKey, symmetricKey));
            }
            
            elements.set(3, Block.fromNullable(initializationVector));
            
            if (symmetricKey != null && initializationVector != null) {
                final @Nonnull Time start = Time.getCurrent();
                elements.set(4, element.encrypt(SemanticType.UNKNOWN, symmetricKey, initializationVector));
                Log.verbose("Element with " + element.getLength() + " bytes encrypted in " + start.ago().getValue() + " ms.");
            } else {
                elements.set(4, element);
            }
            
            cache = TupleWrapper.encode(IMPLEMENTATION, elements.freeze());
        }
        return cache;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return getCache().getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        getCache().writeTo(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends BlockBasedWrapper.Factory<EncryptionWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("encryption@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull EncryptionWrapper decodeNonNullable(@Nonnull @NonEncoding @BasedOn("encryption@core.digitalid.net") Block block) throws InvalidEncodingException {
            return new EncryptionWrapper(block, null);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return new Factory(getSemanticType());
    }
    
}
