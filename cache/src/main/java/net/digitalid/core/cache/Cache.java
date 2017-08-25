package net.digitalid.core.cache;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.cache.exceptions.AttributeNotFoundException;
import net.digitalid.core.cache.exceptions.AttributeNotFoundExceptionBuilder;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.exceptions.response.DeclarationExceptionBuilder;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.typeset.FreezableAttributeTypeSet;

/**
 * This class caches the {@link AttributeValue attribute values} of {@link Identity identities} for the attribute-specific {@link SemanticType#getCachingPeriod() caching period}.
 * 
 * @see CacheQuery
 */
@Utility
public abstract class Cache {
    
    /* -------------------------------------------------- Attribute Value -------------------------------------------------- */
    
    /**
     * Returns the expiration time of the given attribute value.
     * 
     * @param type the type that was queried.
     * @param value the value that was replied.
     * @param reply the reply containing the value.
     */
    @Pure
    private static @Nonnull Time getExpiration(@Nonnull SemanticType type, @Nullable AttributeValue value, @Nonnull AttributesReply reply) {
        final @Nonnull Time signatureTime = value instanceof CertifiedAttributeValue ? ((CertifiedAttributeValue) value).getSignature().getTime() : reply.getSignature().getTime();
        final @Nonnull Time cachingPeriod = type.getCachingPeriod();
        return signatureTime.add(cachingPeriod);
    }
    
