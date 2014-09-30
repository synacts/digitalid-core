package ch.virtualid.client;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InitializationError;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.FailedRequestException;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The cache caches the attributes of virtual identities for the attribute-specific caching period.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Cache {
    
    /**
     * Stores the semantic type {@code outgoing.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = mapSemanticType(NonHostIdentifier.OUTGOING_DELEGATIONS);
    
    /**
     * Stores the semantic type {@code incoming.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = mapSemanticType(NonHostIdentifier.INCOMING_DELEGATIONS);
    
    /**
     * Initializes the cache by creating the corresponding database tables if necessary.
     */
    static {
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            // TODO: Leave requester as an idenitity reference, as the role tables are not global but per client.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cache_attribute (requester BIGINT NOT NULL, requestee BIGINT NOT NULL, type BIGINT NOT NULL, time BIGINT NOT NULL, value LONGBLOB NOT NULL, PRIMARY KEY (requester, requestee, type), FOREIGN KEY (requester) REFERENCES map_identity (identity), FOREIGN KEY (requestee) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            connection.commit();
            
            // TODO: Load the public key of 'virtualid.ch'.
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT identity FROM map_identifier WHERE identifier = 'virtualid.ch'")) {
                if (!resultSet.next()) {
                    resultSet.close();
                    connection.commit();
                    
                    // Unless it is the root server, the server should have been delivered with the public key of 'virtualid.ch'.
                    @Nullable InputStream inputStream = Mapper.class.getResourceAsStream("resources/virtualid.ch.public.xdf");
                    @Nonnull SelfcontainedWrapper publicKeyWrapper;
                    if (inputStream != null) {
                        publicKeyWrapper = SelfcontainedWrapper.readAndClose(inputStream);
                    } else {
                        // Since the public key of 'virtualid.ch' is not available, 'virtualid.ch' and 'xdf.ch' are created on this server.
                        @Nonnull Host virtualidHost = new Host(HostIdentifier.VIRTUALID);
                        @Nonnull Host xdfHost = new Host(HostIdentifier.XDF);
                        
                        publicKeyWrapper = new SelfcontainedWrapper(NonHostIdentifier.HOST_PUBLIC_KEY, virtualidHost.getPublicKey());
                        
                        /*
                        // Initialize the two hosts with the types specified in 'resources/Types.xdf'.
                        @Nonnull String clientName = "TypeInitializer";
                        @Nonnull Client client = new Client(clientName);
                        @Nonnull BigInteger virtualidCommitment = virtualidHost.getPublicKey().getAu().pow(client.getSecret()).getValue();
                        @Nonnull BigInteger xdfCommitment = xdfHost.getPublicKey().getAu().pow(client.getSecret()).getValue();
                        
                        inputStream = Mapper.class.getResourceAsStream("resources/Types.xdf");
                        if (inputStream == null) throw new InitializationError("Either the public key of 'virtualid.ch' or the specification of the system-relevant types is provided as a resource.");
                        @Nonnull SelfcontainedWrapper selfcontainedWrapper = SelfcontainedWrapper.readAndClose(inputStream);
                        @Nonnull List<Block> types = new ListWrapper(selfcontainedWrapper.getElement()).getElements();
                        
                        // In a first round, just create the types.
                        for (@Nonnull Block type : types) {
                            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(new TupleWrapper(type).getElements(2)[0]);
                            
                            if (identifier.getHostIdentifier().equals(HostIdentifier.VIRTUALID)) {
                                virtualidHost.openAccount(connection, identifier, SEMANTIC_TYPE, virtualidCommitment, clientName);
                            } else if (identifier.getHostIdentifier().equals(HostIdentifier.XDF)) {
                                xdfHost.openAccount(connection, identifier, SYNTACTIC_TYPE, xdfCommitment, clientName);
                            } else {
                                throw new InitializationError("The type " + identifier + " has a wrong host. Expected: Either 'virtualid.ch' or 'xdf.ch'. Found: " + identifier.getHostIdentifier() + ".");
                            }
                        }
                        
                        // In a second round, add the attributes to the types.
                        for (@Nonnull Block type : types) {
                            @Nonnull Block[] elements = new TupleWrapper(type).getElements(2);
                            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(elements[0]);
                            @Nonnull List<Block> attributes = new ListWrapper(elements[1]).getElements();
                            
                            for (@Nonnull Block attribute : attributes) {
                                @Nonnull Block element = SignatureWrapper.decodeUnverified(attribute).getElement();
                                @Nonnull Host host;
                                if (identifier.getHostIdentifier().equals(HostIdentifier.VIRTUALID)) {
                                    host = virtualidHost;
                                } else if (identifier.getHostIdentifier().equals(HostIdentifier.XDF)) {
                                    host = xdfHost;
                                } else {
                                    throw new InitializationError("The type " + identifier + " has a wrong host. Expected: Either 'virtualid.ch' or 'xdf.ch'. Found: " + identifier.getHostIdentifier() + ".");
                                }
                                host.setAttribute(connection, identifier.getIdentity(), new SelfcontainedWrapper(element).getIdentifier().getIdentity().toSemanticType(), attribute, null);
                            }
                        }
                        */
                    }
                    
                    Cache.setAttribute(null, HostIdentity.VIRTUALID, SemanticType.HOST_PUBLIC_KEY, 0l, new SignatureWrapper(publicKeyWrapper, null).toBlock());
                }
            }
            
        } catch (@Nonnull Exception exception) {
            throw new InitializationError("Could not initialize the cache.", exception);
        }
    }
    
    /**
     * Returns the attribute with the given type of the given identity from the cache.
     * 
     * @param requester the person who requests the attribute or null to indicate an unsigned (and therefore public) request.
     * @param requestee the identity whose attribute is to be returned.
     * @param type the semantic type of the requested attribute.
     * @param time the current time as returned by {@code System.currentTimeMillis()} used to determine whether the attribute is stale (this value can also be set to the past).
     * @return a block of type {@code attribute@virtualid.ch} or null if no such or only a stale attribute is available.
     * @require time >= 0 : "The time value is non-negative.";
     */
    public static @Nullable Block getAttribute(@Nullable Person requester, @Nonnull Identity requestee, @Nonnull SemanticType type, long time) throws SQLException {
        assert time >= 0 : "The time value is non-negative.";
        
        // TODO: Use a role instead of a person for the requester.
        
        @Nullable Block result = null;
        @Nonnull String query = "SELECT value FROM cache_attribute WHERE (requester = " + (requester == null ? 1 : requester) + " OR requester = 1) AND requestee = " + requestee + "  AND type = " + type + " AND time >= " + time;
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) result = new Block(resultSet.getBytes(1));
            connection.commit();
        }
        return result;
    }
    
    /**
     * Sets the attribute with the given type of the given identity in the cache.
     * 
     * @param requester the person who sets the attribute or null to indicate an unsigned (and therefore public) request.
     * @param requestee the identity whose attribute is to be set.
     * @param type the semantic type of the attribute to be set.
     * @param time the time as returned by {@code System.currentTimeMillis()} until which the attribute is considered fresh.
     * @param attribute a block of type {@code attribute@virtualid.ch}.
     * @require time >= 0 : "The time value is non-negative.";
     */
    public static void setAttribute(@Nullable Person requester, @Nonnull Identity requestee, @Nonnull SemanticType type, long time, @Nonnull Block attribute) throws SQLException {
        assert time >= 0 : "The time value is non-negative.";
        
        @Nonnull String statement = "REPLACE INTO cache_attribute (requester, requestee, type, time, value) VALUES (?, ?, ?, ?, ?)";
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, (requester == null ? 1 : requester.getNumber()));
            preparedStatement.setLong(2, requestee.getNumber());
            preparedStatement.setLong(3, type.getNumber());
            preparedStatement.setLong(4, time);
            Database.setBlock(preparedStatement, 5, attribute);
            preparedStatement.executeUpdate();
            connection.commit();
        }
    }
    
    
    /**
     * Returns the unwrapped attribute of the given subject with the given type.
     * 
     * @param role the role that queries the attribute or null for anonymous requests.
     * @param subject the identity whose attribute is to be returned.
     * @param type the type of the attribute which is to be returned
     * 
     * @return the unwrapped attribute of the given subject with the given type.
     * 
     * @require type.isAttributeType() : "The given type is an attribute type.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    public static @Nonnull Block getAttributeNotNullUnwrapped(@Nullable Role role, @Nonnull Identity subject, @Nonnull SemanticType type) throws SQLException, FailedRequestException, InvalidEncodingException, InvalidDeclarationException {
        assert type.isAttributeType() : "The given type is an attribute type.";
        
        throw new UnsupportedOperationException(); // TODO
    }
    
    public static @Nonnull Block getAttributeNotNullUnwrapped(@Nonnull Identity subject, @Nonnull SemanticType type) throws SQLException, FailedRequestException, InvalidEncodingException, InvalidDeclarationException {
        return getAttributeNotNullUnwrapped(null, subject, type);
    }
    
}
