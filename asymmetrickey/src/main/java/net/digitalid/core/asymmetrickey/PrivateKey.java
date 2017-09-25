package net.digitalid.core.asymmetrickey;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.group.InGroup;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.GroupWithKnownOrder;

/**
 * This class stores the groups and exponents of a host's private key.
 * 
 * @invariant getCompositeGroup().getModulus().equals(getP().multiply(getQ())) : "The modulus of the composite group is the product of p and q.";
 * 
 * @see PublicKey
 * @see KeyPair
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PrivateKey extends AsymmetricKey {
    
    /* -------------------------------------------------- Composite Group -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull GroupWithKnownOrder getCompositeGroup();
    
    /* -------------------------------------------------- Prime Factors -------------------------------------------------- */
    
    /**
     * Returns the first prime factor of the composite group's modulus.
     */
    @Pure
    protected abstract @Nonnull BigInteger getP();
    
    /**
     * Returns the second prime factor of the composite group's modulus.
     */
    @Pure
    protected abstract @Nonnull BigInteger getQ();
    
    /* -------------------------------------------------- Decryption Exponent -------------------------------------------------- */
    
    /**
     * Returns the decryption exponent d of this private key.
     */
    @Pure
    public abstract @Nonnull Exponent getD();
    
    /**
     * Returns the decryption exponent in the subgroup of p.
     */
    @Pure
    @Derive("d.getValue().mod(p.subtract(BigInteger.ONE))")
    protected abstract @Nonnull BigInteger getDModPMinus1();
    
    /**
     * Returns the decryption exponent in the subgroup of q.
     */
    @Pure
    @Derive("d.getValue().mod(q.subtract(BigInteger.ONE))")
    protected abstract @Nonnull BigInteger getDModQMinus1();
    
    /**
     * Returns the identity of p's subgroup in the Chinese Remainder Theorem.
     */
    @Pure
    @Derive("q.modInverse(p).multiply(q).mod(compositeGroup.getModulus())")
    protected abstract @Nonnull BigInteger getPIdentityCRT();
    
    /**
     * Returns the identity of q's subgroup in the Chinese Remainder Theorem.
     */
    @Pure
    @Derive("p.modInverse(q).multiply(p).mod(compositeGroup.getModulus())")
    protected abstract @Nonnull BigInteger getQIdentityCRT();
    
    /**
     * Returns the integer c raised to the power of d by using the Chinese Remainder Theorem.
     */
    @Pure
    public @Nonnull @InGroup("compositeGroup") Element powD(@Nonnull BigInteger c) {
        final @Nonnull BigInteger mModP = c.modPow(getDModPMinus1(), getP());
        final @Nonnull BigInteger mModQ = c.modPow(getDModQMinus1(), getQ());
        final @Nonnull BigInteger value = mModP.multiply(getPIdentityCRT()).add(mModQ.multiply(getQIdentityCRT()));
        return getCompositeGroup().getElement(value);
    }
    
    /**
     * Returns the element c raised to the power of d by using the Chinese Remainder Theorem.
     */
    @Pure
    public @Nonnull @InGroup("compositeGroup") Element powD(@Nonnull @InGroup("compositeGroup") Element c) {
        return powD(c.getValue());
    }
    
    /* -------------------------------------------------- Square Group -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull GroupWithKnownOrder getSquareGroup();
    
    /* -------------------------------------------------- Decryption Exponent -------------------------------------------------- */
    
    /**
     * Returns the decryption exponent x of this private key.
     */
    @Pure
    public abstract @Nonnull Exponent getX();
    
    /* -------------------------------------------------- Validate -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Require.that(getCompositeGroup().getModulus().equals(getP().multiply(getQ()))).orThrow("The modulus of the composite group has to be the product of p and q.");
    }
    
}