    /**
     * Returns the attribute values of the given requestee with the given types.
     * The attribute values are returned in the same order as given by the types.
     * If an attribute value is not available, the value null is returned instead.
     * If an attribute value is certified, the certificate is verified and stripped
     * from the attribute values in case the signature or the delegation is invalid.
     * 
     * @param requester the role that queries the attribute values or null for hosts.
     * @param requestee the identity whose attribute values are to be returned.
     * @param expiration the time at which the cached attribute values have to be fresh.
     * @param types the types of the attribute values which are to be returned.
     * 
     * @require !Arrays.asList(types).contains(PublicKeyRetrieverImplementation.PUBLIC_KEY_CHAIN) || types.length == 1 : "If the public key chain of a host is queried, it is the only type.";
     * @require for (SemanticType type : types) type != null && type.isAttributeFor(requestee.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given requestee.";
     * 
     * @ensure return.length == types.length : "The returned attribute values are as many as the given types.";
     * @ensure for (i = 0; i < return.length; i++) return[i] == null || return[i].getContent().getType().equals(types[i])) : "Each returned attribute value is either null or matches the corresponding type.";
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull @NullableElements @NonEmpty AttributeValue[] getAttributeValues(@Nullable Role requester, @Nonnull InternalIdentity requestee, @Nonnull @NonNegative Time expiration, @Nonnull @NonNullableElements @NonEmpty SemanticType... types) throws ExternalException {
        Require.that(expiration.isNonNegative()).orThrow("The given time has to be non-negative but was $.", expiration);
        Require.that(types.length > 0).orThrow("At least one type has to be given.");
        Require.that(!Arrays.asList(types).contains(PublicKeyRetrieverImplementation.PUBLIC_KEY_CHAIN) || types.length == 1).orThrow("If the public key chain of a host is queried, it has to be the only type.");
        for (final @Nullable SemanticType type : types) { Require.that(type != null && type.isAttributeFor(requestee.getCategory())).orThrow("Each type has to be non-null and can be used as an attribute for the category of the given requestee."); }
        
        final @Nonnull AttributeValue[] result = new AttributeValue[types.length];
        
        final @Nonnull FreezableAttributeTypeSet typesToRetrieve = FreezableAttributeTypeSet.withNoTypes();
        final @Nonnull List<Integer> indexesToStore = new LinkedList<>();
        for (int i = 0; i < types.length; i++) {
            final @Nonnull Pair<Boolean, AttributeValue> cache = CacheModule.getCachedAttributeValue(requester, requestee, expiration, types[i]);
            if (cache.get0()) {
                result[i] = cache.get1();
            } else {
                typesToRetrieve.add(types[i]);
                indexesToStore.add(i);
            }
        }
        
        if (typesToRetrieve.size() > 0) {
            if (typesToRetrieve.contains(PublicKeyRetrieverImplementation.PUBLIC_KEY_CHAIN)) {
                // TODO: Implement a mechanism where the signature can be verified after the public key chain has been stored.
//                final @Nonnull AttributesReply reply = new Request(requestee.getAddress().getHostIdentifier()).send(false).getReplyNotNull(0);
//                final @Nullable AttributeValue value = reply.getAttributeValues().getNullable(0);
//                setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
//                reply.getSignatureNotNull().verify();
//                result[0] = value;
            } else {
                final @Nonnull AttributesQuery query = AttributesQueryBuilder.withAttributeTypes(typesToRetrieve/* TODO: .freeze() */).withPublished(true).withProvidedEntity(requester).withProvidedSubject(requestee.getAddress()).build();
                final @Nonnull AttributesReply reply = query.send(AttributesReplyConverter.INSTANCE);
                final @Nonnull ReadOnlyList<AttributeValue> values = reply.getAttributeValues();
                if (values.size() != typesToRetrieve.size()) { throw DeclarationExceptionBuilder.withMessage(Strings.format("number of attributes", typesToRetrieve.size(), values.size())).withIdentity(requestee).build(); }
                for (int i = 0; i < values.size(); i++) {
                    final @Nullable AttributeValue value = values.get(i);
                    final @Nonnull SemanticType type = typesToRetrieve.get(i);
                    if (value != null && !value.getContent().getType().equals(type)) { throw DeclarationExceptionBuilder.withMessage(Strings.format("The queried type $ and the replied type $ should be the same.", type.getAddress(), value.getContent().getType().getAddress())).withIdentity(requestee).build(); }
                    CacheModule.setCachedAttributeValue(requester, requestee, getExpiration(type, value, reply), type, value, reply);
                    result[indexesToStore.get(i)] = value;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Returns the attribute value of the given requestee with the given type.
     * If the attribute value is certified, the certificate is verified and stripped
     * from the attribute value in case the signature or the delegation is invalid.
     * 
     * @param requester the role that queries the attribute value or null for hosts.
     * @param requestee the identity whose attribute value is to be returned.
     * @param expiration the time at which the cached attribute value has to be fresh.
     * @param type the type of the attribute value which is to be returned.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * 
     * @require type.isAttributeFor(requestee.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getContent().getType().equals(type)) : "The returned attribute value matches the given type.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull AttributeValue getAttributeValue(@Nullable Role requester, @Nonnull InternalIdentity requestee, @Nonnull @NonNegative Time expiration, @Nonnull @AttributeType SemanticType type) throws ExternalException {
        final @Nonnull AttributeValue[] attributeValues = getAttributeValues(requester, requestee, expiration, type);
        if (attributeValues[0] == null) { throw AttributeNotFoundExceptionBuilder.withIdentity(requestee).withType(type).build(); }
        else { return attributeValues[0]; }
    }
    
    /* -------------------------------------------------- Host Lookup -------------------------------------------------- */
    
    /**
     * Establishes the identity of the given host identifier by checking its existence and requesting its public key chain.
     * (This method is only to be called by the identifier resolver implementation.)
     * 
     * @param identifier the host identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given host identifier.
     * 
     * @require !identifier.isMapped() : "The identifier is not mapped.";
     */
    @NonCommitting
    @PureWithSideEffects
    public static @Nonnull HostIdentity establishHostIdentity(@Nonnull /* @NonMapped */HostIdentifier identifier) throws ExternalException {
        // TODO: Is this still necessary and at the right place?
        
        throw new UnsupportedOperationException();
        
//        Require.that(!identifier.isMapped()).orThrow("The identifier is not mapped.");
//        
//        final @Nonnull HostIdentity identity = Mapper.mapHostIdentity(identifier);
//        final @Nonnull Response response;
//        try {
//            response = new Request(identifier).send(false);
//        } catch (@Nonnull IOException exception) {
//            throw IdentityNotFoundException.get(identifier);
//        }
//        final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
//        final @Nullable AttributeValue value = reply.getAttributeValues().getNullable(0);
//        if (value == null) { throw AttributeNotFoundException.get(identity, PublicKeyChain.TYPE); }
//        if (!value.isCertified()) { throw CertificateNotFoundException.get(identity, PublicKeyChain.TYPE); }
//        setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
//        reply.getSignatureNotNull().verify();
//        return identity;
    }
    
}
