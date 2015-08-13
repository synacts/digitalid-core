package net.digitalid.core.wrappers;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.AgentModule;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.client.Commitment;
import net.digitalid.core.client.SecretCommitment;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.cryptography.Parameters;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.external.InvalidSignatureException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.io.Log;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.synchronizer.Audit;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code signature@core.digitalid.net} that is signed by a client.
 * <p>
 * Format: {@code (commitment, t, s)}
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class ClientSignatureWrapper extends SignatureWrapper {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code hash.client.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType HASH = SemanticType.map("hash.client.signature@core.digitalid.net").load(HashWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code client.signature@core.digitalid.net}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.map("client.signature@core.digitalid.net").load(TupleWrapper.TYPE, Commitment.TYPE, HASH, Exponent.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Commitment –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the commitment of this client signature.
     */
    private final @Nonnull Commitment commitment;
    
    /**
     * Returns the commitment of this client signature.
     * 
     * @return the commitment of this client signature.
     */
    @Pure
    public @Nonnull Commitment getCommitment() {
        return commitment;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new client signature wrapper with the given parameters.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param commitment the commitment containing the client secret.
     * 
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    private ClientSignatureWrapper(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
        super(type, element, subject, audit);
        
        this.commitment = commitment;
    }
    
    /**
     * Creates a new client signature wrapper from the given blocks.
     * (Only to be called by {@link SignatureWrapper#decodeWithoutVerifying(ch.xdf.Block, boolean, net.digitalid.core.entity.Entity)}.)
     * 
     * @param block the block that contains the signed element.
     * @param clientSignature the client signature to be decoded.
     * @param verified whether the signature is already verified.
     */
    @Locked
    @NonCommitting
    ClientSignatureWrapper(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, @NonEncoding @BasedOn("client.signature@core.digitalid.net") @Nonnull Block clientSignature, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        super(block, verified);
        
        assert clientSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(clientSignature).getNonNullableElements(3);
        this.commitment = new Commitment(elements.getNonNullable(0));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the element with a new client signature wrapper and signs it according to the arguments.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param commitment the commitment containing the client secret.
     * 
     * @return a new client signature wrapper with the given arguments.
     * 
     * @require element == null || element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @ensure return.isVerified() : "The returned signature is verified.";
     */
    @Pure
    public static @Nonnull <V extends Storable<V>> ClientSignatureWrapper sign(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable V element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
        return new ClientSignatureWrapper(type, Block.fromNullable(element), subject, audit, commitment);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Checks –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return super.isSignedLike(signature) && commitment.equals(((ClientSignatureWrapper) signature).commitment);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Verifying –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public void verify() throws InvalidEncodingException, InvalidSignatureException {
        assert !isVerified() : "This signature is not verified.";
        
        final @Nonnull Time start = new Time();
        
        if (getNonNullableTime().isLessThan(Time.TROPICAL_YEAR.ago())) throw new InvalidSignatureException("The client signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(getCache());
        final @Nonnull BigInteger hash = tuple.getNonNullableElement(0).getHash();
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(tuple.getNonNullableElement(2)).getNonNullableElements(3);
        final @Nonnull BigInteger t = HashWrapper.decodeNonNullable(elements.getNonNullable(1));
        final @Nonnull Exponent s = new Exponent(elements.getNonNullable(2));
        final @Nonnull BigInteger h = t.xor(hash);
        final @Nonnull Element value = commitment.getPublicKey().getAu().pow(s).multiply(commitment.getValue().pow(h));
        if (!t.equals(value.toBlock().getHash()) || s.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The client signature is invalid.");
        
        Log.verbose("Signature verified in " + start.ago().getValue() + " ms.");
        
        setVerified();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Signing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    void sign(@Nonnull @NonFrozen FreezableArray<Block> elements) {
        final @Nonnull Time start = new Time();
        
        final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(3);
        final @Nonnull SecretCommitment commitment = (SecretCommitment) this.commitment;
        subelements.set(0, commitment.toBlock());
        final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT);
        final @Nonnull BigInteger t = commitment.getPublicKey().getAu().pow(r).toBlock().getHash();
        subelements.set(1, HashWrapper.encodeNonNullable(HASH, t));
        final @Nonnull Exponent h = new Exponent(t.xor(elements.getNonNullable(0).getHash()));
        final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
        subelements.set(2, s.toBlock());
        elements.set(2, TupleWrapper.encode(SIGNATURE, subelements.freeze()));
        
        Log.verbose("Element signed in " + start.ago().getValue() + " ms.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Agent –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public @Nullable ClientAgent getAgent(@Nonnull NonHostEntity entity) throws SQLException {
        return AgentModule.getClientAgent(entity, commitment);
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull ClientAgent getAgentCheckedAndRestricted(@Nonnull NonHostEntity entity, @Nullable PublicKey publicKey) throws PacketException, SQLException {
        if (publicKey != null && !commitment.getPublicKey().equals(publicKey)) throw new PacketException(PacketError.KEYROTATION, "The client has to recommit its secret.");
        final @Nullable ClientAgent agent = AgentModule.getClientAgent(entity, commitment);
        if (agent == null) throw new PacketException(PacketError.AUTHORIZATION, "The element was not signed by an authorized client.");
        agent.checkNotRemoved();
        return agent;
    }
    
}
