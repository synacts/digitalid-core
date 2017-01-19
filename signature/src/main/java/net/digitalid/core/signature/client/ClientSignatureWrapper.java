package net.digitalid.core.signature.client;

//package net.digitalid.core.conversion.wrappers.signature;
//
//import java.math.BigInteger;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.collections.freezable.FreezableArray;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.exceptions.InternalException;
//import net.digitalid.utility.freezable.NonFrozen;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.system.logger.Log;
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.Locked;
//import net.digitalid.database.annotations.transaction.NonCommitting;
//import net.digitalid.database.core.exceptions.DatabaseException;
//
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.annotations.NonEncoding;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.conversion.wrappers.value.binary.Binary256Wrapper;
//import net.digitalid.core.cryptography.signature.exceptions.ExpiredClientSignatureException;
//import net.digitalid.core.cryptography.signature.exceptions.InvalidClientSignatureException;
//import net.digitalid.core.packet.exceptions.NetworkException;
//import net.digitalid.core.packet.exceptions.RequestErrorCode;
//import net.digitalid.core.packet.exceptions.RequestException;
//import net.digitalid.core.synchronizer.Audit;
//
//import net.digitalid.service.core.auxiliary.Time;
//import net.digitalid.service.core.concepts.agent.AgentModule;
//import net.digitalid.service.core.concepts.agent.ClientAgent;
//import net.digitalid.service.core.converter.xdf.Encode;
//import net.digitalid.service.core.converter.xdf.XDF;
//import net.digitalid.service.core.cryptography.Element;
//import net.digitalid.service.core.cryptography.Exponent;
//import net.digitalid.service.core.cryptography.Parameters;
//import net.digitalid.service.core.cryptography.PublicKey;
//import net.digitalid.service.core.entity.NonHostEntity;
//import net.digitalid.service.core.identifier.InternalIdentifier;
//import net.digitalid.service.core.identity.SemanticType;
//import net.digitalid.service.core.identity.annotations.BasedOn;
//import net.digitalid.service.core.identity.annotations.Loaded;
//import net.digitalid.service.core.site.client.Commitment;
//import net.digitalid.service.core.site.client.SecretCommitment;
//
///**
// * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code signature@core.digitalid.net} that is signed by a client.
// * <p>
// * Format: {@code (commitment, t, s)}
// */
//@Immutable
//public final class ClientSignatureWrapper extends SignatureWrapper {
//    
//    /* -------------------------------------------------- Implementation -------------------------------------------------- */
//    
//    /**
//     * Stores the semantic type {@code hash.client.signature@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType HASH = SemanticType.map("hash.client.signature@core.digitalid.net").load(Binary256Wrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code client.signature@core.digitalid.net}.
//     */
//    static final @Nonnull SemanticType SIGNATURE = SemanticType.map("client.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Commitment.TYPE, HASH, Exponent.TYPE);
//    
//    /* -------------------------------------------------- Commitment -------------------------------------------------- */
//    
//    /**
//     * Stores the commitment of this client signature.
//     */
//    private final @Nonnull Commitment commitment;
//    
//    /**
//     * Returns the commitment of this client signature.
//     * 
//     * @return the commitment of this client signature.
//     */
//    @Pure
//    public @Nonnull Commitment getCommitment() {
//        return commitment;
//    }
//    
//    /* -------------------------------------------------- Constructors -------------------------------------------------- */
//    
//    /**
//     * Creates a new client signature wrapper with the given parameters.
//     * 
//     * @param type the semantic type of the new signature wrapper.
//     * @param element the element of the new signature wrapper.
//     * @param subject the identifier of the identity about which a statement is made.
//     * @param audit the audit or null if no audit shall be appended.
//     * @param commitment the commitment containing the client secret.
//     * 
//     * @require element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
//     * 
//     * @ensure isVerified() : "This signature is verified.";
//     */
//    private ClientSignatureWrapper(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
//        super(type, element, subject, audit);
//        
//        this.commitment = commitment;
//    }
//    
//    /**
//     * Creates a new client signature wrapper from the given blocks.
//     * (Only to be called by {@link SignatureWrapper#decodeWithoutVerifying(ch.xdf.Block, boolean, net.digitalid.service.core.entity.Entity)}.)
//     * 
//     * @param block the block that contains the signed element.
//     * @param clientSignature the client signature to be decoded.
//     * @param verified whether the signature is already verified.
//     */
//    @Locked
//    @NonCommitting
//    ClientSignatureWrapper(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, @Nonnull @NonEncoding @BasedOn("client.signature@core.digitalid.net") Block clientSignature, boolean verified) throws ExternalException {
//        super(block, verified);
//        
//        Require.that(clientSignature.getType().isBasedOn(SIGNATURE)).orThrow("The signature is based on the implementation type.");
//        
//        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(clientSignature).getNonNullableElements(3);
//        this.commitment = new Commitment(elements.getNonNullable(0));
//    }
//    
//    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
//    
//    /**
//     * Encodes the element with a new client signature wrapper and signs it according to the arguments.
//     * 
//     * @param type the semantic type of the new signature wrapper.
//     * @param element the element of the new signature wrapper.
//     * @param subject the identifier of the identity about which a statement is made.
//     * @param audit the audit or null if no audit shall be appended.
//     * @param commitment the commitment containing the client secret.
//     * 
//     * @return a new client signature wrapper with the given arguments.
//     * 
//     * @require element == null || element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
//     * 
//     * @ensure return.isVerified() : "The returned signature is verified.";
//     */
//    @Pure
//    public static @Nonnull <V extends XDF<V, ?>> ClientSignatureWrapper sign(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable V element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull SecretCommitment commitment) {
//        return new ClientSignatureWrapper(type, Encode.nullable(element), subject, audit, commitment);
//    }
//    
//    /* -------------------------------------------------- Checks -------------------------------------------------- */
//    
//    @Pure
//    @Override
//    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
//        return super.isSignedLike(signature) && commitment.equals(((ClientSignatureWrapper) signature).commitment);
//    }
//    
//    /* -------------------------------------------------- Verifying -------------------------------------------------- */
//    
//    @Pure
//    @Override
//    public void verify() throws ExternalException {
//        Require.that(!isVerified()).orThrow("This signature is not verified.");
//        
//        final @Nonnull Time start = Time.getCurrent();
//        
//        if (getNonNullableTime().isLessThan(Time.TROPICAL_YEAR.ago())) { throw ExpiredClientSignatureException.get(this); }
//        
//        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(getCache());
//        final @Nonnull BigInteger hash = tuple.getNonNullableElement(0).getHash();
//        
//        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(tuple.getNonNullableElement(2)).getNonNullableElements(3);
//        final @Nonnull BigInteger t = Binary256Wrapper.decodeNonNullable(elements.getNonNullable(1));
//        final @Nonnull Exponent s = Exponent.get(elements.getNonNullable(2));
//        final @Nonnull BigInteger h = t.xor(hash);
//        final @Nonnull Element value = commitment.getPublicKey().getAu().pow(s).multiply(commitment.getValue().pow(h));
//        if (!t.equals(Encode.nonNullable(value).getHash()) || s.getBitLength() > Parameters.RANDOM_EXPONENT) { throw InvalidClientSignatureException.get(this); }
//        
//        Log.verbose("Signature verified in " + start.ago().getValue() + " ms.");
//        
//        setVerified();
//    }
//    
//    /* -------------------------------------------------- Signing -------------------------------------------------- */
//    
//    @Override
//    void sign(@Nonnull @NonFrozen FreezableArray<Block> elements) {
//        final @Nonnull Time start = Time.getCurrent();
//        
//        final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(3);
//        final @Nonnull SecretCommitment commitment = (SecretCommitment) this.commitment;
//        subelements.set(0, commitment.toBlock());
//        final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT);
//        final @Nonnull BigInteger t = Encode.nonNullable(commitment.getPublicKey().getAu().pow(r)).getHash();
//        subelements.set(1, Binary256Wrapper.encodeNonNullable(HASH, t));
//        final @Nonnull Exponent h = Exponent.get(t.xor(elements.getNonNullable(0).getHash()));
//        final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
//        subelements.set(2, Encode.nonNullable(s));
//        elements.set(2, TupleWrapper.encode(SIGNATURE, subelements.freeze()));
//        
//        Log.verbose("Element signed in " + start.ago().getValue() + " ms.");
//    }
//    
//    /* -------------------------------------------------- Agent -------------------------------------------------- */
//    
//    @Pure
//    @Locked
//    @Override
//    @NonCommitting
//    public @Nullable ClientAgent getAgent(@Nonnull NonHostEntity entity) throws DatabaseException {
//        return AgentModule.getClientAgent(entity, commitment);
//    }
//    
//    @Pure
//    @Locked
//    @Override
//    @NonCommitting
//    public @Nonnull ClientAgent getAgentCheckedAndRestricted(@Nonnull NonHostEntity entity, @Nullable PublicKey publicKey) throws RequestException, DatabaseException {
//        if (publicKey != null && !commitment.getPublicKey().equals(publicKey)) { throw RequestException.get(RequestErrorCode.KEYROTATION, "The client has to recommit its secret."); }
//        final @Nullable ClientAgent agent = AgentModule.getClientAgent(entity, commitment);
//        if (agent == null) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The element was not signed by an authorized client."); }
//        agent.checkNotRemoved();
//        return agent;
//    }
//    
//}
