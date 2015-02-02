package ch.xdf;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cache.Cache;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.InitializationVector;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.server.Server;
import ch.virtualid.util.FreezableArray;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * Wraps a block with the syntactic type {@code encryption@xdf.ch} for encoding and decoding.
 * The structure of encrypted blocks is a tuple that consists of the time of the encryption,
 * the receiving host's identifier, the encrypted key and the possibly encrypted element.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class EncryptionWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code encryption@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("encryption@xdf.ch").load(1);
    
    /**
     * Stores the semantic type {@code recipient.encryption@virtualid.ch}.
     */
    public static final @Nonnull SemanticType RECIPIENT = SemanticType.create("recipient.encryption@virtualid.ch").load(HostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code key.encryption@virtualid.ch}.
     */
    private static final @Nonnull SemanticType KEY = SemanticType.create("key.encryption@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code encryption@virtualid.ch}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.create("encryption@virtualid.ch").load(TupleWrapper.TYPE, Time.TYPE, RECIPIENT, KEY, InitializationVector.TYPE, SemanticType.UNKNOWN);
    
    
    /**
     * Caches the encrypted key for a given pair of public key and symmetric key.
     */
    private static final @Nonnull Map<Pair<PublicKey, SymmetricKey>, Block> encryptions = new ConcurrentHashMap<Pair<PublicKey, SymmetricKey>, Block>();
    
    /**
     * Encrypts the given symmetric key for the given public key.
     * 
     * @param publicKey the public key for which the given symmetric key is to be encrypted.
     * @param symmetricKey the symmetric key which is to be encrypted for the given public key.
     * 
     * @return a block containing the given symmetric key encrypted for the given public key.
     */
    private static @Nonnull Block encrypt(@Nonnull PublicKey publicKey, @Nonnull SymmetricKey symmetricKey) {
        final @Nonnull Pair<PublicKey, SymmetricKey> pair = new Pair<PublicKey, SymmetricKey>(publicKey, symmetricKey);
        @Nullable Block key = encryptions.get(pair);
        if (key == null) {
            key = publicKey.getCompositeGroup().getElement(symmetricKey.getValue()).pow(publicKey.getE()).toBlock().setType(KEY);
            encryptions.put(pair, key);
        }
        return key;
    }
    
    /**
     * Caches the symmetric key for a given pair of private key and encrypted key.
     */
    private static final @Nonnull Map<Pair<PrivateKey, Block>, SymmetricKey> decryptions = new ConcurrentHashMap<Pair<PrivateKey, Block>, SymmetricKey>();
    
    /**
     * Decrypts the given key with the given private key.
     * 
     * @param privateKey the private key for which the given key is encrypted.
     * @param key the block containing the encrypted key for the given private key.
     * 
     * @return the symmetric key with the decrypted value of the given encrypted key.
     */
    private static @Nonnull SymmetricKey decrypt(@Nonnull PrivateKey privateKey, @Nonnull Block key) throws InvalidEncodingException {
        final @Nonnull Pair<PrivateKey, Block> pair = new Pair<PrivateKey, Block>(privateKey, key);
        @Nullable SymmetricKey symmetricKey = decryptions.get(pair);
        if (symmetricKey == null) {
            final @Nonnull BigInteger value = new IntegerWrapper(key).getValue();
            symmetricKey = new SymmetricKey(privateKey.powD(value).getValue());
            decryptions.put(pair, symmetricKey);
        }
        return symmetricKey;
    }
    
    
    /**
     * Stores the time of encryption.
     */
    private final @Nonnull Time time;
    
    /**
     * Stores the element of this wrapper.
     * 
     * @invariant element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the block's type.";
     */
    private final @Nullable Block element;
    
    /**
     * Stores the identifier of the host for which the element is encrypted or null if the recipient is not known.
     */
    private final @Nullable HostIdentifier recipient;
    
    /**
     * Stores the symmetric key that is used for the encryption of the element or null if no encryption is used.
     */
    private final @Nullable SymmetricKey symmetricKey;
    
    /**
     * Stores the public key that is used for the encryption of the symmetric key or null if the block is decoded.
     */
    private final @Nullable PublicKey publicKey;
    
    /**
     * Stores the initialization vector that is used for the encryption of the element or null if no encryption is used.
     */
    private final @Nullable InitializationVector initializationVector;
    
    /**
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param recipient the identifier of the host for which the element is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key that is used for the encryption of the element or null if no encryption is used.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    @DoesNotCommit
    public EncryptionWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        super(type);
        
        assert element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
        
        this.time = new Time();
        this.element = element;
        this.recipient = recipient;
        this.symmetricKey = symmetricKey;
        this.publicKey = (recipient == null || symmetricKey == null) ? null : Cache.getPublicKey(recipient, time);
        this.initializationVector = (element == null || symmetricKey == null) ? null : new InitializationVector();
    }
    
    /**
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into this new block.
     * @param recipient the identifier of the host for which the element is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key that is used for the encryption of the element or null if no encryption is used.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    @DoesNotCommit
    public EncryptionWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        this(type, Block.toBlock(element), recipient, symmetricKey);
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to be wrapped and decoded.
     * @param symmetricKey the symmetric key used for decryption or null if the element is encrypted for a host or not at all.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public EncryptionWrapper(@Nonnull Block block, @Nullable SymmetricKey symmetricKey) throws InvalidEncodingException {
        super(block);
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(new Block(IMPLEMENTATION, block));
        this.time = new Time(tuple.getElementNotNull(0));
        
        if (tuple.isElementNull(1)) {
            this.recipient = null;
        } else {
            this.recipient = IdentifierClass.create(tuple.getElementNotNull(1)).toHostIdentifier();
            if (!Server.hasHost(recipient)) throw new InvalidEncodingException(recipient + " does not run on this server.");
        }
        
        final @Nullable Block key = tuple.getElement(2);
        this.initializationVector = tuple.isElementNull(3) ? null : new InitializationVector(tuple.getElementNotNull(3));
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
        
        final @Nullable Block element = tuple.getElement(4);
        if (element != null) {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNotNull(0);
            final @Nullable SymmetricKey sk = this.symmetricKey;
            final @Nullable InitializationVector iv = this.initializationVector;
            if (sk != null) {
                if (iv == null) throw new InvalidEncodingException("The initialization vector may not be null for decryption.");
                this.element = element.decrypt(parameter, sk, iv);
            } else {
                this.element = element.setType(parameter);
            }
        } else {
            this.element = null;
        }
        
        this.publicKey = null;
    }
    
    
    /**
     * Returns the time of encryption.
     * 
     * @return the time of encryption.
     */
    @Pure
    public @Nonnull Time getTime() {
        return time;
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @ensure element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the block's type.";
     */
    @Pure
    public @Nullable Block getElement() {
        return element;
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @throws InvalidEncodingException if the element is null.
     * 
     * @ensure element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is based on the parameter of the block's type.";
     */
    @Pure
    public @Nonnull Block getElementNotNull() throws InvalidEncodingException {
        if (element == null) throw new InvalidEncodingException("The compressed element is null.");
        return element;
    }
    
    /**
     * Returns the identity of the host for which the element is encrypted or null if the recipient is not known.
     * 
     * @return the identity of the host for which the element is encrypted or null if the recipient is not known.
     */
    @Pure
    public @Nullable HostIdentifier getRecipient() {
        return recipient;
    }
    
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
     * Returns the initialization vector that is used for the encryption of the element or null if no encryption is used.
     * 
     * @return the initialization vector that is used for the encryption of the element or null if no encryption is used.
     */
    @Pure
    public @Nullable InitializationVector getInitializationVector() {
        return initializationVector;
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
            @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(5);
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
            
            if (element != null) {
                elements.set(4, symmetricKey == null || initializationVector == null ? element : element.encrypt(SemanticType.UNKNOWN, symmetricKey, initializationVector));
            }
            
            cache = new TupleWrapper(IMPLEMENTATION, elements.freeze()).toBlock();
        }
        return cache;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return getCache().getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        getCache().writeTo(block);
    }
    
}
