package net.digitalid.core.credential;

import java.math.BigInteger;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.ExposedExponentConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class abstracts from client and host credentials.
 * 
 * @see ClientCredential
 * @see HostCredential
 */
@Immutable
public abstract class Credential extends RootClass {
    
    // TODO: Check this?
    // if (isIdentityBased() && i != null) { throw InvalidParameterValueCombinationException.get("If the credential is identity-based, the value i has to be null."); }
    
    /* -------------------------------------------------- Exposed Exponent -------------------------------------------------- */
    
    /**
     * Returns the exposed exponent of this credential.
     */
    @Pure
    public abstract @Nonnull ExposedExponent getExposedExponent();
    
    /* -------------------------------------------------- Hash of Exponent -------------------------------------------------- */
    
    @Pure
    protected @Nonnull Exponent deriveO() {
        try {
            return ExponentBuilder.withValue(new BigInteger(1, XDF.hash(getExposedExponent(), ExposedExponentConverter.INSTANCE))).build();
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO: How to handle or propagate such exceptions?
        }
    }
    
    /**
     * Returns the hash of the exposed exponent.
     */
    @Pure
    @Derive("deriveO()")
    public abstract @Nonnull Exponent getO();
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Returns the restrictions of the client or null in case they are not shown.
     */
    @Pure
    public abstract @Nullable Restrictions getRestrictions();
    
    /**
     * Returns the restrictions of the client.
     * 
     * @require getRestrictions() != null : "The restrictions are not null.";
     */
    @Pure
    public final @Nonnull Restrictions getRestrictionsNotNull() {
        Require.that(getRestrictions() != null).orThrow("The restrictions are not null.");
        
        return getRestrictions();
    }
    
    /* -------------------------------------------------- Serial Number -------------------------------------------------- */
    
    /**
     * Returns the serial number of this credential or null if it is not shown.
     */
    @Pure
    public abstract @Nullable Exponent getI();
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    /**
     * Returns whether this credential is similar to the given credential.
     * Credentials are similar to each other if they are a randomization of the same credential.
     */
    @Pure
    public boolean isSimilarTo(@Nonnull Credential credential) {
        return getExposedExponent().equals(credential.getExposedExponent()) && Objects.equals(getI(), credential.getI());
    }
    
}
