package net.digitalid.core.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.directory.Directory;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.cache.exceptions.AttributeNotFoundException;
import net.digitalid.core.cache.exceptions.CertificateNotFoundException;
import net.digitalid.core.identification.exceptions.IdentityNotFoundException;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.exceptions.InvalidReplyParameterValueException;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.attribute.CertifiedAttributeValue;
import net.digitalid.service.core.concepts.contact.FreezableAttributeTypeSet;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.cryptography.PublicKeyChain;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.identity.resolution.annotations.NonMapped;
import net.digitalid.service.core.packet.Request;
import net.digitalid.service.core.packet.Response;
import net.digitalid.service.core.site.host.Host;

/**
 * This class caches the {@link AttributeValue attribute values} of {@link Identity identities} for the attribute-specific {@link SemanticType#getCachingPeriod() caching period}.
 */
@Stateless
public final class Cache {
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    static {
        Require.that(Threading.isMainThread()).orThrow("This static block is called in the main thread.");
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_cache (identity " + Mapper.FORMAT + " NOT NULL, role " + Mapper.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, found BOOLEAN NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + AttributeValue.FORMAT + ", reply " + Reply.FORMAT + ", PRIMARY KEY (identity, role, type, found), FOREIGN KEY (identity) " + Mapper.REFERENCE + ", FOREIGN KEY (role) " + Mapper.REFERENCE + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            Database.onInsertUpdate(statement, "general_cache", 4, "identity", "role", "type", "found", "time", "value", "reply");
            Mapper.addReference("general_cache", "identity", "identity", "role", "type", "found");
            Mapper.addReference("general_cache", "role", "identity", "role", "type", "found");
        } catch (@Nonnull SQLException exception) {
            throw InitializationError.get("Could not initialize the cache.", exception);
        }
    }
    
