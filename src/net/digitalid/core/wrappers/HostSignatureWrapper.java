package net.digitalid.core.wrappers;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
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
import net.digitalid.core.io.Log;
import net.digitalid.core.server.Server;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.synchronizer.Audit;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code signature@core.digitalid.net} that is signed by a host.
 * <p>
 * Format: {@code (identifier, value)}
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class HostSignatureWrapper extends SignatureWrapper {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code signer.host.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SIGNER = SemanticType.map("signer.host.signature@core.digitalid.net").load(InternalIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code value.host.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE = SemanticType.map("value.host.signature@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code host.signature@core.digitalid.net}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.map("host.signature@core.digitalid.net").load(TupleWrapper.TYPE, SIGNER, VALUE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Signer –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the identifier of the internal identity that is signing as a host.
     * (Certificates and external actions require that not only host identifiers are allowed here.)
     */
    private final @Nonnull InternalIdentifier signer;
    
    /**
     * Returns the identifier of the identity that is signing as a host.
     * 
     * @return the identifier of the identity that is signing as a host.
     */
    @Pure
    public @Nonnull InternalIdentifier getSigner() {
        return signer;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host signature wrapper with the given parameters.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is appended.
     * @param signer the identifier of the signing identity.
     * 
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    private HostSignatureWrapper(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull InternalIdentifier signer) {
        super(type, element, subject, audit);
        
        assert Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
        
        this.signer = signer;
    }
    
    /**
     * Creates a new host signature wrapper from the given blocks.
     * (Only to be called by {@link SignatureWrapper#decodeWithoutVerifying(ch.xdf.Block, boolean, net.digitalid.core.entity.Entity)}.)
     * 
     * @param block the block that contains the signed element.
     * @param hostSignature the host signature to be decoded.
     * @param verified whether the signature is already verified.
     */
    HostSignatureWrapper(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, @Nonnull @NonEncoding @BasedOn("host.signature@core.digitalid.net") Block hostSignature, boolean verified) throws InvalidEncodingException {
        super(block, verified);
        
        assert hostSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        this.signer = IdentifierClass.create(TupleWrapper.decode(hostSignature).getNonNullableElement(0)).toInternalIdentifier();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the element with a new host signature wrapper and signs it according to the arguments.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is appended.
     * @param signer the identifier of the signing identity.
     * 
     * @return a new host signature wrapper with the given arguments.
     * 
     * @require element == null || element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure return.isVerified() : "The returned signature is verified.";
     */
    @Pure
    public static @Nonnull <V extends Storable<V>> HostSignatureWrapper sign(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable V element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull InternalIdentifier signer) {
        return new HostSignatureWrapper(type, Block.fromNullable(element), subject, audit, signer);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Checks –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return super.isSignedLike(signature) && signer.equals(((HostSignatureWrapper) signature).signer);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Verifying –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        assert !isVerified() : "This signature is not verified.";
        
        final @Nonnull Time start = new Time();
        
        if (getNonNullableTime().isLessThan(Time.TWO_YEARS.ago())) throw new InvalidSignatureException("The host signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(getCache());
        final @Nonnull BigInteger hash = tuple.getNonNullableElement(0).getHash();
        
        final @Nonnull PublicKey publicKey;
        if (signer.getHostIdentifier().equals(HostIdentifier.DIGITALID)) {
            publicKey = new PublicKeyChain(Cache.getStaleAttributeContent(HostIdentity.DIGITALID, null, PublicKeyChain.TYPE)).getKey(getNonNullableTime());
        } else {
            publicKey = Cache.getPublicKey(signer.getHostIdentifier(), getNonNullableTime());
        }
        
        final @Nonnull ReadOnlyArray<Block> subelements = TupleWrapper.decode(tuple.getNonNullableElement(1)).getNonNullableElements(2);
        if (!publicKey.getCompositeGroup().getElement(subelements.getNonNullable(1)).pow(publicKey.getE()).getValue().equals(hash)) throw new InvalidSignatureException("The host signature is not valid.");
        
        Log.verbose("Signature verified in " + start.ago().getValue() + " ms.");
        
        setVerified();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Signing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    void sign(@Nonnull @NullableElements @NonFrozen FreezableArray<Block> elements) {
        final @Nonnull Time start = new Time();
        
        final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(2);
        subelements.set(0, signer.toBlock().setType(SIGNER));
        try {
            final @Nonnull PrivateKey privateKey = Server.getHost(signer.getHostIdentifier()).getPrivateKeyChain().getKey(getNonNullableTime());
            subelements.set(1, privateKey.powD(elements.getNonNullable(0).getHash()).toBlock().setType(VALUE));
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("There should always be a key for the current time.", exception);
        }
        elements.set(1, TupleWrapper.encode(SIGNATURE, subelements.freeze()));
        
        Log.verbose("Element signed in " + start.ago().getValue() + " ms.");
    }
    
}
