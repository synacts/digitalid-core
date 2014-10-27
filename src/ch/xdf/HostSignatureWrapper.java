package ch.xdf;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Cache;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Category;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
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
     * Stores the semantic type {@code host.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("host.signature@virtualid.ch").load(TupleWrapper.TYPE, IdentityClass.IDENTIFIER, Element.TYPE);
    
    
    /**
     * Stores the identifier of the identity that is signing as a host.
     * (Certificates and external actions require that not only host identifiers are allowed here.)
     */
    private final @Nonnull Identifier signer;
    
    /**
     * Stores the public key that is used for verifying the host signature.
     */
    private final @Nonnull PublicKey publicKey;
    
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
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * @require !type.isBasedOn(Certificate.TYPE) || element != null && signer instanceof NonHostIdentifier : "If the signature is a certificate, the element is not null and the signer is a non-host.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull Identifier signer) {
        super(type, element, subject, audit);
        
        assert Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
        assert !type.isBasedOn(Certificate.TYPE) || element != null && signer instanceof NonHostIdentifier : "If the signature is a certificate, the element is not null and the signer is a non-host.";
        
        this.signer = signer;
        try {
            this.publicKey = Server.getHost(signer.getHostIdentifier()).getPublicKeyChain().getKey(getTimeNotNull());
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("There should always be a key for the current time.", exception);
        }
    }
    
    /**
     * Encodes the element into a new block and signs it according to the arguments.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param signer the identifier of the signing identity.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * @require !type.isBasedOn(Certificate.TYPE) || element != null && signer instanceof NonHostIdentifier : "If the signature is a certificate, the element is not null and the signer is a non-host.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull Identifier subject, @Nonnull Identifier signer) {
        this(type, element, subject, null, signer);
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
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * @require !type.isBasedOn(Certificate.TYPE) || element != null && signer instanceof NonHostIdentifier : "If the signature is a certificate, the element is not null and the signer is a non-host.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull Identifier signer) {
        this(type, Block.toBlock(element), subject, audit, signer);
    }
    
    /**
     * Encodes the element into a new block and signs it according to the arguments.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param signer the identifier of the signing identity.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * @require !type.isBasedOn(Certificate.TYPE) || element != null && signer instanceof NonHostIdentifier : "If the signature is a certificate, the element is not null and the signer is a non-host.";
     */
    public HostSignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nonnull Identifier subject, @Nonnull Identifier signer) {
        this(type, element, subject, null, signer);
    }
    
    /**
     * Wraps the given block and decodes the given signature.
     * 
     * @param block the block to be wrapped.
     * @param hostSignature the signature to be decoded.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     * @require hostSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
     */
    HostSignatureWrapper(@Nonnull Block block, @Nonnull Block hostSignature) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
        
        assert hostSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        this.signer = Identifier.create(new TupleWrapper(hostSignature).getElementNotNull(0));
        this.publicKey = Cache.getPublicKey(signer.getHostIdentifier(), getTimeNotNull());
        
        if (getType().isBasedOn(Certificate.TYPE)) {
            if (getElement() == null) throw new InvalidEncodingException("If this signature is a certificate, the element may not be null.");
            if (getSigner() instanceof HostIdentifier) throw new InvalidEncodingException("If this signature is a certificate, the signer may not be a host.");
        }
    }
    
    /**
     * Returns the identifier of the identity that is signing as a host.
     * 
     * @return the identifier of the identity that is signing as a host.
     */
    @Pure
    public @Nonnull Identifier getSigner() {
        return signer;
    }
    
    /**
     * Returns whether this host signature is a certificate.
     * 
     * @return whether this host signature is a certificate.
     * 
     * @ensure !return || getElement() != null && getSigner() instanceof NonHostIdentifier : "If this is a certificate, the element is not null and the signer is a non-host.";
     */
    @Pure
    public boolean isCertificate() {
        return getType().isBasedOn(Certificate.TYPE);
    }
    
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return super.isSignedLike(signature) && signer.equals(((HostSignatureWrapper) signature).signer);
    }
    
    
    @Pure
    @Override
    public void verify() throws InvalidEncodingException, InvalidSignatureException {
        if (getTimeNotNull().isLessThan(Time.TWO_YEARS.ago())) throw new InvalidSignatureException("The host signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(getCache());
        final @Nonnull BigInteger hash = tuple.getElementNotNull(0).getHash();
        
        final @Nonnull ReadonlyArray<Block> subelements = new TupleWrapper(tuple.getElementNotNull(1)).getElementsNotNull(2);
        if (!publicKey.getCompositeGroup().getElement(subelements.getNotNull(1)).pow(publicKey.getE()).getValue().equals(hash)) throw new InvalidSignatureException("The host signature is not valid.");
    }
    
    @Override
    protected void sign(@Nonnull FreezableArray<Block> elements, @Nonnull BigInteger hash) {
        assert elements.isNotFrozen() : "The elements are not frozen.";
        
        final @Nonnull FreezableArray<Block> subelements = new FreezableArray<Block>(2);
        subelements.set(0, signer.toBlock());
        try {
            final @Nonnull PrivateKey privateKey = Server.getHost(signer.getHostIdentifier()).getPrivateKeyChain().getKey(getTimeNotNull());
            subelements.set(1, privateKey.powD(hash).toBlock());
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("There should always be a key for the current time.", exception);
        }
        elements.set(1, new TupleWrapper(SIGNATURE, subelements.freeze()).toBlock());
    }
    
    
    /**
     * Stores the semantic type {@code delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATION = SemanticType.create("delegation@virtualid.ch").load(NonHostIdentity.IDENTIFIER); // TODO: , PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATIONS = SemanticType.create("list.delegation@virtualid.ch").load(ListWrapper.TYPE, DELEGATION);
    
    /**
     * Stores the semantic type {@code outgoing.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = SemanticType.create("outgoing.list.delegation@virtualid.ch").load(new Category[] {Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Stores the semantic type {@code incoming.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = SemanticType.create("incoming.list.delegation@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Verifies the signature as a certificate and throws an exception if it is not valid.
     * 
     * @require isCertificate() : "This signature is a certificate.";
     */
    @Pure
    public void verifyAsCertificate() throws SQLException, IOException, PacketException, ExternalException {
        assert isCertificate() : "This signature is a certificate.";
        
        verify();
        // TODO: Check that the signer has the corresponding delegation for the given attribute and value.
    }
    
    /**
     * Determines whether the given VID is authorized to certify the given element.
     * 
     * @param identifier the identifier of the certifying VID.
     * @param value the certified value as a selfcontained block.
     * @return {@code true} if the given VID is authorized to certify the given element, {@code false} otherwise.
     * @require identifier != null : "The identifier is not null.";
     * @require value != null : "The value is not null.";
     */
//    @Deprecated
//    private static boolean isAuthorized(String identifier, Block value) throws Exception {
//        assert identifier != null : "The identifier is not null.";
//        assert value != null : "The value is not null.";
//        
//        long vid = Mapper.getVid(identifier);
//        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
//        
//        if (vid == type) return true;
//        
//        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
//        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
//        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
//        if (attribute == null) return false;
//        
//        List<Block> incoming_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//        for (Block incoming_delegation : incoming_delegations) {
//            Block[] elements = new TupleWrapper(incoming_delegation).getElementsNotNull(3);
//            if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type) {
//                String restriction = new StringWrapper(elements[2]).getString();
//                Expression expression = Expression.parse(restriction);
//                if (expression.matches(value)) {
//                    // Check that the delegating VID references the current VID with the same type and expression.
//                    identifier = new StringWrapper(elements[1]).getString();
//                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
//                    if (attribute == null) continue;
//                    List<Block> outgoing_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//                    for (Block outgoing_delegation : outgoing_delegations) {
//                        elements = new TupleWrapper(outgoing_delegation).getElementsNotNull(3);
//                        if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type && Mapper.getVid(new StringWrapper(elements[1]).getString()) == vid && new StringWrapper(elements[2]).getString().equalsIgnoreCase(restriction)) {
//                            if (isAuthorized(identifier, value)) return true;
//                        }
//                    }
//                }
//            }
//        }
//        
//        return false;
//    }
    
}
