package ch.xdf;

import ch.virtualid.agent.ClientAgent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Commitment;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.packet.Audit;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} that is signed by a client.
 * <p>
 * Format: {@code (commitment, t, s)}
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
     * @param type the semantic type of the new block.
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
     * @param type the semantic type of the new block.
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
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     * @require clientSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
     */
    ClientSignatureWrapper(@Nonnull Block block, @Nonnull Block clientSignature) throws SQLException, IOException, PacketException, ExternalException {
        super(block, true);
        
        assert clientSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(clientSignature).getElementsNotNull(3);
        this.commitment = new Commitment(elements.getNotNull(0));
    }
    
    
    /**
     * Returns the commitment of this client signature.
     * 
     * @return the commitment of this client signature.
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
        if (new Time().subtract(getTimeNotNull()).isGreaterThan(Time.TROPICAL_YEAR)) throw new InvalidSignatureException("The client signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(getCache());
        final @Nonnull BigInteger hash = tuple.getElementNotNull(0).getHash();
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(tuple.getElementNotNull(2)).getElementsNotNull(3);
        final @Nonnull BigInteger t = new HashWrapper(elements.getNotNull(1)).getValue();
        final @Nonnull Exponent s = new Exponent(elements.getNotNull(2));
        final @Nonnull BigInteger h = t.xor(hash);
        final @Nonnull Element value = commitment.getPublicKey().getAu().pow(s).multiply(commitment.getValue().pow(h));
        if (!t.equals(value.toBlock().getHash()) || s.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The client signature is invalid.");
    }
    
    @Override
    protected void sign(@Nonnull FreezableArray<Block> elements, @Nonnull BigInteger hash) {
        final @Nonnull FreezableArray<Block> subelements = new FreezableArray<Block>(3);
        final @Nonnull SecretCommitment commitment = (SecretCommitment) this.commitment;
        subelements.set(0, commitment.toBlock());
        final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT);
        final @Nonnull BigInteger t = commitment.getPublicKey().getAu().pow(r).toBlock().getHash();
        subelements.set(1, new HashWrapper(HASH, t).toBlock());
        final @Nonnull Exponent h = new Exponent(t.xor(hash));
        final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
        subelements.set(2, s.toBlock());
        elements.set(2, new TupleWrapper(SIGNATURE, subelements.freeze()).toBlock());
    }
    
    
    @Pure
    @Override
    public @Nullable ClientAgent getAgent(@Nonnull Entity entity) throws SQLException {
        return Agents.getClientAgent(entity, getCommitment());
    }
    
    @Pure
    @Override
    public @Nonnull ClientAgent getAgentCheckedAndRestricted(@Nonnull Entity entity) throws PacketException, SQLException {
        final @Nullable ClientAgent agent = Agents.getClientAgent(entity, getCommitment());
        if (agent == null) throw new PacketException(PacketError.AUTHORIZATION, "The element was not signed by an authorized client.");
        agent.checkNotRemoved();
        return agent;
    }
    
}
