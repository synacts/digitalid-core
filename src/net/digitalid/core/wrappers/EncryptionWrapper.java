package net.digitalid.core.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.InflaterOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoded;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.InitializationVector;
import net.digitalid.core.cryptography.PrivateKey;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.io.Log;
import net.digitalid.core.server.Server;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;

/**
 * Wraps an element with the syntactic type {@code encryption@core.digitalid.net} for encoding and decoding.
 * The structure of encrypted blocks is a tuple that consists of the time of the encryption, the receiving
 * host's identifier, the encrypted key, the initialization vector and the possibly encrypted element.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class EncryptionWrapper extends BlockWrapper<EncryptionWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code encryption@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("encryption@core.digitalid.net").load(1);
    
    /**
     * Stores the semantic type {@code semantic.encryption@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.encryption@core.digitalid.net").load(TYPE);
    
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
            final @Nonnull Time start = new Time();
            key = publicKey.getCompositeGroup().getElement(symmetricKey.getValue()).pow(publicKey.getE()).toBlock().setType(KEY);
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
            final @Nonnull Time start = new Time();
            final @Nonnull BigInteger value = IntegerWrapper.decodeNonNullable(key);
            symmetricKey = new SymmetricKey(privateKey.powD(value).getValue());
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
     * 
     * @return the identifier of the host for which the element is encrypted or null if the recipient is not known.
     */
    @Pure
    public @Nullable HostIdentifier getRecipient() {
        return recipient;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Symmetric Key –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the symmetric key that is used for the encryption of the element or null if no encryption is used.
     */
    private final @Nullable SymmetricKey symmetricKey;
    
    /**
     * Returns the symmetric key that is used for the encryption of the element or null if no encryption is used.
     * 
     * @return the symmetric key that is used for the encryption of the element or null if no encryption is used.
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
    @NonCommitting
    private EncryptionWrapper(@Nonnull @Loaded @BasedOn("encryption@core.digitalid.net") SemanticType type, @Nonnull Block element, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        super(type);
        
        assert element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is based on the parameter of the given type.";
        
        this.time = new Time();
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
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    private EncryptionWrapper(@Nonnull @BasedOn("encryption@core.digitalid.net") Block block, @Nullable SymmetricKey symmetricKey) throws InvalidEncodingException {
        super(block.getType());
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(Block.get(IMPLEMENTATION, block));
        this.time = new Time(tuple.getNonNullableElement(0));
        
        if (tuple.isElementNull(1)) {
            this.recipient = null;
        } else {
            this.recipient = IdentifierClass.create(tuple.getNonNullableElement(1)).toHostIdentifier();
            if (!Server.hasHost(recipient)) throw new InvalidEncodingException(recipient + " does not run on this server.");
        }
        
        final @Nullable Block key = tuple.getNullableElement(2);
        this.initializationVector = InitializationVector.FACTORY.decodeNullable(tuple.getNullableElement(3));
        if (recipient == null) {
            // Encrypted for clients.
            if (key == null) {
                if (symmetricKey == null) throw new InvalidEncodingException("A symmetric key is needed in order to decrypt the response.");
                this.symmetricKey = symmetricKey;
            } else {
                this.symmetricKey = null;
            }
        } else {
            // Encrypted for hosts.
            if (key != null) {
                final @Nonnull PrivateKey privateKey = Server.getHost(recipient).getPrivateKeyChain().getKey(time);
                this.symmetricKey = decrypt(privateKey, key);
            } else {
                this.symmetricKey = null;
            }
        }
        
        final @Nullable Block element = tuple.getNullableElement(4);
        if (element != null) {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
            final @Nullable SymmetricKey sk = this.symmetricKey;
            final @Nullable InitializationVector iv = this.initializationVector;
            if (sk != null) {
                final @Nonnull Time start = new Time();
                if (iv == null) throw new InvalidEncodingException("The initialization vector may not be null for decryption.");
                this.element = element.decrypt(parameter, sk, iv);
                Log.verbose("Element with " + element.getLength() + " bytes decrypted in " + start.ago().getValue() + " ms.");
            } else {
                this.element = element.setType(parameter);
            }
        } else {
            this.element = null;
        }
        
        this.publicKey = null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final Factory FACTORY = new Factory(SEMANTIC);
    
    /**
     * Compresses the given element into a new non-nullable block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to compress into the new block.
     * 
     * @return a new non-nullable block containing the given element.
     */
    @Pure
    public static @Nonnull @NonEncoding <V extends Storable<V>> Block compressNonNullable(@Nonnull @Loaded @BasedOn("compression@core.digitalid.net") SemanticType type, @Nonnull V element) {
        return FACTORY.encodeNonNullable(new CompressionWrapper(type, Block.fromNonNullable(element)));
    }
    
    /**
     * Compresses the given element into a new nullable block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to compress into the new block.
     * 
     * @return a new nullable block containing the given element.
     */
    @Pure
    public static @Nullable @NonEncoding <V extends Storable<V>> Block compressNullable(@Nonnull @Loaded @BasedOn("compression@core.digitalid.net") SemanticType type, @Nullable V element) {
        return element == null ? null : compressNonNullable(type, element);
    }
    
    /**
     * Decompresses the given non-nullable block. 
     * 
     * @param block the block to be decompressed.
     * 
     * @return the element contained in the given block.
     */
    @Pure
    @NonCommitting
    public static @Nonnull @NonEncoding Block decompressNonNullable(@Nonnull @NonEncoding @BasedOn("compression@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).element;
    }
    
    /**
     * Decompresses the given nullable block. 
     * 
     * @param block the block to be decompressed.
     * 
     * @return the element contained in the given block.
     */
    @Pure
    @NonCommitting
    public static @Nullable @NonEncoding Block decompressNullable(@Nullable @NonEncoding @BasedOn("compression@core.digitalid.net") Block block) throws InvalidEncodingException {
        return block == null ? null : decompressNonNullable(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encryption of the element.
     * 
     * @invariant cache.getType().equals(IMPLEMENTATION) : "The cache is of the implementation type.";
     */
    private @Nullable Block cache;
    
    /**
     * Returns the cached encryption of the element.
     * 
     * @return the cached encryption of the element.
     */
    @Pure
    private @Nonnull Block getCache() {
        if (cache == null) {
            @Nonnull FreezableArray<Block> elements = new FreezableArray<>(5);
            elements.set(0, time.toBlock());
            elements.set(1, Block.toBlock(RECIPIENT, recipient));
            
            if (recipient == null) {
                // Encrypt by hosts for clients.
                elements.set(2, isEncrypted() ? null : new IntegerWrapper(KEY, BigInteger.ZERO).toBlock());
            } else {
                // Encrypt for hosts.
                if (symmetricKey != null) {
                    assert publicKey != null : "The public key is not null because this method is only called for encoding a block.";
                    elements.set(2, encrypt(publicKey, symmetricKey));
                }
            }
            
            elements.set(3, Block.toBlock(initializationVector));
            
            if (symmetricKey == null || initializationVector == null) {
                elements.set(4, element);
            } else {
                final @Nonnull Time start = new Time();
                elements.set(4, element.encrypt(SemanticType.UNKNOWN, symmetricKey, initializationVector));
                Log.verbose("Element with " + element.getLength() + " bytes encrypted in " + start.ago().getValue() + " ms.");
            }
            
            cache = new TupleWrapper(IMPLEMENTATION, elements.freeze()).toBlock();
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
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        getCache().writeTo(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    private static class Factory extends BlockWrapper.Factory<EncryptionWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("encryption@core.digitalid.net") SemanticType type) {
            super(type);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        public @Nonnull EncryptionWrapper decodeNonNullable(@Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
            try {
                final @Nonnull Time start = new Time();
                final @Nonnull ByteArrayOutputStream uncompressed = new ByteArrayOutputStream(2 * block.getLength());
                block.writeTo(new InflaterOutputStream(uncompressed), true);
                final @Nonnull @Encoded Block element = Block.get(parameter, uncompressed.toByteArray());
                Log.verbose("Element with " + element.getLength() + " bytes decompressed in " + start.ago().getValue() + " ms.");
                return new EncryptionWrapper(block.getType(), element);
            } catch (@Nonnull IOException exception) {
                throw new InvalidEncodingException("The given block could not be decompressed.", exception);
            }
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull BlockWrapper.Factory<EncryptionWrapper> getFactory() {
        return new Factory(getSemanticType());
    }
    
}
