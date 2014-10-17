package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.AttributeNotFoundException;
import ch.virtualid.exceptions.external.CertificateNotFoundException;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The cache caches the {@link Attribute attributes} of {@link Identity identities} for the attribute-specific {@link SemanticType#getCachingPeriod() caching period}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.2
 */
public final class Cache {
    
    /**
     * Initializes the cache by creating the corresponding database tables if necessary.
     */
    static {
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
//            // TODO: Leave requester as an idenitity reference, as the role tables are not global but per client.
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cache_attribute (requester BIGINT NOT NULL, requestee BIGINT NOT NULL, type BIGINT NOT NULL, time BIGINT NOT NULL, value LONGBLOB NOT NULL, PRIMARY KEY (requester, requestee, type), FOREIGN KEY (requester) REFERENCES map_identity (identity), FOREIGN KEY (requestee) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
//            connection.commit();
//            
//            // TODO: Load the public key of 'virtualid.ch'.
//            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT identity FROM map_identifier WHERE identifier = 'virtualid.ch'")) {
//                if (!resultSet.next()) {
//                    resultSet.close();
//                    connection.commit();
//                    
//                    // Unless it is the root server, the server should have been delivered with the public key of 'virtualid.ch'.
//                    @Nullable InputStream inputStream = Mapper.class.getResourceAsStream("resources/virtualid.ch.public.xdf");
//                    @Nonnull SelfcontainedWrapper publicKeyWrapper;
//                    if (inputStream != null) {
//                        publicKeyWrapper = SelfcontainedWrapper.readAndClose(inputStream);
//                    } else {
//                        // Since the public key of 'virtualid.ch' is not available, 'virtualid.ch' and 'xdf.ch' are created on this server.
//                        @Nonnull Host virtualidHost = new Host(HostIdentifier.VIRTUALID);
//                        @Nonnull Host xdfHost = new Host(HostIdentifier.XDF);
//                        
//                        publicKeyWrapper = new SelfcontainedWrapper(NonHostIdentifier.HOST_PUBLIC_KEY, virtualidHost.getPublicKey());
//                        
//                        /*
//                        // Initialize the two hosts with the types specified in 'resources/Types.xdf'.
//                        @Nonnull String clientName = "TypeInitializer";
//                        @Nonnull Client client = new Client(clientName);
//                        @Nonnull BigInteger virtualidCommitment = virtualidHost.getPublicKey().getAu().pow(client.getSecret()).getValue();
//                        @Nonnull BigInteger xdfCommitment = xdfHost.getPublicKey().getAu().pow(client.getSecret()).getValue();
//                        
//                        inputStream = Mapper.class.getResourceAsStream("resources/Types.xdf");
//                        if (inputStream == null) throw new InitializationError("Either the public key of 'virtualid.ch' or the specification of the system-relevant types is provided as a resource.");
//                        @Nonnull SelfcontainedWrapper selfcontainedWrapper = SelfcontainedWrapper.readAndClose(inputStream);
//                        @Nonnull List<Block> types = new ListWrapper(selfcontainedWrapper.getElement()).getElements();
//                        
//                        // In a first round, just create the types.
//                        for (@Nonnull Block type : types) {
//                            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(new TupleWrapper(type).getElements(2)[0]);
//                            
//                            if (identifier.getHostIdentifier().equals(HostIdentifier.VIRTUALID)) {
//                                virtualidHost.openAccount(connection, identifier, SEMANTIC_TYPE, virtualidCommitment, clientName);
//                            } else if (identifier.getHostIdentifier().equals(HostIdentifier.XDF)) {
//                                xdfHost.openAccount(connection, identifier, SYNTACTIC_TYPE, xdfCommitment, clientName);
//                            } else {
//                                throw new InitializationError("The type " + identifier + " has a wrong host. Expected: Either 'virtualid.ch' or 'xdf.ch'. Found: " + identifier.getHostIdentifier() + ".");
//                            }
//                        }
//                        
//                        // In a second round, add the attributes to the types.
//                        for (@Nonnull Block type : types) {
//                            @Nonnull Block[] elements = new TupleWrapper(type).getElements(2);
//                            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(elements[0]);
//                            @Nonnull List<Block> attributes = new ListWrapper(elements[1]).getElements();
//                            
//                            for (@Nonnull Block attribute : attributes) {
//                                @Nonnull Block element = SignatureWrapper.decodeUnverified(attribute).getElement();
//                                @Nonnull Host host;
//                                if (identifier.getHostIdentifier().equals(HostIdentifier.VIRTUALID)) {
//                                    host = virtualidHost;
//                                } else if (identifier.getHostIdentifier().equals(HostIdentifier.XDF)) {
//                                    host = xdfHost;
//                                } else {
//                                    throw new InitializationError("The type " + identifier + " has a wrong host. Expected: Either 'virtualid.ch' or 'xdf.ch'. Found: " + identifier.getHostIdentifier() + ".");
//                                }
//                                host.setAttribute(connection, identifier.getIdentity(), new SelfcontainedWrapper(element).getIdentifier().getIdentity().toSemanticType(), attribute, null);
//                            }
//                        }
//                        */
//                    }
//                    
//                    Cache.setAttribute(null, HostIdentity.VIRTUALID, SemanticType.HOST_PUBLIC_KEY, 0l, new SignatureWrapper(publicKeyWrapper, null).toBlock());
//                }
//            }
        } catch (@Nonnull Exception exception) {
            throw new InitializationError("Could not initialize the cache.", exception);
        }
    }
    
//    /**
//     * Returns the attribute with the given type of the given identity from the cache.
//     * 
//     * @param requester the person who requests the attribute or null to indicate an unsigned (and therefore public) request.
//     * @param requestee the identity whose attribute is to be returned.
//     * @param type the semantic type of the requested attribute.
//     * @param time the current time as returned by {@code System.currentTimeMillis()} used to determine whether the attribute is stale (this value can also be set to the past).
//     * @return a block of type {@code attribute@virtualid.ch} or null if no such or only a stale attribute is available.
//     * @require time >= 0 : "The time value is non-negative.";
//     */
//    private static @Nullable Block getAttribute(@Nullable Person requester, @Nonnull Identity requestee, @Nonnull SemanticType type, long time) throws SQLException {
//        assert time >= 0 : "The time value is non-negative.";
//        
//        // TODO: Use a role instead of a person for the requester.
//        
//        @Nullable Block result = null;
//        @Nonnull String query = "SELECT value FROM cache_attribute WHERE (requester = " + (requester == null ? 1 : requester) + " OR requester = 1) AND requestee = " + requestee + "  AND type = " + type + " AND time >= " + time;
//        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) result = new Block(resultSet.getBytes(1));
//            connection.commit();
//        }
//        return result;
//    }
//    
//    /**
//     * Sets the attribute with the given type of the given identity in the cache.
//     * 
//     * @param requester the person who sets the attribute or null to indicate an unsigned (and therefore public) request.
//     * @param requestee the identity whose attribute is to be set.
//     * @param type the semantic type of the attribute to be set.
//     * @param time the time as returned by {@code System.currentTimeMillis()} until which the attribute is considered fresh.
//     * @param attribute a block of type {@code attribute@virtualid.ch}.
//     * @require time >= 0 : "The time value is non-negative.";
//     */
//    private static void setAttribute(@Nullable Person requester, @Nonnull Identity requestee, @Nonnull SemanticType type, long time, @Nonnull Block attribute) throws SQLException {
//        assert time >= 0 : "The time value is non-negative.";
//        
//        @Nonnull String statement = "REPLACE INTO cache_attribute (requester, requestee, type, time, value) VALUES (?, ?, ?, ?, ?)";
//        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, (requester == null ? 1 : requester.getNumber()));
//            preparedStatement.setLong(2, requestee.getNumber());
//            preparedStatement.setLong(3, type.getNumber());
//            preparedStatement.setLong(4, time);
//            Database.setBlock(preparedStatement, 5, attribute);
//            preparedStatement.executeUpdate();
//            connection.commit();
//        }
//    }
    
    
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
     * @require for (SemanticType type : types) type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.length == types.length : "The returned attributes are as many as the given types.";
     * @ensure for (SignatureWrapper attribute : return) return.getType().equals(Certificate.TYPE) : "Each returned attribute has the indicated type.";
     * @ensure for (i = 0; i < return.length; i++) new SelfcontainedWrapper(return[i].getElementNotNull()).getElement().getType().equals(types[i])) : "Each returned attribute matches the corresponding type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper[] getAttributes(@Nonnull Identity identity, @Nullable Role role, @Nonnull Time time, @Nonnull SemanticType... types) throws SQLException, IOException, PacketException, ExternalException {
        assert time.isNonNegative() : "The given time is non-negative.";
        assert types.length > 0 : "At least one type is given.";
        for (final @Nullable SemanticType type : types) assert type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
        
        throw new UnsupportedOperationException("Retrieving attributes is not yet supported!");
//        // TODO (long-term): Verify the new public key of virtualid.ch with the stale one and remove the following line.
//        if (identity.equals(HostIdentity.VIRTUALID)) time = Time.MIN;
//        
//        boolean verification = true;
//        Block[] attributes = new Block[types.length];
//        List<Block> typesToRetrieve = new ArrayList<Block>(types.length);
//        List<Integer> indexesToStore = new ArrayList<Integer>(types.length);
//        for (int i = 0; i < types.length; i++) {
//            if (types[i] == Vid.HOST_PUBLIC_KEY) verification = false;
//            attributes[i] = Cache.getAttribute(requester, requestee, types[i], time);
//            if (attributes[i] == null) {
//                typesToRetrieve.add(new StringWrapper(Mapper.getIdentifier(types[i])).toBlock());
//                indexesToStore.add(i);
//            }
//        }
//        
//        if (typesToRetrieve.size() > 0) {
//            // Attribute requests for hosts are not encrypted.
//            SymmetricKey symmetricKey = null;
//            if (!Category.isHost(requestee)) symmetricKey = new SymmetricKey();
//            
//            // TODO (long-term): Determine how the request is to be signed, i.e. in which context of the requester is the requestee.
//            
//            SelfcontainedWrapper content = new SelfcontainedWrapper("request.get.attribute@virtualid.ch", new ListWrapper(typesToRetrieve).toBlock());
//            Packet response;
//            if (requester == requestee) {
//                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee), 0l, client.getSecret()).send(verification);
//            } else if (requester != 0 && Category.isPerson(requestee)) {
//                AgentPermissions authorization = new AgentPermissions();
//                for (int i : indexesToStore) authorization.put(types[i], false);
//                Credential[] credentials = new Credential[]{client.getCredential(requester, requester, client.getRandomizedAuthorization(requester, requester, authorization))};
//                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee), 0l, credentials, false).send(verification);
//            } else {
//                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee)).send(verification);
//            }
//            
//            List<Block> retrievedAttributes = new ListWrapper(response.getContents().getElement()).getElements();
//            
//            long currentTime = System.currentTimeMillis();
//            long responseTime = responsegetSignaturese().getTime();
//            for (int i = 0; i < retrievedAttributes.size(); i++) {
//                Block declaration = retrievedAttributes.get(i);
//                if (declaration.isNotEmpty()) {
//                    Block[] elements = new TupleWrapper(declaration).getElementsNotNull(2);
//                    Block attribute = elements[0];
//                    
//                    SignatureWrapper certificate = new SignatureWrapper(attribute, true);
//                    long type = Mapper.getVid(new SelfcontainedWrapper(certificate.getElement()).getIdentifier());
//                    if (type != types[indexesToStore.get(i)]) throw new Exception("Request: The host delivered the requested attributes in a wrong order.");
//                    long caching = getCachingPeriod(type);
//                    
//                    // Verify that the signature is still valid or remove it otherwise.
//                    long retrievalTime = responseTime;
//                    if (certificate.getSigner() == null) {
//                        if (certificate.isSigned()) throw new Exception("Request: Attributes may only be certified by hosts.");
//                        if (type == Vid.HOST_PUBLIC_KEY && requestee != Vid.VIRTUALID) throw new Exception("Request: The public key is certified.");
//                    } else {
//                        long certificateTime = certificate.getTime();
//                        if (Mapper.getVid(certificate.getIdentifier()) == requestee && certificateTime + caching > currentTime && isAuthorized(certificate.getSigner(), certificate.getElement())) {
//                            retrievalTime = certificateTime;
//                        } else {
//                            if (type == Vid.HOST_PUBLIC_KEY) throw new Exception("Request: The certificate of the public key is invalid.");
//                            attribute = new SignatureWrapper(certificate.getElement()).getBlock();
//                        }
//                    }
//                    
//                    attributes[indexesToStore.get(i)] = attribute;
//                    Cache.setAttribute(requester, requestee, type, retrievalTime + caching, attribute);
//                }
//            }
//            
//            if (!verification) responsgetSignaturesre().verify();
//        }
//        
//        return attributes;
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
     * @param types the types of the attributes which are to be returned.
     * 
     * @return the attributes of the given identity with the given types.
     * 
     * @require types.length > 0 : "At least one type is given.";
     * @require for (SemanticType type : types) type != null && type.isAttributeFor(identity.getCategory()) : "Each type is not null and can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.length == types.length : "The returned attributes are as many as the given types.";
     * @ensure for (SignatureWrapper attribute : return) return.getType().equals(Certificate.TYPE) : "Each returned attribute has the indicated type.";
     * @ensure for (i = 0; i < return.length; i++) new SelfcontainedWrapper(return[i].getElementNotNull()).getElement().getType().equals(types[i])) : "Each returned attribute matches the corresponding type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper[] getAttributes(@Nonnull Identity identity, @Nullable Role role, @Nonnull SemanticType... types) throws SQLException, IOException, PacketException, ExternalException {
        return getAttributes(identity, role, new Time(), types);
    }
    
    /**
     * Returns the attribute of the given identity with the given type.
     * If the attribute is not available, a {@link AttributeNotFoundException} is thrown instead.
     * If the attribute is certified, the certificate is verified and stripped
     * from the attribute in case the signature or the delegation is invalid.
     * 
     * @param identity the identity whose attribute is to be returned.
     * @param role the role that queries the attribute or null for hosts.
     * @param type the type of the attribute which is to be returned.
     * 
     * @return the attribute of the given identity with the given type.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(Certificate.TYPE) : "The returned attribute has the indicated type.";
     * @ensure new SelfcontainedWrapper(return.getElementNotNull()).getElement().getType().equals(type)) : "The returned attribute matches the given type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper getAttribute(@Nonnull Identity identity, @Nullable Role role, @Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SignatureWrapper[] attributes = getAttributes(identity, role, type);
        if (attributes[0] == null) throw new AttributeNotFoundException(identity, type);
        else return attributes[0];
    }
    
    /**
     * Returns the attribute value of the given identity with the given type.
     * If the attribute is not available, a {@link AttributeNotFoundException} is thrown.
     * If the attribute should be certified but is not, a {@link CertificateNotFoundException} is thrown.
     * 
     * @param identity the identity whose attribute value is to be returned.
     * @param role the role that queries the attribute value or null for hosts.
     * @param type the type of the attribute value which is to be returned.
     * @param certified whether the attribute value should be certified.
     * 
     * @return the attribute value of the given identity with the given type.
     * 
     * @require type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public static @Nonnull Block getAttributeValue(@Nonnull Identity identity, @Nullable Role role, @Nonnull SemanticType type, boolean certified) throws SQLException, IOException, PacketException, ExternalException {
        assert type.isAttributeFor(identity.getCategory()) : "The type can be used as an attribute for the category of the given identity.";
        
        final @Nonnull SignatureWrapper attribute = getAttribute(identity, role, type);
        if (certified && !(attribute instanceof HostSignatureWrapper)) throw new CertificateNotFoundException(identity, type);
        final @Nonnull Block block = new SelfcontainedWrapper(attribute.getElementNotNull()).getElement();
        if (!block.getType().equals(type)) throw new InvalidEncodingException("The returned attribute does not match the queried type.");
        return block;
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
        return new PublicKeyChain(getAttributeValue(identity, null, PublicKeyChain.TYPE, true));
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
    
}
