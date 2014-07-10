package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import static ch.virtualid.cryptography.PublicKey.TI;
import static ch.virtualid.cryptography.PublicKey.TO;
import static ch.virtualid.cryptography.PublicKey.TU;
import static ch.virtualid.cryptography.PublicKey.TUPLE;
import static ch.virtualid.cryptography.PublicKey.TV;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.Nonnull;

/**
 * This class generates new key pairs.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class KeyPair implements Immutable {
    
    /**
     * Stores the private key of this key pair.
     */
    private final @Nonnull PrivateKey privateKey;
    
    /**
     * Stores the public key of this key pair.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Creates a new key pair with random values.
     */
    public KeyPair() {
        final @Nonnull Random random = new SecureRandom();
        
        // Determine a new RSA group with two inverse exponents and a random generator.
        final @Nonnull BigInteger p = getSafePrime(Parameters.FACTOR, random);
        final @Nonnull BigInteger q = getSafePrime(Parameters.FACTOR, random);
        
        final @Nonnull BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        final @Nonnull BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        
        final @Nonnull BigInteger modulus = p.multiply(q);
        final @Nonnull BigInteger order = pMinus1.multiply(qMinus1);
        final @Nonnull Group compositeGroup = new Group(modulus, order);
        
        final @Nonnull Exponent e = new Exponent(BigInteger.valueOf(65537)).getNextRelativePrime(compositeGroup);
        final @Nonnull Exponent d = e.inverse(compositeGroup);
        
        @Nonnull Element ab = compositeGroup.getRandomElement();
        while (ab.pow(pMinus1).isOne() || ab.pow(qMinus1).isOne() || ab.pow(order.shiftRight(2)).isOne()) ab = compositeGroup.getRandomElement();
        
        // Determine the four other bases.
        final @Nonnull Exponent eu = compositeGroup.getRandomExponent();
        final @Nonnull Element au = ab.pow(eu);
        
        final @Nonnull Exponent ei = compositeGroup.getRandomExponent();
        final @Nonnull Element ai = ab.pow(ei);
        
        final @Nonnull Exponent ev = compositeGroup.getRandomExponent();
        final @Nonnull Element av = ab.pow(ev);
        
        final @Nonnull Exponent eo = compositeGroup.getRandomExponent();
        final @Nonnull Element ao = ab.pow(eo);
        
        // Make a non-interactive proof of their correctness.
        final @Nonnull Exponent ru = compositeGroup.getRandomExponent();
        final @Nonnull Element tu = ab.pow(ru);
        
        final @Nonnull Exponent ri = compositeGroup.getRandomExponent();
        final @Nonnull Element ti = ab.pow(ri);
        
        final @Nonnull Exponent rv = compositeGroup.getRandomExponent();
        final @Nonnull Element tv = ab.pow(rv);
        
        final @Nonnull Exponent ro = compositeGroup.getRandomExponent();
        final @Nonnull Element to = ab.pow(ro);
        
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(tu.toBlock().setType(TU), ti.toBlock().setType(TI), tv.toBlock().setType(TV), to.toBlock().setType(TO));
        final @Nonnull Exponent t = new Exponent(new TupleWrapper(TUPLE, elements).toBlock().getHash());
        
        final @Nonnull Exponent su = ru.subtract(t.multiply(eu));
        final @Nonnull Exponent si = ri.subtract(t.multiply(ei));
        final @Nonnull Exponent sv = rv.subtract(t.multiply(ev));
        final @Nonnull Exponent so = ro.subtract(t.multiply(eo));
        
        assert tu.equals(ab.pow(su).multiply(au.pow(t))) && ti.equals(ab.pow(si).multiply(ai.pow(t))) && tv.equals(ab.pow(sv).multiply(av.pow(t))) && to.equals(ab.pow(so).multiply(ao.pow(t))) : "The non-interactive proof of the bases' correctness is valid.";
        
        // Determine the values for the verifiable encryption.
        final @Nonnull BigInteger z = BigInteger.probablePrime(Parameters.VERIFIABLE_ENCRYPTION, random);
        final @Nonnull BigInteger zMinus1 = z.subtract(BigInteger.ONE);
        
        final @Nonnull Group squareGroup = new Group(z.pow(2), z.multiply(zMinus1));
        @Nonnull Element g = squareGroup.getRandomElement();
        while (g.pow(z).isOne() || g.pow(zMinus1).isOne()) g = squareGroup.getRandomElement();
        
        final @Nonnull Exponent x = squareGroup.getRandomExponent();
        final @Nonnull Element y = g.pow(x);
        final @Nonnull Element zPlus1 = squareGroup.getElement(z.add(BigInteger.ONE));
        
        // Create and store the private and the public key.
        this.privateKey = new PrivateKey(compositeGroup, p, q, d, squareGroup, x);
        this.publicKey = new PublicKey(compositeGroup.dropOrder(), e, ab, au, ai, av, ao, t, su, si, sv, so, squareGroup.dropOrder(), g, y, zPlus1);
    }
    
    /**
     * Returns a safe prime with the given bit-length.
     * 
     * @param length the bit-length of the safe prime.
     * @param random the random number generator used.
     * 
     * @return a safe prime with the given bit-length.
     * 
     * @require length > 1 : "The length is positive.";
     */
    @Pure
    private static @Nonnull BigInteger getSafePrime(int length, @Nonnull Random random) {
        assert length > 1 : "The length is positive.";
        
        while (true) {
            final @Nonnull BigInteger prime = BigInteger.probablePrime(length - 1, random);
            final @Nonnull BigInteger value = prime.shiftLeft(1).add(BigInteger.ONE);
            if (value.isProbablePrime(100)) return value;
        }
    }
    
    
    /**
     * Returns the private key of this key pair.
     * 
     * @return the private key of this key pair.
     */
    @Pure
    public @Nonnull PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /**
     * Returns the public key of this key pair.
     * 
     * @return the public key of this key pair.
     */
    @Pure
    public @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
}
