package net.digitalid.core.asymmetrickey;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.GroupWithKnownOrder;
import net.digitalid.core.group.GroupWithKnownOrderBuilder;
import net.digitalid.core.parameters.Parameters;

/**
 * This class generates new key pairs.
 * 
 * @see PrivateKey
 * @see PublicKey
 */
@Immutable
public class KeyPair extends RootClass {
    
    /* -------------------------------------------------- Private Key -------------------------------------------------- */
    
    private final @Nonnull PrivateKey privateKey;
    
    /**
     * Returns the private key of this key pair.
     */
    @Pure
    public @Nonnull PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Returns the public key of this key pair.
     */
    @Pure
    public @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
    /* -------------------------------------------------- Utility -------------------------------------------------- */
    
    /**
     * Returns a safe prime with the given bit-length.
     */
    @Pure
    private static @Nonnull BigInteger getSafePrime(@Positive int length, @Nonnull Random random) {
        Require.that(length > 0).orThrow("The length has to be positive.");
        
        while (true) {
            final @Nonnull BigInteger prime = BigInteger.probablePrime(length - 1, random);
            final @Nonnull BigInteger value = prime.shiftLeft(1).add(BigInteger.ONE);
            if (value.isProbablePrime(64)) { return value; }
        }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */

    /**
     * Creates a new key pair with random values.
     */
    protected KeyPair() {
        final @Nonnull Random random = new SecureRandom();
        
        Log.debugging("Generating a new key pair of length " + Parameters.FACTOR.get() * Parameters.FACTOR.get());
        
        Log.verbose("Generating the safe prime 'p' of length " + Parameters.FACTOR.get());
        final @Nonnull BigInteger p = getSafePrime(Parameters.FACTOR.get(), random);
        
        Log.verbose("Generating the safe prime 'q' of length " + Parameters.FACTOR.get());
        final @Nonnull BigInteger q = getSafePrime(Parameters.FACTOR.get(), random);
        
        Log.verbose("Calculating the modulus and order of the composite group.");
        final @Nonnull BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        final @Nonnull BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        
        final @Nonnull BigInteger modulus = p.multiply(q);
        final @Nonnull BigInteger order = pMinus1.multiply(qMinus1);
        final @Nonnull GroupWithKnownOrder compositeGroup = GroupWithKnownOrderBuilder.withModulus(modulus).withOrder(order).build();
        
        Log.verbose("Generating the encryption and decryption exponents.");
        final @Nonnull Exponent e = ExponentBuilder.withValue(BigInteger.valueOf(65_537)).build().getNextRelativePrime(compositeGroup);
        final @Nonnull Exponent d = e.inverse(compositeGroup);
        
        Log.verbose("Generating the bases of the composite group.");
        @Nonnull Element ab = compositeGroup.getRandomElement();
        while (ab.pow(pMinus1).isOne() || ab.pow(qMinus1).isOne() || ab.pow(order.shiftRight(2)).isOne()) { ab = compositeGroup.getRandomElement(); }
        
        final @Nonnull Exponent eu = compositeGroup.getRandomExponent();
        final @Nonnull Element au = ab.pow(eu);
        
        final @Nonnull Exponent ei = compositeGroup.getRandomExponent();
        final @Nonnull Element ai = ab.pow(ei);
        
        final @Nonnull Exponent ev = compositeGroup.getRandomExponent();
        final @Nonnull Element av = ab.pow(ev);
        
        final @Nonnull Exponent eo = compositeGroup.getRandomExponent();
        final @Nonnull Element ao = ab.pow(eo);
        
        final @Nonnull Exponent ru = compositeGroup.getRandomExponent();
        final @Nonnull Element tu = ab.pow(ru);
        
        final @Nonnull Exponent ri = compositeGroup.getRandomExponent();
        final @Nonnull Element ti = ab.pow(ri);
        
        final @Nonnull Exponent rv = compositeGroup.getRandomExponent();
        final @Nonnull Element tv = ab.pow(rv);
        
        final @Nonnull Exponent ro = compositeGroup.getRandomExponent();
        final @Nonnull Element to = ab.pow(ro);
        
        final @Nonnull Exponent t = ExponentBuilder.withValue(HashGenerator.generateHash(tu, ti, tv, to)).build();
        
        final @Nonnull Exponent su = ru.subtract(t.multiply(eu));
        final @Nonnull Exponent si = ri.subtract(t.multiply(ei));
        final @Nonnull Exponent sv = rv.subtract(t.multiply(ev));
        final @Nonnull Exponent so = ro.subtract(t.multiply(eo));
        
        Log.verbose("Validating the proof that all bases are in the same subgroup.");
        Validate.that(tu.equals(ab.pow(su).multiply(au.pow(t))) && ti.equals(ab.pow(si).multiply(ai.pow(t))) && tv.equals(ab.pow(sv).multiply(av.pow(t))) && to.equals(ab.pow(so).multiply(ao.pow(t)))).orThrow("The non-interactive proof of the bases' correctness has to be valid.");
        
        Log.verbose("Generating the probable prime 'z' of length " + Parameters.VERIFIABLE_ENCRYPTION.get());
        final @Nonnull BigInteger z = BigInteger.probablePrime(Parameters.VERIFIABLE_ENCRYPTION.get(), random);
        final @Nonnull BigInteger zMinus1 = z.subtract(BigInteger.ONE);
        
        Log.verbose("Calculating the modulus and order of the square group.");
        final @Nonnull GroupWithKnownOrder squareGroup = GroupWithKnownOrderBuilder.withModulus(z.pow(2)).withOrder(z.multiply(zMinus1)).build();
        
        Log.verbose("Choosing the random element 'g' in the square group.");
        @Nonnull Element g = squareGroup.getRandomElement();
        while (g.pow(z).isOne() || g.pow(zMinus1).isOne()) { g = squareGroup.getRandomElement(); }
        
        Log.verbose("Choosing the random exponent 'x' in the square group.");
        final @Nonnull Exponent x = squareGroup.getRandomExponent();
        final @Nonnull Element y = g.pow(x);
        final @Nonnull Element zPlus1 = squareGroup.getElement(z.add(BigInteger.ONE));
        
        // Create and store the private and the public key.
        this.privateKey = PrivateKeyBuilder.withCompositeGroup(compositeGroup).withP(p).withQ(q).withD(d).withSquareGroup(squareGroup).withX(x).build(); 
        this.publicKey = PublicKeyBuilder.withCompositeGroup(compositeGroup.dropOrder()).withE(e).withAb(ab).withAu(au).withAi(ai).withAv(av).withAo(ao).withT(t).withSu(su).withSi(si).withSv(sv).withSo(so).withSquareGroup(squareGroup.dropOrder()).withG(g).withY(y).withZPlus1(zPlus1).build();
    }
    
    /**
     * Returns a new key pair with random values.
     */
    @Pure
    public static @Nonnull KeyPair withRandomValues() {
        return new KeyPair();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean equals(@NonCaptured @Unmodified @Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof KeyPair)) { return false; }
        final @Nonnull KeyPair that = (KeyPair) object;
        return this.privateKey.equals(that.privateKey) && this.publicKey.equals(that.publicKey);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + privateKey.hashCode();
        hash = 53 * hash + publicKey.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "KeyPair(privateKey: " + privateKey + ", publicKey: " + publicKey + ")";
    }
    
}
