package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class stores the groups, elements and exponents of a host's public key.
 * 
 * @invariant verifySubgroupProof() : "The elements au, ai, av and ao are in the subgroup of ab.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PublicKey implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code composite.group.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType COMPOSITE_GROUP = SemanticType.create("composite.group.public.key.host@virtualid.ch").load(Group.TYPE);
    
    /**
     * Stores the semantic type {@code e.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType E = SemanticType.create("e.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code ab.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AB = SemanticType.create("ab.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code au.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AU = SemanticType.create("au.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ai.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AI = SemanticType.create("ai.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code av.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AV = SemanticType.create("av.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ao.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AO = SemanticType.create("ao.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code t.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType T = SemanticType.create("t.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code su.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SU = SemanticType.create("su.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code si.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SI = SemanticType.create("si.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sv.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SV = SemanticType.create("sv.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code so.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SO = SemanticType.create("so.public.key.host@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code square.group.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SQUARE_GROUP = SemanticType.create("square.group.public.key.host@virtualid.ch").load(Group.TYPE);
    
    /**
     * Stores the semantic type {@code g.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType G = SemanticType.create("g.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code y.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType Y = SemanticType.create("y.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code z.public.key.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType Z = SemanticType.create("z.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("public.key.host@virtualid.ch").load(TupleWrapper.TYPE, COMPOSITE_GROUP, E, AB, AU, AI, AV, AO, T, SU, SI, SV, SO, SQUARE_GROUP, G, Y, Z);
    
    
    /**
     * Stores the semantic type {@code tu.public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TU = SemanticType.create("tu.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ti.public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TI = SemanticType.create("ti.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code tv.public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TV = SemanticType.create("tv.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code to.public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TO = SemanticType.create("to.public.key.host@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code tuple.public.key.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TUPLE = SemanticType.create("tuple.public.key.host@virtualid.ch").load(TupleWrapper.TYPE, TU, TI, TV, TO);
    
    
    /**
     * Stores the semantic type {@code w1.verifiable.encryption@virtualid.ch}.
     */
    public static final @Nonnull SemanticType W1 = SemanticType.create("w1.verifiable.encryption@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code w2.verifiable.encryption@virtualid.ch}.
     */
    public static final @Nonnull SemanticType W2 = SemanticType.create("w2.verifiable.encryption@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code verifiable.encryption@virtualid.ch}.
     */
    public static final @Nonnull SemanticType VERIFIABLE_ENCRYPTION = SemanticType.create("verifiable.encryption@virtualid.ch").load(TupleWrapper.TYPE, W1, W2);
    
    
    /**
     * Stores the composite group for encryption and signing.
     * 
     * @invariant compositeGroup.hasNoOrder() : "The order of the composite group is unknown.";
     */
    private final @Nonnull Group compositeGroup;
    
    /**
     * Stores the encryption and verification exponent.
     */
    private final @Nonnull Exponent e;
    
    /**
     * Stores the base for blinding.
     */
    private final @Nonnull Element ab;
    
    /**
     * Stores the base of the client's secret.
     */
    private final @Nonnull Element au;
    
    /**
     * Stores the base of the serial number.
     */
    private final @Nonnull Element ai;
    
    /**
     * Stores the base of the hashed identifier.
     */
    private final @Nonnull Element av;
    
    /**
     * Stores the base of the exposed arguments.
     */
    private final @Nonnull Element ao;
    
    /**
     * Stores the hash of the temporary commitments in the subgroup proof.
     */
    private final @Nonnull Exponent t;
    
    /**
     * Stores the solution for the proof that au is in the subgroup of ab.
     */
    private final @Nonnull Exponent su;
    
    /**
     * Stores the solution for the proof that ai is in the subgroup of ab.
     */
    private final @Nonnull Exponent si;
    
    /**
     * Stores the solution for the proof that av is in the subgroup of ab.
     */
    private final @Nonnull Exponent sv;
    
    /**
     * Stores the solution for the proof that ao is in the subgroup of ab.
     */
    private final @Nonnull Exponent so;
    
    /**
     * Stores the square group for verifiable encryption.
     * 
     * @invariant squareGroup.hasNoOrder() : "The order of the square group is unknown.";
     */
    private final @Nonnull Group squareGroup;
    
    /**
     * Stores the generator of the square group.
     */
    private final @Nonnull Element g;
    
    /**
     * Stores the encryption element of the square group.
     */
    private final @Nonnull Element y;

    /**
     * Stores the encryption base of the square group.
     */
    private final @Nonnull Element zPlus1;

    /**
     * Creates a new public key with the given groups, bases and exponents.
     * 
     * @param compositeGroup the composite group for encryption and signing.
     * @param e the encryption and verification exponent.
     * 
     * @param ab the base for blinding.
     * @param au the base of the client's secret.
     * @param ai the base of the serial number.
     * @param av the base of the hashed identifier.
     * @param ao the base of the exposed arguments.
     * 
     * @param t the hash of the temporary commitments in the subgroup proof.
     * @param su the solution for the proof that au is in the subgroup of ab.
     * @param si the solution for the proof that ai is in the subgroup of ab.
     * @param sv the solution for the proof that av is in the subgroup of ab.
     * @param so the solution for the proof that ao is in the subgroup of ab.
     * 
     * @param squareGroup the square group for verifiable encryption.
     * @param g the generator of the square group.
     * @param y the encryption element of the square group.
     * @param zPlus1 the encryption base of the square group.
     * 
     * @require compositeGroup.hasNoOrder() : "The order of the composite group is unknown.";
     * @require squareGroup.hasNoOrder() : "The order of the square group is unknown.";
     *          
     * @require ab.isElement(compositeGroup) : "ab is an element in the composite group.";
     * @require au.isElement(compositeGroup) : "au is an element in the composite group.";
     * @require ai.isElement(compositeGroup) : "ai is an element in the composite group.";
     * @require av.isElement(compositeGroup) : "av is an element in the composite group.";
     * @require ao.isElement(compositeGroup) : "ao is an element in the composite group.";
     *          
     * @require g.isElement(squareGroup) : "g is an element in the square group.";
     * @require y.isElement(squareGroup) : "y is an element in the square group.";
     * @require zPlus1.isElement(squareGroup) : "zPlus1 is an element in the square group.";
     *          
     * @require verifySubgroupProof() : "Assert that au, ai, av and ao are in the subgroup of ab.";
     */
    PublicKey(@Nonnull Group compositeGroup, @Nonnull Exponent e, @Nonnull Element ab, @Nonnull Element au, @Nonnull Element ai, @Nonnull Element av, @Nonnull Element ao, @Nonnull Exponent t, @Nonnull Exponent su, @Nonnull Exponent si, @Nonnull Exponent sv, @Nonnull Exponent so, @Nonnull Group squareGroup, @Nonnull Element g, @Nonnull Element y, @Nonnull Element zPlus1) {
        assert compositeGroup.hasNoOrder() : "The order of the composite group is unknown.";
        assert squareGroup.hasNoOrder() : "The order of the square group is unknown.";
        
        assert ab.isElement(compositeGroup) : "ab is an element in the composite group.";
        assert au.isElement(compositeGroup) : "au is an element in the composite group.";
        assert ai.isElement(compositeGroup) : "ai is an element in the composite group.";
        assert av.isElement(compositeGroup) : "av is an element in the composite group.";
        assert ao.isElement(compositeGroup) : "ao is an element in the composite group.";
        
        assert g.isElement(squareGroup) : "g is an element in the square group.";
        assert y.isElement(squareGroup) : "y is an element in the square group.";
        assert zPlus1.isElement(squareGroup) : "zPlus1 is an element in the square group.";
        
        this.compositeGroup = compositeGroup;
        this.e = e;
        this.ab = ab;
        this.au = au;
        this.ai = ai;
        this.av = av;
        this.ao = ao;
        this.t = t;
        this.su = su;
        this.si = si;
        this.sv = sv;
        this.so = so;
        this.squareGroup = squareGroup;
        this.g = g;
        this.y = y;
        this.zPlus1 = zPlus1;
        
        assert verifySubgroupProof() : "The elements au, ai, av and ao are in the subgroup of ab.";
    }
    
    /**
     * Creates a new public key with the groups, bases and exponents that are encoded in the given block.
     * 
     * @param block the block containing the public key.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PublicKey(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(16);
        this.compositeGroup = new Group(elements.getNotNull(0));
        this.e = new Exponent(elements.getNotNull(1));
        this.ab = compositeGroup.getElement(elements.getNotNull(2));
        this.au = compositeGroup.getElement(elements.getNotNull(3));
        this.ai = compositeGroup.getElement(elements.getNotNull(4));
        this.av = compositeGroup.getElement(elements.getNotNull(5));
        this.ao = compositeGroup.getElement(elements.getNotNull(6));
        this.t = new Exponent(elements.getNotNull(7));
        this.su = new Exponent(elements.getNotNull(8));
        this.si = new Exponent(elements.getNotNull(9));
        this.sv = new Exponent(elements.getNotNull(10));
        this.so = new Exponent(elements.getNotNull(11));
        this.squareGroup = new Group(elements.getNotNull(12));
        this.g = squareGroup.getElement(elements.getNotNull(13));
        this.y = squareGroup.getElement(elements.getNotNull(14));
        this.zPlus1 = squareGroup.getElement(elements.getNotNull(15));
        
        if (compositeGroup.hasOrder()) throw new InvalidEncodingException("The order of the composite group may not be known.");
        if (squareGroup.hasOrder()) throw new InvalidEncodingException("The order of the square group may not be known.");
        
        if (!verifySubgroupProof()) throw new InvalidEncodingException("The proof that au, ai, av and ao are in the subgroup of ab is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(16);
        elements.set(0, compositeGroup.toBlock().setType(COMPOSITE_GROUP));
        elements.set(1, e.toBlock().setType(E));
        elements.set(2, ab.toBlock().setType(AB));
        elements.set(3, au.toBlock().setType(AU));
        elements.set(4, ai.toBlock().setType(AI));
        elements.set(5, av.toBlock().setType(AV));
        elements.set(6, ao.toBlock().setType(AO));
        elements.set(7, t.toBlock().setType(T));
        elements.set(8, su.toBlock().setType(SU));
        elements.set(9, si.toBlock().setType(SI));
        elements.set(10, sv.toBlock().setType(SV));
        elements.set(11, so.toBlock().setType(SO));
        elements.set(12, squareGroup.toBlock().setType(SQUARE_GROUP));
        elements.set(13, g.toBlock().setType(G));
        elements.set(14, y.toBlock().setType(Y));
        elements.set(15, zPlus1.toBlock().setType(Z));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    /**
     * Returns whether the proof that au, ai, av and ao are in the subgroup of ab is correct.
     * 
     * @return {@code true} if the proof that au, ai, av and ao are in the subgroup of ab is correct, {@code false} otherwise.
     */
    @Pure
    public boolean verifySubgroupProof() {
        final @Nonnull Element tu = ab.pow(su).multiply(au.pow(t));
        final @Nonnull Element ti = ab.pow(si).multiply(ai.pow(t));
        final @Nonnull Element tv = ab.pow(sv).multiply(av.pow(t));
        final @Nonnull Element to = ab.pow(so).multiply(ao.pow(t));
        
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(tu.toBlock().setType(TU), ti.toBlock().setType(TI), tv.toBlock().setType(TV), to.toBlock().setType(TO));
        return t.getValue().equals(new TupleWrapper(TUPLE, elements.freeze()).toBlock().getHash());
    }
    
    
    /**
     * Returns the composite group for encryption and signing.
     * 
     * @return the composite group for encryption and signing.
     * 
     * @return return.hasNoOrder() : "The order of the composite group is unknown.";
     */
    @Pure
    public @Nonnull Group getCompositeGroup() {
        return compositeGroup;
    }
    
    /**
     * Returns the encryption and verification exponent.
     * 
     * @return the encryption and verification exponent.
     */
    @Pure
    public @Nonnull Exponent getE() {
        return e;
    }
    
    /**
     * Returns the base for blinding.
     * 
     * @return the base for blinding.
     */
    @Pure
    public @Nonnull Element getAb() {
        return ab;
    }
    
    /**
     * Returns the base of the client's secret.
     * 
     * @return the base of the client's secret.
     */
    @Pure
    public @Nonnull Element getAu() {
        return au;
    }
    
    /**
     * Returns the base of the serial number.
     * 
     * @return the base of the serial number.
     */
    @Pure
    public @Nonnull Element getAi() {
        return ai;
    }
    
    /**
     * Returns the base of the hashed identifier.
     * 
     * @return the base of the hashed identifier.
     */
    @Pure
    public @Nonnull Element getAv() {
        return av;
    }
    
    /**
     * Returns the base of the exposed arguments.
     * 
     * @return the base of the exposed arguments.
     */
    @Pure
    public @Nonnull Element getAo() {
        return ao;
    }
    
    /**
     * Returns the hash of the temporary commitments in the subgroup proof.
     * 
     * @return the hash of the temporary commitments in the subgroup proof.
     */
    @Pure
    public @Nonnull Exponent getT() {
        return t;
    }
    
    /**
     * Returns the solution for the proof that au is in the subgroup of ab.
     * 
     * @return the solution for the proof that au is in the subgroup of ab.
     */
    @Pure
    public @Nonnull Exponent getSu() {
        return su;
    }
    
    /**
     * Returns the solution for the proof that ai is in the subgroup of ab.
     * 
     * @return the solution for the proof that ai is in the subgroup of ab.
     */
    @Pure
    public @Nonnull Exponent getSi() {
        return si;
    }
    
    /**
     * Returns the solution for the proof that av is in the subgroup of ab.
     * 
     * @return the solution for the proof that av is in the subgroup of ab.
     */
    @Pure
    public @Nonnull Exponent getSv() {
        return sv;
    }
    
    /**
     * Returns the solution for the proof that ao is in the subgroup of ab.
     * 
     * @return the solution for the proof that ao is in the subgroup of ab.
     */
    @Pure
    public @Nonnull Exponent getSo() {
        return so;
    }
    
    /**
     * Returns the square group for verifiable encryption.
     * 
     * @return the square group for verifiable encryption.
     * 
     * @ensure return.hasNoOrder() : "The order of the square group is unknown.";
     */
    @Pure
    public @Nonnull Group getSquareGroup() {
        return squareGroup;
    }
    
    /**
     * Returns the generator of the square group.
     * 
     * @return the generator of the square group.
     */
    @Pure
    public @Nonnull Element getG() {
        return g;
    }
    
    /**
     * Returns the encryption element of the square group.
     * 
     * @return the encryption element of the square group.
     */
    @Pure
    public @Nonnull Element getY() {
        return y;
    }
    
    /**
     * Returns the encryption base of the square group.
     * 
     * @return the encryption base of the square group.
     */
    @Pure
    public @Nonnull Element getZPlus1() {
        return zPlus1;
    }
    
    /**
     * Returns the verifiable encryption of the given value m with the random value r.
     * 
     * @param m the message to be verifiably encrypted.
     * @param r the random value to blind the message.
     * 
     * @return the verifiable encryption of the given value m with the random value r.
     * 
     * @ensure return.getType().isBasedOn(VERIFIABLE_ENCRYPTION) : "The returned block is of the verifiable encryption type.";
     */
    @Pure
    public @Nonnull Block getVerifiableEncryption(@Nonnull Exponent m, @Nonnull Exponent r) {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, y.pow(r).multiply(zPlus1.pow(m)).toBlock().setType(W1));
        elements.set(1, g.pow(r).toBlock().setType(W2));
        return new TupleWrapper(VERIFIABLE_ENCRYPTION, elements.freeze()).toBlock();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof PublicKey)) return false;
        final @Nonnull PublicKey other = (PublicKey) object;
        return this.compositeGroup.equals(other.compositeGroup)
                && this.e.equals(other.e)
                && this.ab.equals(other.ab)
                && this.au.equals(other.au)
                && this.ai.equals(other.ai)
                && this.av.equals(other.av)
                && this.ao.equals(other.ao)
                && this.t.equals(other.t)
                && this.su.equals(other.su)
                && this.si.equals(other.si)
                && this.sv.equals(other.sv)
                && this.so.equals(other.so)
                && this.squareGroup.equals(other.squareGroup)
                && this.g.equals(other.g)
                && this.y.equals(other.y)
                && this.zPlus1.equals(other.zPlus1);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + compositeGroup.hashCode();
        hash = 13 * hash + e.hashCode();
        hash = 13 * hash + ab.hashCode();
        hash = 13 * hash + au.hashCode();
        hash = 13 * hash + ai.hashCode();
        hash = 13 * hash + av.hashCode();
        hash = 13 * hash + ao.hashCode();
        hash = 13 * hash + t.hashCode();
        hash = 13 * hash + su.hashCode();
        hash = 13 * hash + si.hashCode();
        hash = 13 * hash + sv.hashCode();
        hash = 13 * hash + so.hashCode();
        hash = 13 * hash + squareGroup.hashCode();
        hash = 13 * hash + g.hashCode();
        hash = 13 * hash + y.hashCode();
        hash = 13 * hash + zPlus1.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Public Key [n = " + compositeGroup.getModulus() + ", e = " + e + ", z^2 = " + squareGroup.getModulus() + ", g = " + g + ", y = " + y + "]";
    }
    
}