    /**
     * Initializes the cache with the public key of {@code digitalid.net}.
     * 
     * @require Threading.isMainThread() : "This method is called in the main thread.";
     */
    @Committing
    public static void initialize() {
        Require.that(Threading.isMainThread()).orThrow("This method is called in the main thread.");
        
        try {
            Database.lock();
            if (!getCachedAttributeValue(HostIdentity.DIGITALID, null, Time.MIN, PublicKeyChain.TYPE).getElement0()) {
                // Unless it is the root server, the program should have been delivered with the public key chain certificate of 'core.digitalid.net'.
                final @Nullable InputStream inputStream = Cache.class.getResourceAsStream("/net/digitalid/core/resources/core.digitalid.net.certificate.xdf");
                final @Nonnull AttributeValue value;
                if (inputStream != null) {
                    value = AttributeValue.get(SelfcontainedWrapper.decodeBlockFrom(inputStream, true).checkType(AttributeValue.TYPE), true);
                    Log.information("The public key chain of the root host was loaded from the provided resources.");
                } else {
                    // Since the public key chain of 'core.digitalid.net' is not available, the host 'core.digitalid.net' is created on this server.
                    final @Nonnull Host host = new Host(HostIdentifier.DIGITALID);
                    value = new CertifiedAttributeValue(host.getPublicKeyChain(), HostIdentity.DIGITALID, PublicKeyChain.TYPE);
                    final @Nonnull File certificateFile = new File(Directory.getHostsDirectory().getPath() + "/core.digitalid.net.certificate.xdf");
                    SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, value).writeTo(new FileOutputStream(certificateFile), true);
                    Log.warning("The public key chain of the root host was not found and thus 'core.digitalid.net' was created on this machine.");
                }
                setCachedAttributeValue(HostIdentity.DIGITALID, null, Time.MIN, PublicKeyChain.TYPE, value, null);
            }
            Database.commit();
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            throw InitializationError.get("Could not initialize the cache.", exception);
        } finally {
            Database.unlock();
        }
    }
    
    /* -------------------------------------------------- Database Access -------------------------------------------------- */
    
    /**
     * Invalidates all the cached attribute values of the given identity.
     * 
     * @param identity the identity whose cached attribute values are to be invalidated.
     */
    @Locked
    @NonCommitting
    public static void invalidateCachedAttributeValues(@Nonnull InternalNonHostIdentity identity) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull Time time = Time.getCurrent();
            statement.executeUpdate("UPDATE general_cache SET time = " + time + " WHERE (identity = " + identity + " OR role = " + identity + ") AND time > " + time);
        }
    }
    
    /**
     * Returns the cached attribute value with the given type of the given identity.
     * 
     * @param identity the identity whose cached attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param time the time at which the cached attribute value has to be fresh.
     * @param type the type of the attribute value which is to be returned.
     * 
     * @return a pair of a boolean indicating whether the attribute value of the given type is cached and the value being cached or null if it is not available.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getValue1() == null || return.getValue1().getContent().getType().equals(type) : "The content of the returned attribute value is null or matches the given type.";
     */
    @Locked
    @NonCommitting
    private static @Nonnull @Frozen ReadOnlyPair<Boolean, AttributeValue> getCachedAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws ExternalException {
        Require.that(time.isNonNegative()).orThrow("The given time is non-negative.");
        Require.that(type.isAttributeFor(identity.getCategory())).orThrow("The type can be used as an attribute for the category of the given identity.");
        
        if (time.equals(Time.MAX)) { return new FreezablePair<Boolean, AttributeValue>(false, null).freeze(); }
        final @Nonnull String query = "SELECT found, value FROM general_cache WHERE identity = " + identity + " AND (role = " + HostIdentity.DIGITALID + (role != null ? " OR role = " + role.getIdentity() : "") + ") AND type = " + type + " AND time >= " + time;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            boolean found = false;
            @Nullable AttributeValue value = null;
            while (resultSet.next()) {
                found = true;
                if (resultSet.getBoolean(1)) {
                    value = AttributeValue.get(resultSet, 2).checkContentType(type);
                    break;
                }
            }
            return new FreezablePair<>(found, value).freeze();
        }
    }
    
    /**
     * Sets the cached attribute value with the given type of the given identity.
     * 
     * @param identity the identity whose cached attribute value is to be set.
     * @param role the role that queried the attribute value or null for public.
     * @param time the time at which the cached attribute value will expire.
     * @param type the type of the attribute value which is to be set.
     * @param value the cached attribute value which is to be set.
     * @param reply the reply that returned the given attribute value.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * @require value == null || value.isVerified() : "The attribute value is null or its signature is verified.";
     * 
     * @ensure value == null || value.getContent().getType().equals(type) : "The content of the given attribute value is null or matches the given type.";
     */
    @Locked
    @NonCommitting
    private static void setCachedAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, @Nullable AttributeValue value, @Nullable Reply reply) throws DatabaseException, InvalidReplyParameterValueException {
        Require.that(time.isNonNegative()).orThrow("The given time is non-negative.");
        Require.that(type.isAttributeFor(identity.getCategory())).orThrow("The type can be used as an attribute for the category of the given identity.");
        Require.that(value == null || value.isVerified()).orThrow("The attribute value is null or its signature is verified.");
        
        if (value != null) { value.checkContentType(type); }
        
        final @Nonnull String SQL = Database.getConfiguration().REPLACE() + " INTO general_cache (identity, role, type, found, time, value, reply) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            identity.set(preparedStatement, 1);
            if (role != null) { role.getIdentity().set(preparedStatement, 2); }
            else { HostIdentity.DIGITALID.set(preparedStatement, 2); }
            type.set(preparedStatement, 3);
            preparedStatement.setBoolean(4, value != null);
            time.set(preparedStatement, 5);
            AttributeValue.set(value, preparedStatement, 6);
            Reply.set(reply, preparedStatement, 7);
            preparedStatement.executeUpdate();
        }
    }
    
    /* -------------------------------------------------- Attribute Retrieval -------------------------------------------------- */
    
    /**
     * Returns the expiration time of the given attribute value.
     * 
     * @param type the type that was queried.
     * @param value the value that was replied.
     * @param reply the reply containing the value.
     * 
     * @return the expiration time of the given attribute value.
     */
    @Pure
    private static @Nonnull Time getExpiration(@Nonnull SemanticType type, @Nullable AttributeValue value, @Nonnull AttributesReply reply) throws InvalidEncodingException {
        if (value != null && !value.getContent().getType().equals(type)) { throw InvalidReplyParameterValueException.get(reply, "attribute type", type.getAddress(), value.getContent().getType().getAddress()); }
        return type.getCachingPeriodNotNull().add(value instanceof CertifiedAttributeValue ? ((CertifiedAttributeValue) value).getTime() : reply.getSignatureNotNull().getNonNullableTime());
    }
    
    /**
     * Returns the attribute values of the given identity with the given types.
     * The attribute values are returned in the same order as given by the types.
     * If an attribute value is not available, the value null is returned instead.
     * If an attribute value is certified, the certificate is verified and stripped
     * from the attribute values in case the signature or the delegation is invalid.
     * 
     * @param identity the identity whose attribute values are to be returned.
     * @param role the role that queries the attribute values or null for hosts.
     * @param time the time at which the cached attribute values have to be fresh.
     * @param types the types of the attribute values which are to be returned.
     * 
     * @return the attribute values of the given identity with the given types.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require types.length > 0 : "At least one type is given.";
     * @require !Arrays.asList(types).contains(PublicKeyChain.TYPE) || types.length == 1 : "If the public key chain of a host is queried, it is the only type.";
     * @require for (SemanticType type : types) type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.length == types.length : "The returned attribute values are as many as the given types.";
     * @ensure for (i = 0; i < return.length; i++) return[i] == null || return[i].getContent().getType().equals(types[i])) : "Each returned attribute value is either null or matches the corresponding type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull AttributeValue[] getAttributeValues(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType... types) throws ExternalException {
        Require.that(time.isNonNegative()).orThrow("The given time is non-negative.");
        Require.that(types.length > 0).orThrow("At least one type is given.");
        Require.that(!Arrays.asList(types).contains(PublicKeyChain.TYPE) || types.length == 1).orThrow("If the public key chain of a host is queried, it is the only type.");
        for (final @Nullable SemanticType type : types) { Require.that(type != null && type.isAttributeFor(identity.getCategory())).orThrow("Each type is not null and can be used as an attribute for the category of the given identity."); }
        
        final @Nonnull AttributeValue[] attributeValues = new AttributeValue[types.length];
        final @Nonnull FreezableAttributeTypeSet typesToRetrieve = new FreezableAttributeTypeSet();
        final @Nonnull List<Integer> indexesToStore = new LinkedList<>();
        for (int i = 0; i < types.length; i++) {
            final @Nonnull @Frozen ReadOnlyPair<Boolean, AttributeValue> cache = getCachedAttributeValue(identity, role, time, types[i]);
            if (cache.getElement0()) {
                attributeValues[i] = cache.getElement1();
            } else {
                typesToRetrieve.add(types[i]);
                indexesToStore.add(i);
            }
        }
        
        if (typesToRetrieve.size() > 0) {
            if (typesToRetrieve.contains(PublicKeyChain.TYPE)) {
                final @Nonnull AttributesReply reply = new Request(identity.getAddress().getHostIdentifier()).send(false).getReplyNotNull(0);
                final @Nullable AttributeValue value = reply.getAttributeValues().getNullable(0);
                setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
                reply.getSignatureNotNull().verify();
                attributeValues[0] = value;
            } else {
                final @Nonnull Response response = new AttributesQuery(role, identity.getAddress(), typesToRetrieve.freeze(), true).send();
                final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
                final @Nonnull ReadOnlyList<AttributeValue> values = reply.getAttributeValues();
                if (values.size() != typesToRetrieve.size()) { throw InvalidReplyParameterValueException.get(reply, "number of attributes", typesToRetrieve.size(), values.size()); }
                int i = 0;
                for (final @Nonnull SemanticType type : typesToRetrieve) {
                    final @Nullable AttributeValue value = values.getNullable(i);
                    setCachedAttributeValue(identity, response.getRequest().isSigned() ? role : null, getExpiration(type, value, reply), type, value, reply);
                    attributeValues[indexesToStore.get(i)] = value;
                    i++;
                }
            }
        }
        
        return attributeValues;
    }
    
    /**
     * Returns the attribute value of the given identity with the given type.
     * If the attribute value is certified, the certificate is verified and stripped
     * from the attribute value in case the signature or the delegation is invalid.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param time the time at which the cached attribute value has to be fresh.
     * @param type the type of the attribute value which is to be returned.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getContent().getType().equals(type)) : "The returned attribute value matches the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull AttributeValue getAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws ExternalException {
        final @Nonnull AttributeValue[] attributeValues = getAttributeValues(identity, role, time, type);
        if (attributeValues[0] == null) { throw AttributeNotFoundException.get(identity, type); }
        else { return attributeValues[0]; }
    }
    
    /**
     * Returns the attribute content of the given identity with the given type.
     * 
     * @param identity the identity whose attribute content is to be returned.
     * @param role the role that queries the attribute content or null for hosts.
     * @param time the time at which the cached attribute content has to be fresh.
     * @param type the type of the attribute content which is to be returned.
     * @param certified whether the attribute content should be certified.
     * 
     * @return the attribute content of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute value is not available.
     * @throws CertificateNotFoundException if the value should be certified but is not.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned content has the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Block getAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, boolean certified) throws ExternalException {
        final @Nonnull AttributeValue value = getAttributeValue(identity, role, time, type);
        if (certified && !value.isCertified()) { throw CertificateNotFoundException.get(identity, type); }
        return value.getContent();
    }
    
    /**
     * Returns the fresh attribute content of the given identity with the given type.
     * 
     * @param identity the identity whose attribute content is to be returned.
     * @param role the role that queries the attribute content or null for hosts.
     * @param type the type of the attribute content which is to be returned.
     * @param certified whether the attribute content should be certified.
     * 
     * @return the attribute content of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute value is not available.
     * @throws CertificateNotFoundException if the value should be certified but is not.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned content has the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Block getFreshAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws ExternalException {
        return getAttributeContent(identity, role, Time.getCurrent(), type, certified);
    }
    
    /**
     * Returns the reloaded attribute content of the given identity with the given type.
     * 
     * @param identity the identity whose attribute content is to be returned.
     * @param role the role that queries the attribute content or null for hosts.
     * @param type the type of the attribute content which is to be returned.
     * @param certified whether the attribute content should be certified.
     * 
     * @return the attribute content of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute value is not available.
     * @throws CertificateNotFoundException if the value should be certified but is not.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned content has the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Block getReloadedAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws ExternalException {
        return getAttributeContent(identity, role, Time.MAX, type, certified);
    }
    
    /**
     * Returns the stale attribute content of the given identity with the given type.
     * 
     * @param identity the identity whose attribute content is to be returned.
     * @param role the role that queries the attribute content or null for hosts.
     * @param type the type of the attribute content which is to be returned.
     * 
     * @return the attribute content of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute value is not available.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned content has the given type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Block getStaleAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type) throws ExternalException {
        return getAttributeContent(identity, role, Time.MIN, type, false);
    }
    
    /**
     * Returns the public key chain of the given identity.
     * 
     * @param identity the identity of the host whose public key chain is to be returned.
     * 
     * @return the public of key chain the given identity.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull PublicKeyChain getPublicKeyChain(@Nonnull HostIdentity identity) throws ExternalException {
        return new PublicKeyChain(getFreshAttributeContent(identity, null, PublicKeyChain.TYPE, true));
    }
    
    /**
     * Returns the public key of the given host identifier at the given time.
     * 
     * @param identifier the identifier of the host whose public key is to be returned.
     * @param time the time at which the public key has to be active in the key chain.
     * 
     * @return the public key of the given host identifier at the given time.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull PublicKey getPublicKey(@Nonnull HostIdentifier identifier, @Nonnull Time time) throws ExternalException {
        return getPublicKeyChain(identifier.getIdentity()).getKey(time);
    }
    
    /* -------------------------------------------------- Host Lookup -------------------------------------------------- */
    
    /**
     * Establishes the identity of the given host identifier by checking its existence and requesting its public key chain.
     * (This method is only to be called by {@link Mapper#getIdentity(net.digitalid.service.core.identifier.Identifier)}.)
     * 
     * @param identifier the host identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given host identifier.
     * 
     * @require !identifier.isMapped() : "The identifier is not mapped.";
     */
    @Locked
    @NonCommitting
    public static @Nonnull HostIdentity establishHostIdentity(@Nonnull @NonMapped HostIdentifier identifier) throws ExternalException {
        Require.that(!identifier.isMapped()).orThrow("The identifier is not mapped.");
        
        final @Nonnull HostIdentity identity = Mapper.mapHostIdentity(identifier);
        final @Nonnull Response response;
        try {
            response = new Request(identifier).send(false);
        } catch (@Nonnull IOException exception) {
            throw IdentityNotFoundException.get(identifier);
        }
        final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
        final @Nullable AttributeValue value = reply.getAttributeValues().getNullable(0);
        if (value == null) { throw AttributeNotFoundException.get(identity, PublicKeyChain.TYPE); }
        if (!value.isCertified()) { throw CertificateNotFoundException.get(identity, PublicKeyChain.TYPE); }
        setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
        reply.getSignatureNotNull().verify();
        return identity;
    }
    
}
