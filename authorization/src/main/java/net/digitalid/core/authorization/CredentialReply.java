package net.digitalid.core.authorization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Replies the parameters of a new credential.
 * 
 * @see CredentialInternalQuery
 * @see CredentialExternalQuery
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
abstract class CredentialReply extends QueryReply<NonHostEntity> implements CoreHandler<NonHostEntity> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the public key of the host that issued the credential.
     */
    @Pure
    abstract @Nonnull PublicKey getPublicKey();
    
    /**
     * Returns the restrictions for which the credential is issued.
     */
    @Pure
    abstract @Nullable Restrictions getRestrictions();
    
    /**
     * Returns the issuance time rounded down to the last half-hour.
     */
    @Pure
    abstract @Nonnull Time getIssuance();
    
    /**
     * Returns the certifying base of the issued credential.
     */
    @Pure
    abstract @Nonnull Element getC();
    
    /**
     * Returns the certifying exponent of the issued credential.
     */
    @Pure
    abstract @Nonnull Exponent getE();
    
    /**
     * Returns the serial number of the issued credential.
     */
    @Pure
    abstract @Nonnull Exponent getI();
    
    /* -------------------------------------------------- Decription -------------------------------------------------- */
    
    // TODO: Replicate the derivation of the public key and the issuance check.
    
//    /**
//     * Creates a query reply that decodes a packet with the given signature for the given entity.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the host signature of this handler.
//     * @param number the number that references this reply.
//     * @param block the content which is to be decoded.
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
//     */
//    @NonCommitting
//    private CredentialReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, number);
//        
//        if (!hasEntity()) { throw InternalException.get("A credential reply must have an entity."); }
//        
//        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
//        this.restrictions = tuple.isElementNotNull(0) ? new Restrictions(entity, tuple.getNonNullableElement(0)) : null;
//        this.issuance = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(1));
//        this.publicKey = Cache.getPublicKey(signature.getNonNullableSubject().getHostIdentifier(), issuance);
//        this.c = publicKey.getCompositeGroup().getElement(tuple.getNonNullableElement(2));
//        this.e = Exponent.get(tuple.getNonNullableElement(3));
//        this.i = Exponent.get(tuple.getNonNullableElement(4));
//        
//        if (issuance.isLessThan(Time.HOUR.ago())) { throw InvalidParameterValueException.get("issuance time", issuance); }
//    }
    
    /* -------------------------------------------------- Client Credential -------------------------------------------------- */
    
    // TODO
    
//    /**
//     * Returns an internal credential with the given parameters.
//     * 
//     * @param randomizedPermissions the client's randomized permissions.
//     * @param role the role that is assumed or null in case no role is assumed.
//     * @param b the blinding exponent of the issued credential.
//     * @param u the client's secret of the issued credential.
//     * 
//     * @return an internal credential with the given parameters.
//     * 
//     * @require hasSignature() : "This handler has a signature.";
//     */
//    @NonCommitting
//    @Nonnull ClientCredential getInternalCredential(@Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nonnull BigInteger b, @Nonnull Exponent u) throws ExternalException {
//        Require.that(hasSignature()).orThrow("This handler has a signature.");
//        
//        if (restrictions == null) { throw InvalidParameterValueCombinationException.get("The restrictions may not be null for internal credentials."); }
//        final @Nonnull Exponent v = Exponent.get(restrictions.toBlock().getHash());
//        
//        final @Nonnull InternalPerson issuer = getSignatureNotNull().getNonNullableSubject().getIdentity().castTo(InternalPerson.class);
//        return new ClientCredential(publicKey, issuer, issuance, randomizedPermissions, role, restrictions, c, e, Exponent.get(b), u, i, v);
//    }
//    
//    /**
//     * Returns an external credential with the given parameters.
//     * 
//     * @param randomizedPermissions the client's randomized permissions.
//     * @param attributeContent the attribute content for access control.
//     * @param b the blinding exponent of the issued credential.
//     * @param u the client's secret of the issued credential.
//     * @param v the hash of the requester's identifier.
//     * 
//     * @return an external credential with the given parameters.
//     * 
//     * @require hasSignature() : "This handler has a signature.";
//     */
//    @NonCommitting
//    @Nonnull ClientCredential getExternalCredential(@Nonnull RandomizedAgentPermissions randomizedPermissions, @Nonnull Block attributeContent, @Nonnull BigInteger b, @Nonnull Exponent u, @Nonnull Exponent v) throws ExternalException {
//        Require.that(hasSignature()).orThrow("This handler has a signature.");
//        
//        if (restrictions != null) { throw InvalidParameterValueCombinationException.get("The restrictions must be null for external credentials."); }
//        
//        final @Nonnull InternalNonHostIdentity issuer = getSignatureNotNull().getNonNullableSubject().getIdentity().castTo(InternalNonHostIdentity.class);
//        return new ClientCredential(publicKey, issuer, issuance, randomizedPermissions, attributeContent, c, e, Exponent.get(b), u, i, v, false);
//    }
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<NonHostEntity> method) {
        return method instanceof CredentialInternalQuery;
    }
    
}
