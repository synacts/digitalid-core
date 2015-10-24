package net.digitalid.service.core.cryptography;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.database.storing.Storable;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;

/**
 * This class stores the groups, elements and exponents of a host's public key.
 * 
 * @invariant verifySubgroupProof() : "The elements au, ai, av and ao are in the subgroup of ab.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class PublicKey implements Storable<PublicKey> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code composite.group.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType COMPOSITE_GROUP = SemanticType.map("composite.group.public.key.host@core.digitalid.net").load(GroupWithUnknownOrder.TYPE);
    
    /**
     * Stores the semantic type {@code e.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType E = SemanticType.map("e.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code ab.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AB = SemanticType.map("ab.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code au.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AU = SemanticType.map("au.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ai.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AI = SemanticType.map("ai.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code av.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AV = SemanticType.map("av.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ao.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AO = SemanticType.map("ao.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code t.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType T = SemanticType.map("t.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code su.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SU = SemanticType.map("su.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code si.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SI = SemanticType.map("si.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sv.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SV = SemanticType.map("sv.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code so.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SO = SemanticType.map("so.public.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code square.group.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SQUARE_GROUP = SemanticType.map("square.group.public.key.host@core.digitalid.net").load(GroupWithUnknownOrder.TYPE);
    
    /**
     * Stores the semantic type {@code g.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType G = SemanticType.map("g.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code y.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType Y = SemanticType.map("y.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code z.public.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType Z = SemanticType.map("z.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("public.key.host@core.digitalid.net").load(TupleWrapper.TYPE, COMPOSITE_GROUP, E, AB, AU, AI, AV, AO, T, SU, SI, SV, SO, SQUARE_GROUP, G, Y, Z);
    
    
    /**
     * Stores the semantic type {@code tu.public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TU = SemanticType.map("tu.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code ti.public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TI = SemanticType.map("ti.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code tv.public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TV = SemanticType.map("tv.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code to.public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TO = SemanticType.map("to.public.key.host@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code tuple.public.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TUPLE = SemanticType.map("tuple.public.key.host@core.digitalid.net").load(TupleWrapper.TYPE, TU, TI, TV, TO);
    
    
    /**
     * Stores the semantic type {@code w1.verifiable.encryption@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType W1 = SemanticType.map("w1.verifiable.encryption@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code w2.verifiable.encryption@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType W2 = SemanticType.map("w2.verifiable.encryption@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code verifiable.encryption@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType VERIFIABLE_ENCRYPTION = SemanticType.map("verifiable.encryption@core.digitalid.net").load(TupleWrapper.TYPE, W1, W2);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Composite Group –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the composite group for encryption and signing.
     */
    private final @Nonnull GroupWithUnknownOrder compositeGroup;
    
    /**
     * Returns the composite group for encryption and signing.
     * 
     * @return the composite group for encryption and signing.
     */
    @Pure
    public @Nonnull GroupWithUnknownOrder getCompositeGroup() {
        return compositeGroup;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encryption Exponent –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encryption and verification exponent.
     */
    private final @Nonnull Exponent e;
    
    /**
     * Returns the encryption and verification exponent.
     * 
     * @return the encryption and verification exponent.
     */
    @Pure
    public @Nonnull Exponent getE() {
        return e;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Base for Blinding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the base for blinding.
     */
    private final @Nonnull Element ab;
    
    /**
     * Returns the base for blinding.
     * 
     * @return the base for blinding.
     */
    @Pure
    public @Nonnull Element getAb() {
        return ab;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Base for Secret –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the base of the client's secret.
     */
    private final @Nonnull Element au;
    
    /**
     * Returns the base of the client's secret.
     * 
     * @return the base of the client's secret.
     */
    @Pure
    public @Nonnull Element getAu() {
        return au;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Base for Serial –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the base of the serial number.
     */
    private final @Nonnull Element ai;
    
    /**
     * Returns the base of the serial number.
     * 
     * @return the base of the serial number.
     */
    @Pure
    public @Nonnull Element getAi() {
        return ai;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Base for Identifier –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the base of the hashed identifier.
     */
    private final @Nonnull Element av;
    
    /**
     * Returns the base of the hashed identifier.
     * 
     * @return the base of the hashed identifier.
     */
    @Pure
    public @Nonnull Element getAv() {
        return av;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Base for Arguments –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the base of the exposed arguments.
     */
    private final @Nonnull Element ao;
    
    /**
     * Returns the base of the exposed arguments.
     * 
     * @return the base of the exposed arguments.
     */
    @Pure
    public @Nonnull Element getAo() {
        return ao;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Subgroup Proof –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
        
        final @Nonnull FreezableArray<Block> elements = FreezableArray.getNonNullable(Block.fromNonNullable(tu, PublicKey.TU), Block.fromNonNullable(ti, PublicKey.TI), Block.fromNonNullable(tv, PublicKey.TV), Block.fromNonNullable(to, PublicKey.TO));
        return t.getValue().equals(TupleWrapper.encode(TUPLE, elements.freeze()).getHash());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Square Group –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the square group for verifiable encryption.
     */
    private final @Nonnull GroupWithUnknownOrder squareGroup;
    
    /**
     * Returns the square group for verifiable encryption.
     * 
     * @return the square group for verifiable encryption.
     */
    @Pure
    public @Nonnull GroupWithUnknownOrder getSquareGroup() {
        return squareGroup;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Group Generator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the generator of the square group.
     */
    private final @Nonnull Element g;
    
    /**
     * Returns the generator of the square group.
     * 
     * @return the generator of the square group.
     */
    @Pure
    public @Nonnull Element getG() {
        return g;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encryption Element –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encryption element of the square group.
     */
    private final @Nonnull Element y;

    /**
     * Returns the encryption element of the square group.
     * 
     * @return the encryption element of the square group.
     */
    @Pure
    public @Nonnull Element getY() {
        return y;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encryption Base –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encryption base of the square group.
     */
    private final @Nonnull Element zPlus1;
    
    /**
     * Returns the encryption base of the square group.
     * 
     * @return the encryption base of the square group.
     */
    @Pure
    public @Nonnull Element getZPlus1() {
        return zPlus1;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */

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
    private PublicKey(@Nonnull GroupWithUnknownOrder compositeGroup, @Nonnull Exponent e, @Nonnull Element ab, @Nonnull Element au, @Nonnull Element ai, @Nonnull Element av, @Nonnull Element ao, @Nonnull Exponent t, @Nonnull Exponent su, @Nonnull Exponent si, @Nonnull Exponent sv, @Nonnull Exponent so, @Nonnull GroupWithUnknownOrder squareGroup, @Nonnull Element g, @Nonnull Element y, @Nonnull Element zPlus1) {
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
    @Pure
    public static @Nonnull PublicKey get(@Nonnull GroupWithUnknownOrder compositeGroup, @Nonnull Exponent e, @Nonnull Element ab, @Nonnull Element au, @Nonnull Element ai, @Nonnull Element av, @Nonnull Element ao, @Nonnull Exponent t, @Nonnull Exponent su, @Nonnull Exponent si, @Nonnull Exponent sv, @Nonnull Exponent so, @Nonnull GroupWithUnknownOrder squareGroup, @Nonnull Element g, @Nonnull Element y, @Nonnull Element zPlus1) {
        return new PublicKey(compositeGroup, e, ab, au, ai, av, ao, t, su, si, sv, so, squareGroup, g, y, zPlus1);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Verifiable Encryption –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the verifiable encryption of the given value m with the random value r.
     * 
     * @param m the message to be verifiably encrypted.
     * @param r the random value to blind the message.
     * 
     * @return the verifiable encryption of the given value m with the random value r.
     */
    @Pure
    public @Nonnull @BasedOn("verifiable.encryption@core.digitalid.net") Block getVerifiableEncryption(@Nonnull Exponent m, @Nonnull Exponent r) {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
        elements.set(0, Block.fromNonNullable(y.pow(r).multiply(zPlus1.pow(m)), W1));
        elements.set(1, Block.fromNonNullable(g.pow(r), W2));
        return TupleWrapper.encode(VERIFIABLE_ENCRYPTION, elements.freeze());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends BlockBasedSimpleNonConceptFactory<PublicKey> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull PublicKey publicKey) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(16);
            elements.set(0, Block.fromNonNullable(publicKey.compositeGroup, COMPOSITE_GROUP));
            elements.set(1, Block.fromNonNullable(publicKey.e, E));
            elements.set(2, Block.fromNonNullable(publicKey.ab, AB));
            elements.set(3, Block.fromNonNullable(publicKey.au, AU));
            elements.set(4, Block.fromNonNullable(publicKey.ai, AI));
            elements.set(5, Block.fromNonNullable(publicKey.av, AV));
            elements.set(6, Block.fromNonNullable(publicKey.ao, AO));
            elements.set(7, Block.fromNonNullable(publicKey.t, T));
            elements.set(8, Block.fromNonNullable(publicKey.su, SU));
            elements.set(9, Block.fromNonNullable(publicKey.si, SI));
            elements.set(10, Block.fromNonNullable(publicKey.sv, SV));
            elements.set(11, Block.fromNonNullable(publicKey.so, SO));
            elements.set(12, Block.fromNonNullable(publicKey.squareGroup, SQUARE_GROUP));
            elements.set(13, Block.fromNonNullable(publicKey.g, G));
            elements.set(14, Block.fromNonNullable(publicKey.y, Y));
            elements.set(15, Block.fromNonNullable(publicKey.zPlus1, Z));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull PublicKey decodeNonNullable(@Nonnull @BasedOn("public.key.host@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(16);
            final @Nonnull GroupWithUnknownOrder compositeGroup = GroupWithUnknownOrder.FACTORY.decodeNonNullable(elements.getNonNullable(0));
            final @Nonnull Exponent e = Exponent.get(elements.getNonNullable(1));
            final @Nonnull Element ab = compositeGroup.getElement(elements.getNonNullable(2));
            final @Nonnull Element au = compositeGroup.getElement(elements.getNonNullable(3));
            final @Nonnull Element ai = compositeGroup.getElement(elements.getNonNullable(4));
            final @Nonnull Element av = compositeGroup.getElement(elements.getNonNullable(5));
            final @Nonnull Element ao = compositeGroup.getElement(elements.getNonNullable(6));
            final @Nonnull Exponent t = Exponent.get(elements.getNonNullable(7));
            final @Nonnull Exponent su = Exponent.get(elements.getNonNullable(8));
            final @Nonnull Exponent si = Exponent.get(elements.getNonNullable(9));
            final @Nonnull Exponent sv = Exponent.get(elements.getNonNullable(10));
            final @Nonnull Exponent so = Exponent.get(elements.getNonNullable(11));
            final @Nonnull GroupWithUnknownOrder squareGroup = GroupWithUnknownOrder.FACTORY.decodeNonNullable(elements.getNonNullable(12));
            final @Nonnull Element g = squareGroup.getElement(elements.getNonNullable(13));
            final @Nonnull Element y = squareGroup.getElement(elements.getNonNullable(14));
            final @Nonnull Element zPlus1 = squareGroup.getElement(elements.getNonNullable(15));
            
            final @Nonnull Element tu = ab.pow(su).multiply(au.pow(t));
            final @Nonnull Element ti = ab.pow(si).multiply(ai.pow(t));
            final @Nonnull Element tv = ab.pow(sv).multiply(av.pow(t));
            final @Nonnull Element to = ab.pow(so).multiply(ao.pow(t));
            
            if (!t.getValue().equals(TupleWrapper.encode(TUPLE, Block.fromNonNullable(tu, PublicKey.TU), Block.fromNonNullable(ti, PublicKey.TI), Block.fromNonNullable(tv, PublicKey.TV), Block.fromNonNullable(to, PublicKey.TO)).getHash())) throw new InvalidEncodingException("The proof that au, ai, av and ao are in the subgroup of ab is invalid.");
            
            return new PublicKey(compositeGroup, e, ab, au, ai, av, ao, t, su, si, sv, so, squareGroup, g, y, zPlus1);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
