package net.digitalid.core.credential;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;

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
//        final @Nonnull Exponent r = Exponent.get(new BigInteger(Parameters.BLINDING_EXPONENT - Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
//        return new ClientCredential(getPublicKey(), getIssuer(), getIssuance(), getRandomizedPermissions(), getRole(), getAttributeContent(), getRestrictions(), c.multiply(getPublicKey().getAb().pow(r)), e, b.subtract(e.multiply(r)), u, getI(), v, oneTime);
        return null;
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
//     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
//     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
//     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
//     * @require role == null || restrictions != null : "If a role is given, the restrictions are not null.";
//     * @require attributeContent != null || issuer instanceof Person : "If the attribute content is null, the issuer is a person.";
//     * @require (attributeContent == null) != (restrictions == null) : "Either the attribute content or the restrictions are null (but not both).";
//     * @require restrictions == null || restrictions.toBlock().getHash().equals(v.getValue()) : "If the restrictions are not null, their hash has to equal v.";
//     * @require !oneTime || attributeContent != null : "If the credential can be used only once, the attribute content may not be null.";
        
//        Require.that(restrictions == null || restrictions.toBlock().getHash().equals(v.getValue())).orThrow("If the restrictions are not null, their hash has to equal v.");
//        Require.that(!oneTime || attributeContent != null).orThrow("If the credential can be used only once, the attribute content may not be null.");
//        
//        if (!publicKey.getAo().pow(getO()).equals(c.pow(e).multiply(publicKey.getAb().pow(b)).multiply(publicKey.getAu().pow(u)).multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)))) { throw new InvalidSignatureException("The credential issued by " + issuer.getAddress() + " is invalid."); }
//        
//        Validate.that(isIdentityBased() != isAttributeBased()).orThrow("This credential is either identity- or attribute-based.");
//        Validate.that(!isRoleBased() || isIdentityBased()).orThrow("If this credential is role-based, it is also identity-based");
//        Validate.that(!isAttributeBased() || getRestrictions() == null).orThrow("If this credential is attribute-based, the restrictions are null.");
//        Validate.that(!isRoleBased() || getPermissions() != null && getRestrictions() != null).orThrow("If this credential is role-based, both the permissions and the restrictions are not null.");
        super.validate();
    }
    
}
