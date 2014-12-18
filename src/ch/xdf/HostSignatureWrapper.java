package ch.xdf;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Cache;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Audit;
import ch.virtualid.server.Server;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} that is signed by a host.
 * <p>
 * Format: {@code (identifier, value)}
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostSignatureWrapper extends SignatureWrapper implements Immutable {
    
    /**
     * Stores the semantic type {@code signer.host.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SIGNER = SemanticType.create("signer.host.signature@virtualid.ch").load(InternalIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code value.host.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType VALUE = SemanticType.create("value.host.signature@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code host.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("host.signature@virtualid.ch").load(TupleWrapper.TYPE, SIGNER, VALUE);
    
    
    /**
     * Stores the identifier of the internal identity that is signing as a host.
     * (Certificates and external actions require that not only host identifiers are allowed here.)
     */
    private final @Nonnull InternalIdentifier signer;
    
    /**
     * Encodes the element into a new block and signs it according to the arguments.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is appended.
     * @param signer the identifier of the signing identity.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull InternalIdentifier signer) {
        super(type, element, subject, audit);
        
        assert Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
        
        this.signer = signer;
    }
    
    /**
     * Encodes the element into a new block and signs it according to the arguments.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is appended.
     * @param signer the identifier of the signing identity.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull InternalIdentifier signer) {
        this(type, Block.toBlock(element), subject, audit, signer);
    }
    
    /**
     * Wraps the given block and decodes the given signature.
     * 
     * @param block the block to be wrapped.
     * @param hostSignature the signature to be decoded.
     * @param verified whether the signature is already verified.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     * @require hostSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
     */
    HostSignatureWrapper(@Nonnull Block block, @Nonnull Block hostSignature, boolean verified) throws InvalidEncodingException {
        super(block, verified);
        
        assert hostSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        this.signer = IdentifierClass.create(new TupleWrapper(hostSignature).getElementNotNull(0)).toInternalIdentifier();
    }
    
    /**
     * Returns the identifier of the identity that is signing as a host.
     * 
     * @return the identifier of the identity that is signing as a host.
     */
    @Pure
    public @Nonnull InternalIdentifier getSigner() {
        return signer;
    }
    
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return super.isSignedLike(signature) && signer.equals(((HostSignatureWrapper) signature).signer);
    }
    
    
    @Pure
    @Override
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        assert isNotVerified() : "This signature is not verified.";
        
        if (getTimeNotNull().isLessThan(Time.TWO_YEARS.ago())) throw new InvalidSignatureException("The host signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(getCache());
        final @Nonnull BigInteger hash = tuple.getElementNotNull(0).getHash();
        
        final @Nonnull PublicKey publicKey;
        if (signer.getHostIdentifier().equals(HostIdentifier.VIRTUALID)) {
            publicKey = new PublicKeyChain(Cache.getStaleAttributeContent(HostIdentity.VIRTUALID, null, PublicKeyChain.TYPE)).getKey(getTimeNotNull());
        } else {
            publicKey = Cache.getPublicKey(signer.getHostIdentifier(), getTimeNotNull());
        }
        
        final @Nonnull ReadonlyArray<Block> subelements = new TupleWrapper(tuple.getElementNotNull(1)).getElementsNotNull(2);
        if (!publicKey.getCompositeGroup().getElement(subelements.getNotNull(1)).pow(publicKey.getE()).getValue().equals(hash)) throw new InvalidSignatureException("The host signature is not valid.");
        
        setVerified();
    }
    
    @Override
    void sign(@Nonnull FreezableArray<Block> elements) {
        assert elements.isNotFrozen() : "The elements are not frozen.";
        assert elements.isNotNull(0) : "The first element is not null.";
        
        final @Nonnull FreezableArray<Block> subelements = new FreezableArray<Block>(2);
        subelements.set(0, signer.toBlock().setType(SIGNER));
        try {
            final @Nonnull PrivateKey privateKey = Server.getHost(signer.getHostIdentifier()).getPrivateKeyChain().getKey(getTimeNotNull());
            subelements.set(1, privateKey.powD(elements.getNotNull(0).getHash()).toBlock().setType(VALUE));
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("There should always be a key for the current time.", exception);
        }
        elements.set(1, new TupleWrapper(SIGNATURE, subelements.freeze()).toBlock());
    }
    
}
