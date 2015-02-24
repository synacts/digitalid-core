package net.digitalid.core.wrappers;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadonlyArray;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.PrivateKey;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.cryptography.PublicKeyChain;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.external.InvalidSignatureException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.server.Server;
import net.digitalid.core.synchronizer.Audit;

/**
 * Wraps a block with the syntactic type {@code signature@core.digitalid.net} that is signed by a host.
 * <p>
 * Format: {@code (identifier, value)}
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class HostSignatureWrapper extends SignatureWrapper implements Immutable {
    
    /**
     * Stores the semantic type {@code signer.host.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SIGNER = SemanticType.create("signer.host.signature@core.digitalid.net").load(InternalIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code value.host.signature@core.digitalid.net}.
     */
    static final @Nonnull SemanticType VALUE = SemanticType.create("value.host.signature@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code host.signature@core.digitalid.net}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("host.signature@core.digitalid.net").load(TupleWrapper.TYPE, SIGNER, VALUE);
    
    
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
    @NonCommitting
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        assert isNotVerified() : "This signature is not verified.";
        
        if (getTimeNotNull().isLessThan(Time.TWO_YEARS.ago())) throw new InvalidSignatureException("The host signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(getCache());
        final @Nonnull BigInteger hash = tuple.getElementNotNull(0).getHash();
        
        final @Nonnull PublicKey publicKey;
        if (signer.getHostIdentifier().equals(HostIdentifier.DIGITALID)) {
            publicKey = new PublicKeyChain(Cache.getStaleAttributeContent(HostIdentity.DIGITALID, null, PublicKeyChain.TYPE)).getKey(getTimeNotNull());
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
