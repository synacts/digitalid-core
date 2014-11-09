package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.contact.AttributeSet;
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
import ch.virtualid.handler.query.external.AttributesQuery;
import ch.virtualid.handler.reply.query.AttributesReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.Request;
import ch.virtualid.packet.Response;
import ch.virtualid.server.Host;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
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

/**
 * The cache caches the {@link Attribute attributes} of {@link Identity identities} for the attribute-specific {@link SemanticType#getCachingPeriod() caching period}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Cache {
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_cache (identity " + Mapper.FORMAT + " NOT NULL, role " + Mapper.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, found BOOLEAN NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + Block.FORMAT + ", reply " + Reply.FORMAT + ", PRIMARY KEY (identity, role, type, found), FOREIGN KEY (identity) " + Mapper.REFERENCE + ", FOREIGN KEY (role) " + Mapper.REFERENCE + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            Database.onInsertUpdate(statement, "general_cache", 4, "identity", "role", "type", "found", "time", "value", "reply");
            Mapper.addReference("general_cache", "identity", "identity", "role", "type", "found");
            Mapper.addReference("general_cache", "role", "identity", "role", "type", "found");
            
            if (getCachedAttribute(HostIdentity.VIRTUALID, null, Time.MIN, PublicKeyChain.TYPE) == null) {
                // Unless it is the root server, the program should have been delivered with the public key chain certificate of 'virtualid.ch'.
                final @Nullable InputStream inputStream = Cache.class.getResourceAsStream("/ch/virtualid/resources/virtualid.ch.certificate.xdf");
                final @Nonnull SignatureWrapper attribute;
                if (inputStream != null) {
                    attribute = SignatureWrapper.decodeUnverified(new SelfcontainedWrapper(inputStream, true).getElement().checkType(Certificate.TYPE), null);
                } else {
                    // Since the public key chain of 'virtualid.ch' is not available, the host 'virtualid.ch' is created on this server.
                    final @Nonnull Host host = new Host(HostIdentifier.VIRTUALID);
                    attribute = new HostSignatureWrapper(Certificate.TYPE, new SelfcontainedWrapper(Attribute.TYPE, host.getPublicKeyChain().toBlock()), HostIdentifier.VIRTUALID, PublicKeyChain.IDENTIFIER);
                    final @Nonnull File certificateFile = new File(Directory.HOSTS.getPath() + Directory.SEPARATOR + "virtualid.ch.certificate.xdf");
                    new SelfcontainedWrapper(SelfcontainedWrapper.SELFCONTAINED, attribute).write(new FileOutputStream(certificateFile), true);
                }
                setCachedAttribute(HostIdentity.VIRTUALID, null, Time.MIN, PublicKeyChain.TYPE, attribute, null);
            }
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            throw new InitializationError("Could not initialize the cache.", exception);
        }
    }
    
    /**
     * Invalidates all the cached attributes of the given identity.
     * 
     * @param identity the identity whose cached attributes are to be invalidated.
     */
    public static void invalidateCachedAttributes(@Nonnull InternalNonHostIdentity identity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull Time time = new Time();
            statement.executeUpdate("UPDATE general_cache SET time = " + time + " WHERE (identity = " + identity + " OR role = " + identity + ") AND time > " + time);
        }
    }
    
    /**
     * Returns the cached attribute with the given type of the given identity.
     * 
     * @param identity the identity whose cached attribute is to be returned.
     * @param role the role that queries the attribute or null for hosts.
     * @param time the time at which the cached attribute has to be fresh.
     * @param type the type of the attribute which is to be returned.
     * 
     * @return a signature of type {@code Certificate.TYPE} if the attribute is cached, a signature of type {@code Packet.SIGNATURE} if its non-availability is cached or {@code null} if neither of the two is cached.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     */
    private static @Nullable SignatureWrapper getCachedAttribute(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
        
        if (time.equals(Time.MAX)) return null;
        final @Nonnull String query = "SELECT found, value FROM general_cache WHERE identity = " + identity + " AND (role = " + role + " OR role = " + HostIdentity.VIRTUALID + ") AND type = " + type + " AND time >= " + time;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            @Nullable SignatureWrapper attribute = null;
            while (resultSet.next()) {
                final boolean found = resultSet.getBoolean(1);
                if (found) attribute = SignatureWrapper.decodeUnverified(Block.get(Certificate.TYPE, resultSet, 2), role);
                else if (attribute == null) attribute = new SignatureWrapper(Packet.SIGNATURE, (Block) null, null);
            }
            return attribute;
        }
    }
    
    /**
     * Sets the cached attribute with the given type of the given identity.
     * 
     * @param identity the identity whose cached attribute is to be set.
     * @param role the role that queried the attribute or null for public.
     * @param time the time at which the cached attribute will expire.
     * @param type the type of the attribute which is to be set.
     * @param attribute the cached attribute which is to be set.
     * @param reply the reply that returned the given attribute.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * @require attribute == null || attribute.isCertificate() : "The attribute is either null or a certificate.";
     */
    private static void setCachedAttribute(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, @Nullable SignatureWrapper attribute, @Nullable Reply reply) throws SQLException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
        assert attribute == null || attribute.isCertificate() : "The attribute is either null or a certificate.";
        
        final @Nonnull String statement = Database.getConfiguration().REPLACE() + " INTO general_cache (identity, role, type, found, time, value, reply) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(statement)) {
            identity.set(preparedStatement, 1);
            if (role != null) role.getIdentity().set(preparedStatement, 2);
            else HostIdentity.VIRTUALID.set(preparedStatement, 2);
            type.set(preparedStatement, 3);
            preparedStatement.setBoolean(4, attribute != null);
            time.set(preparedStatement, 5);
            Block.set(Block.toBlock(attribute), preparedStatement, 6);
            Reply.set(reply, preparedStatement, 7);
            preparedStatement.executeUpdate();
        }
    }
    
    
    /**
     * Returns the attributes of the given identity with the given types.
     * The attributes are returned in the same order as given by the types.
     * If an attribute is not available, the value null is returned instead.
     * If an attribute is certified, the certificate is verified and stripped
     * from the attribute in case the signature or the delegation is invalid.
     * 
     * @param identity the identity whose attributes are to be returned.
     * @param role the role that queries the attributes or null for hosts.
     * @param time the time at which the cached attributes have to be fresh.
     * @param types the types of the attributes which are to be returned.
     * 
     * @return the attributes of the given identity with the given types.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require types.length > 0 : "At least one type is given.";
     * @require !Arrays.asList(types).contains(PublicKeyChain.TYPE) || types.length == 1 : "If the public key chain of a host is queried, it is the only type.";
     * @require for (SemanticType type : types) type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.length == types.length : "The returned attributes are as many as the given types.";
     * @ensure for (SignatureWrapper attribute : return) return == null || return.isCertificate() : "Each returned attribute is either null or a certificate.";
     * @ensure for (i = 0; i < return.length; i++) return[i] == null || new SelfcontainedWrapper(return[i].getElementNotNull()).getElement().getType().equals(types[i])) : "Each returned attribute is either null or matches the corresponding type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper[] getAttributes(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType... types) throws SQLException, IOException, PacketException, ExternalException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert types.length > 0 : "At least one type is given.";
        assert !Arrays.asList(types).contains(PublicKeyChain.TYPE) || types.length == 1 : "If the public key chain of a host is queried, it is the only type.";
        for (final @Nullable SemanticType type : types) assert type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
        
        final @Nonnull SignatureWrapper[] attributes = new SignatureWrapper[types.length];
        final @Nonnull AttributeSet typesToRetrieve = new AttributeSet();
        final @Nonnull List<Integer> indexesToStore = new LinkedList<Integer>();
        for (int i = 0; i < types.length; i++) {
            final @Nullable SignatureWrapper attribute = getCachedAttribute(identity, role, time, types[i]);
            if (attribute == null) {
                typesToRetrieve.add(types[i]);
                indexesToStore.add(i);
            } else if (attribute.getType().equals(Certificate.TYPE)) {
                attributes[i] = attribute;
            }
        }
        
        if (typesToRetrieve.size() > 0) {
            if (typesToRetrieve.contains(PublicKeyChain.TYPE)) {
                final @Nonnull AttributesReply reply = new Request(identity.getAddress().getHostIdentifier()).send(false).getReplyNotNull(0);
                final @Nullable SignatureWrapper attribute = reply.getAttributes().get(0);
                if (attribute != null && attribute.isSigned()) {
                    setCachedAttribute(identity, null, attribute.getTimeNotNull().add(Time.TROPICAL_YEAR), PublicKeyChain.TYPE, attribute, reply);
                    reply.getSignatureNotNull().verify();
                    attributes[0] = attribute;
                }
            } else {
                final @Nonnull Response response = new AttributesQuery(role, identity.getAddress(), typesToRetrieve.freeze(), true).send();
                final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
                final @Nonnull ReadonlyList<SignatureWrapper> certificates = reply.getAttributes();
                final int size = certificates.size();
                if (typesToRetrieve.size() != size) throw new InvalidEncodingException("The number of queried and replied attributes have to be the same.");
                int i = 0;
                for (final @Nonnull SemanticType type : typesToRetrieve) {
                    final @Nullable SignatureWrapper certificate = certificates.get(i);
                    if (certificate != null) {
                        if (!new SelfcontainedWrapper(certificate.getElementNotNull()).getElement().getType().equals(type)) throw new InvalidEncodingException("A replied attribute does not match the queried type.");
                        final @Nonnull Time expiration = type.getCachingPeriodNotNull().add(certificate.isSigned() ? certificate.getTimeNotNull() : reply.getSignatureNotNull().getTimeNotNull());
                        setCachedAttribute(identity, response.getRequest().isSigned() ? role : null, expiration, type, certificate, reply);
                        attributes[indexesToStore.get(i)] = certificate;
                    }
                    i++;
                }
            }
        }
        
        return attributes;
    }
    
    /**
     * Returns the attribute of the given identity with the given type.
     * If the attribute is certified, the certificate is verified and stripped
     * from the attribute in case the signature or the delegation is invalid.
     * 
     * @param identity the identity whose attribute is to be returned.
     * @param role the role that queries the attribute or null for hosts.
     * @param time the time at which the cached attribute has to be fresh.
     * @param type the type of the attribute which is to be returned.
     * 
     * @return the attribute of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.isCertificate() : "The returned attribute is a certificate.";
     * @ensure new SelfcontainedWrapper(return.getElementNotNull()).getElement().getType().equals(type)) : "The returned attribute matches the given type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper getAttribute(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SignatureWrapper[] attributes = getAttributes(identity, role, time, type);
        if (attributes[0] == null) throw new AttributeNotFoundException(identity, type);
        else return attributes[0];
    }
    
    /**
     * Returns the attribute value of the given identity with the given type.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param time the time at which the cached attribute has to be fresh.
     * @param type the type of the attribute value which is to be returned.
     * @param certified whether the attribute value should be certified.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * @throws CertificateNotFoundException if the attribute should be certified but is not.
     * 
     * @require time.isNonNegative() : "The given time is non-negative.";
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public static @Nonnull Block getAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SignatureWrapper attribute = getAttribute(identity, role, time, type);
        if (certified && !attribute.isSigned()) throw new CertificateNotFoundException(identity, type);
        final @Nonnull Block block = new SelfcontainedWrapper(attribute.getElementNotNull()).getElement();
        if (!block.getType().equals(type)) throw new InvalidEncodingException("The returned attribute does not match the queried type.");
        return block;
    }
    
    /**
     * Returns the fresh attribute value of the given identity with the given type.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param type the type of the attribute value which is to be returned.
     * @param certified whether the attribute value should be certified.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * @throws CertificateNotFoundException if the attribute should be certified but is not.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public static @Nonnull Block getFreshAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        return getAttributeValue(identity, role, new Time(), type, certified);
    }
    
    /**
     * Returns the reloaded attribute value of the given identity with the given type.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param type the type of the attribute value which is to be returned.
     * @param certified whether the attribute value should be certified.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * @throws CertificateNotFoundException if the attribute should be certified but is not.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public static @Nonnull Block getReloadedAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        return getAttributeValue(identity, role, Time.MAX, type, certified);
    }
    
    /**
     * Returns the stale attribute value of the given identity with the given type.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param type the type of the attribute value which is to be returned.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @throws AttributeNotFoundException if the attribute is not available.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public static @Nonnull Block getStaleAttributeValue(@Nonnull InternalIdentity identity, @Nullable Role role, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        return getAttributeValue(identity, role, Time.MIN, type, false);
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
        return new PublicKeyChain(getFreshAttributeValue(identity, null, PublicKeyChain.TYPE, true));
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
     * @require !identifier.isMapped() : "The identifier is not mapped.";
     */
    public static @Nonnull HostIdentity establishHostIdentity(@Nonnull HostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        assert !identifier.isMapped() : "The identifier is not mapped.";
        
        final @Nonnull Response response;
        try {
            response = new Request(identifier).send(false);
        } catch (@Nonnull IOException exception) {
            throw new IdentityNotFoundException(identifier);
        }
        final @Nonnull AttributesReply reply = response.getReplyNotNull(0);
        final @Nonnull HostIdentity identity = Mapper.mapHostIdentity(identifier);
        final @Nullable SignatureWrapper certificate = reply.getAttributes().get(0);
        if (certificate == null) throw new AttributeNotFoundException(identity, PublicKeyChain.TYPE);
        if (!certificate.isSigned()) throw new CertificateNotFoundException(identity, PublicKeyChain.TYPE);
        setCachedAttribute(identity, null, certificate.getTimeNotNull().add(Time.TROPICAL_YEAR), PublicKeyChain.TYPE, certificate, reply);
        reply.getSignatureNotNull().verify();
        return identity;
    }
    
}
