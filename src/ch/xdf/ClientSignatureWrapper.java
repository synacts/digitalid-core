package ch.xdf;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Audit;
import ch.virtualid.server.Server;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} that is signed by a client.
 * <p>
 * Format: {@code (commitment, t, s)}
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class ClientSignatureWrapper extends SignatureWrapper implements Immutable {
    
    /**
     * Stores the semantic type {@code hash.client.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType HASH = SemanticType.create("hash.client.signature@virtualid.ch").load(HashWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code client.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("client.signature@virtualid.ch").load(TupleWrapper.TYPE, Commitment.TYPE, HASH, Exponent.TYPE);
    
    
    /**
     * Stores the commitment of this client signature.
     */
    private final @Nonnull Commitment commitment;
    
    /**
     * Encodes the element into a new block and signs it with the given commitment.
     * 
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param commitment the commitment containing the client secret.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    public ClientSignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
        super(type, element, subject, audit);
        
        this.commitment = commitment;
    }
    
    /**
     * Encodes the element into a new block and signs it according to the argument.
     * 
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param client the commitment containing the client secret.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    public ClientSignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
        this(type, Block.toBlock(element), subject, audit, commitment);
    }
    
    /**
     * Wraps the given block and decodes the given signature.
     * (Only to be called by {@link SignatureWrapper#decodeUnverified(ch.xdf.Block)}.)
     * 
     * @param block the block to be wrapped.
     * @param clientSignature the signature to be decoded.
     */
    ClientSignatureWrapper(@Nonnull Block block, @Nonnull Block clientSignature) throws InvalidEncodingException, FailedIdentityException {
        super(block, true);
        
        @Nullable Identifier subject = getSubject();
        assert subject != null : "The subject of signed statements is never null.";
        publicKey = new PublicKey(Client.getAttributeNotNullUnwrapped(subject.getHostIdentifier().getIdentity(), SemanticType.HOST_PUBLIC_KEY));
        
        @Nonnull Block[] elements = new TupleWrapper(clientSignature).getElementsNotNull(3);
        commitment = new Commitment(elements[0]);
    }
    
    
    /**
     * Returns the commitment containing either the client secret or the actual value.
     * 
     * @return the commitment containing either the client secret or the actual value.
     */
    @Pure
    public @Nonnull Commitment getCommitment() {
        return commitment;
    }
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return super.isSignedLike(signature) && commitment.equals(((ClientSignatureWrapper) signature).commitment);
    }
    
    @Pure
    @Override
    public void verify() throws InvalidEncodingException, InvalidSignatureException {
        if (System.currentTimeMillis() - getTime() > Server.YEAR) throw new InvalidSignatureException("The client signature is out of date.");
        
        @Nonnull Block[] elements = new TupleWrapper(getCache()).getElementsNotNull(4);
        @Nonnull BigInteger hash = elements[0].getHash();
        
        @Nonnull Block[] subelements = new TupleWrapper(elements[2]).getElementsNotNull(3);
        @Nonnull BigInteger t = new IntegerWrapper(subelements[1]).getValue();
        @Nonnull BigInteger s = new IntegerWrapper(subelements[2]).getValue();
        @Nonnull BigInteger h = t.xor(hash);
        @Nonnull Element f = publicKey.getCompositeGroup().getElement(commitment.getValue());
        @Nonnull BigInteger value = publicKey.getAu().pow(s).multiply(f.pow(h)).getValue();
        if (!t.equals(new IntegerWrapper(value).toBlock().getHash()) || s.bitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The client signature is not valid.");
    }
    
    @Override
    protected void sign(@Nonnull Block[] elements, @Nonnull BigInteger hash) {
        @Nonnull Block[] subelements = new Block[3];
        @Nonnull Exponent u = new Exponent(commitment.getValue());
        @Nonnull Element f = publicKey.getAu().pow(u);
        subelements[0] = f.toBlock();
        @Nonnull Exponent r = publicKey.getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT);
        @Nonnull BigInteger t = new IntegerWrapper(publicKey.getAu().pow(r).getValue()).toBlock().getHash();
        subelements[1] = new IntegerWrapper(t).toBlock();
        @Nonnull Exponent h = new Exponent(t.xor(hash));
        @Nonnull Exponent s = r.subtract(u.multiply(h));
        subelements[2] = s.toBlock();
        elements[2] = new TupleWrapper(subelements).toBlock();
    }
    
}
