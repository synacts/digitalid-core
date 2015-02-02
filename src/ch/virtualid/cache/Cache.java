package ch.virtualid.cache;

import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.contact.AttributeTypeSet;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.AttributeNotFoundException;
import ch.virtualid.exceptions.external.CertificateNotFoundException;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.host.Host;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.virtualid.packet.Request;
import ch.virtualid.packet.Response;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
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
import org.javatuples.Pair;

/**
 * This class caches the {@link AttributeValue attribute values} of {@link Identity identities} for the attribute-specific {@link SemanticType#getCachingPeriod() caching period}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Cache {
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_cache (identity " + Mapper.FORMAT + " NOT NULL, role " + Mapper.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, found BOOLEAN NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + AttributeValue.FORMAT + ", reply " + Reply.FORMAT + ", PRIMARY KEY (identity, role, type, found), FOREIGN KEY (identity) " + Mapper.REFERENCE + ", FOREIGN KEY (role) " + Mapper.REFERENCE + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            Database.onInsertUpdate(statement, "general_cache", 4, "identity", "role", "type", "found", "time", "value", "reply");
            Mapper.addReference("general_cache", "identity", "identity", "role", "type", "found");
            Mapper.addReference("general_cache", "role", "identity", "role", "type", "found");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("Could not initialize the cache.", exception);
        }
    }
    
    /**
     * Initializes the cache with the public key of {@code virtualid.ch}.
     * 
     * @require Database.isMainThread() : "This method is called in the main thread.";
     */
    public static void initialize() {
        assert Database.isMainThread() : "This method is called in the main thread.";
        
        try {
            if (!getCachedAttributeValue(HostIdentity.VIRTUALID, null, Time.MIN, PublicKeyChain.TYPE).getValue0()) {
                // Unless it is the root server, the program should have been delivered with the public key chain certificate of 'virtualid.ch'.
                final @Nullable InputStream inputStream = Cache.class.getResourceAsStream("/ch/virtualid/resources/virtualid.ch.certificate.xdf");
                final @Nonnull AttributeValue value;
                if (inputStream != null) {
                    value = AttributeValue.get(new SelfcontainedWrapper(inputStream, true).getElement().checkType(AttributeValue.TYPE), true);
                } else {
                    // Since the public key chain of 'virtualid.ch' is not available, the host 'virtualid.ch' is created on this server.
                    final @Nonnull Host host = new Host(HostIdentifier.VIRTUALID);
                    value = new CertifiedAttributeValue(host.getPublicKeyChain(), HostIdentity.VIRTUALID, PublicKeyChain.TYPE);
                    final @Nonnull File certificateFile = new File(Directory.HOSTS.getPath() + Directory.SEPARATOR + "virtualid.ch.certificate.xdf");
                    new SelfcontainedWrapper(SelfcontainedWrapper.SELFCONTAINED, value).write(new FileOutputStream(certificateFile), true);
                }
                setCachedAttributeValue(HostIdentity.VIRTUALID, null, Time.MIN, PublicKeyChain.TYPE, value, null);
            }
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            throw new InitializationError("Could not initialize the cache.", exception);
        }
    }
    
    /**
     * Invalidates all the cached attribute values of the given identity.
     * 
     * @param identity the identity whose cached attribute values are to be invalidated.
     */
    public static void invalidateCachedAttributeValues(@Nonnull InternalNonHostIdentity identity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull Time time = new Time();
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
    private static @Nonnull Pair<Boolean, AttributeValue> getCachedAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
        
        if (time.equals(Time.MAX)) return new Pair<Boolean, AttributeValue>(false, null);
        final @Nonnull String query = "SELECT found, value FROM general_cache WHERE identity = " + identity + " AND (role = " + HostIdentity.VIRTUALID + (role != null ? " OR role = " + role.getIdentity() : "") + ") AND type = " + type + " AND time >= " + time;
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
            return new Pair<Boolean, AttributeValue>(found, value);
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
    private static void setCachedAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, @Nullable AttributeValue value, @Nullable Reply reply) throws SQLException, InvalidEncodingException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
        assert value == null || value.isVerified() : "The attribute value is null or its signature is verified.";
        
        if (value != null) value.checkContentType(type);
        
        final @Nonnull String SQL = Database.getConfiguration().REPLACE() + " INTO general_cache (identity, role, type, found, time, value, reply) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            identity.set(preparedStatement, 1);
            if (role != null) role.getIdentity().set(preparedStatement, 2);
            else HostIdentity.VIRTUALID.set(preparedStatement, 2);
            type.set(preparedStatement, 3);
            preparedStatement.setBoolean(4, value != null);
            time.set(preparedStatement, 5);
            AttributeValue.set(value, preparedStatement, 6);
            Reply.set(reply, preparedStatement, 7);
            preparedStatement.executeUpdate();
        }
    }
    
    
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
        if (value != null && !value.getContent().getType().equals(type)) throw new InvalidEncodingException("A replied attribute value does not match the queried type.");
        return type.getCachingPeriodNotNull().add(value instanceof CertifiedAttributeValue ? ((CertifiedAttributeValue) value).getTime() : reply.getSignatureNotNull().getTimeNotNull());
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
    public static @Nonnull AttributeValue[] getAttributeValues(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType... types) throws SQLException, IOException, PacketException, ExternalException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert types.length > 0 : "At least one type is given.";
        assert !Arrays.asList(types).contains(PublicKeyChain.TYPE) || types.length == 1 : "If the public key chain of a host is queried, it is the only type.";
        for (final @Nullable SemanticType type : types) assert type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
        
        final @Nonnull AttributeValue[] attributeValues = new AttributeValue[types.length];
        final @Nonnull AttributeTypeSet typesToRetrieve = new AttributeTypeSet();
        final @Nonnull List<Integer> indexesToStore = new LinkedList<Integer>();
        for (int i = 0; i < types.length; i++) {
            final @Nonnull Pair<Boolean, AttributeValue> cache = getCachedAttributeValue(identity, role, time, types[i]);
            if (cache.getValue0()) {
                attributeValues[i] = cache.getValue1();
            } else {
                typesToRetrieve.add(types[i]);
                indexesToStore.add(i);
            }
        }
        
        if (typesToRetrieve.size() > 0) {
            if (typesToRetrieve.contains(PublicKeyChain.TYPE)) {
                final @Nonnull AttributesReply reply = new Request(identity.getAddress().getHostIdentifier()).send(false).getReplyNotNull(0);
                final @Nullable AttributeValue value = reply.getAttributeValues().get(0);
                setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
                reply.getSignatureNotNull().verify();
                attributeValues[0] = value;
            } else {
                final @Nonnull Response response = new AttributesQuery(role, identity.getAddress(), typesToRetrieve.freeze(), true).send();
                final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
                final @Nonnull ReadonlyList<AttributeValue> values = reply.getAttributeValues();
                if (values.size() != typesToRetrieve.size()) throw new InvalidEncodingException("The number of queried and replied attributes have to be the same.");
                int i = 0;
                for (final @Nonnull SemanticType type : typesToRetrieve) {
                    final @Nullable AttributeValue value = values.get(i);
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
    public static @Nonnull AttributeValue getAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AttributeValue[] attributeValues = getAttributeValues(identity, role, time, type);
        if (attributeValues[0] == null) throw new AttributeNotFoundException(identity, type);
        else return attributeValues[0];
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
    public static @Nonnull Block getAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AttributeValue value = getAttributeValue(identity, role, time, type);
        if (certified && !value.isCertified()) throw new CertificateNotFoundException(identity, type);
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
    public static @Nonnull Block getFreshAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        return getAttributeContent(identity, role, new Time(), type, certified);
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
    public static @Nonnull Block getReloadedAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
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
    public static @Nonnull Block getStaleAttributeContent(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
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
    public static @Nonnull PublicKeyChain getPublicKeyChain(@Nonnull HostIdentity identity) throws SQLException, IOException, PacketException, ExternalException {
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
    public static @Nonnull PublicKey getPublicKey(@Nonnull HostIdentifier identifier, @Nonnull Time time) throws SQLException, IOException, PacketException, ExternalException {
        return getPublicKeyChain(identifier.getIdentity()).getKey(time);
    }
    
    
    /**
     * Establishes the identity of the given host identifier by checking its existence and requesting its public key chain.
     * 
     * @param identifier the host identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given host identifier.
     * 
     * @require identifier.isNotMapped() : "The identifier is not mapped.";
     */
    public static @Nonnull HostIdentity establishHostIdentity(@Nonnull HostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        assert identifier.isNotMapped() : "The identifier is not mapped.";
        
        final @Nonnull Response response;
        try {
            response = new Request(identifier).send(false);
        } catch (@Nonnull IOException exception) {
            throw new IdentityNotFoundException(identifier);
        }
        final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
        final @Nonnull HostIdentity identity = Mapper.mapHostIdentity(identifier);
        final @Nullable AttributeValue value = reply.getAttributeValues().get(0);
        if (value == null) throw new AttributeNotFoundException(identity, PublicKeyChain.TYPE);
        if (!value.isCertified()) throw new CertificateNotFoundException(identity, PublicKeyChain.TYPE);
        setCachedAttributeValue(identity, null, getExpiration(PublicKeyChain.TYPE, value, reply), PublicKeyChain.TYPE, value, reply);
        reply.getSignatureNotNull().verify();
        return identity;
    }
    
}
