package net.digitalid.core.credential;


import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.restrictions.RestrictionsConverter;

/**
 * This class models credentials on the client-side.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ClientCredential extends Credential {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the certifying base of this credential.
     */
    @Pure
    public abstract @Nonnull Element getC();
    
    /**
     * Returns the certifying exponent of this credential.
     */
    @Pure
    public abstract @Nonnull Exponent getE();
    
    /**
     * Returns the blinding exponent of this credential.
     */
    @Pure
    public abstract @Nonnull Exponent getB();
    
    /**
     * Returns the client's secret of this credential.
     */
    @Pure
    public abstract @Nonnull Exponent getU();
    
    /**
     * Returns the hash of restrictions or the hash of the subject's identifier.
     */
    @Pure
    public abstract @Nonnull Exponent getV();
    
    /**
     * Returns the serial number of this credential.
     */
    @Pure
    @Override
    public abstract @Nonnull Exponent getI();
    
    /**
     * Returns whether this credential can be used only once (i.e. 'i' is shown).
     */
    @Pure
    @Default("false")
    public abstract boolean isOneTime();
    
    /* -------------------------------------------------- Randomization -------------------------------------------------- */
    
    /**
     * Returns a randomized version of this credential.
     */
    @Pure
    public @Nonnull ClientCredential getRandomizedCredential() {
        final @Nonnull Exponent r = ExponentBuilder.withValue(new BigInteger(Parameters.BLINDING_EXPONENT.get() - Parameters.CREDENTIAL_EXPONENT.get(), new SecureRandom())).build();
        return ClientCredentialBuilder.withExposedExponent(getExposedExponent()).withC(getC().multiply(getExposedExponent().getPublicKey().getAb().pow(r))).withE(getE()).withB(getB().subtract(getE().multiply(r))).withU(getU()).withV(getV()).withI(getI()).withRestrictions(getRestrictions()).withOneTime(isOneTime()).build();
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(getExposedExponent().getHashedOrSaltedPermissions().areExposed()).orThrow("The salted permissions have to be provided for client credentials.");
        try {
            Validate.that(getRestrictions() == null || new BigInteger(1, XDF.hash(getRestrictions(), RestrictionsConverter.INSTANCE)).equals(getV().getValue())).orThrow("If the restrictions are not null, their hash has to equal v.");
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO: How to handle or propagate such exceptions?
        }
        Validate.that(!isOneTime() || isAttributeBased()).orThrow("If the credential can be used only once, it has to be attribute-based.");
        final @Nonnull PublicKey publicKey = getExposedExponent().getPublicKey();
        Validate.that(publicKey.getAo().pow(getO()).equals(getC().pow(getE()).multiply(publicKey.getAb().pow(getB())).multiply(publicKey.getAu().pow(getU())).multiply(publicKey.getAi().pow(getI())).multiply(publicKey.getAv().pow(getV())))).orThrow("The credential issued by $ is invalid.", getExposedExponent().getIssuer().getAddress());
        super.validate();
    }
    
}
