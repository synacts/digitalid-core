package ch.virtualid.handler;

import ch.virtualid.database.Database;
import ch.virtualid.database.Entity;
import ch.virtualid.exceptions.InitializationError;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All replies have to extend this class, declare a public static field with the name {@code TYPE} which
 * states the semantic type of the packets that are handled and provide a constructor with the signature
 * ({@link ConnecSiteink Entity}, {@link SignatureWrapper}, {@link Block}) that only throws
 * {@link InvalidEncodingException} and {@link FailedIdentityException}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.6
 */
public abstract class Reply extends Handler {
    
    /**
     * Stores the data type used to store identities in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference identities.
     */
    public static final @Nonnull String REFERENCE = "REFERENCES reply (reply) ON DELETE SET NULL ON UPDATE CASCADE";
    
    
    /**
     * Initializes the reply logger by creating the corresponding database tables if necessary.
     */
    static {
        try (@Nonnull Connection connection= Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            // Important: Please make sure that any reference to this table allows its entries to be deleted (i.e. 'ON DELETE SET NULL').
            // TODO: Remove signer, subject and type to avoid mutually dependent foreign key constraints?
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS reply (reply " + Database.PRIMARY_KEY() + ", time BIGINT NOT NULL, signer BIGINT NOT NULL, subject BIGINT NOT NULL, type BIGINT NOT NULL, signature LONGBLOB NOT NULL, FOREIGN KEY (signer) REFERENCES map_identity (identity), FOREIGN KEY (subject) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            connection.commit();
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("Could not initialize the reply logger.", exception);
        }
        
        Mapper.addIdentityReference("reply", "subject");
        Mapper.addTypeReference("reply", "type");
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM reply WHERE time < UNIX_TIMESTAMP() * 1000 - " + 2 * YEAR);
                } catch (@Nonnull SQLException exception) {}
            }
        }, 20000, 2592000);
    }
    
    /**
     * Returns the reply with the given number or null if no such reply is available.
     * Please note that the signature is only read from the database and not verified.
     * 
     * @param number the number that references the reply in the database.
     * 
     * @return the reply with the given number or null if no such reply is available.
     */
    public static @Nullable Reply getReply(long number) throws SQLException, InvalidEncodingException, FailedIdentityException {
        @Nonnull String query = "SELECT signature FROM response WHERE response = " + number;
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                @Nonnull SignatureWrapper signature = SignatureWrapper.decodeUnverified(new Block(resultSet.getBytes(1)));
                if (!(signature instanceof HostSignatureWrapper)) throw new InvalidEncodingException("Responses have to be signed by hosts.");
                return (HostSignatureWrapper) signature;
            } else {
                return null;
            }
        }
    }
    
    /**
     * Adds the given reply to the database.
     * 
     * @param reply the reply to be added to the database.
     * 
     * @return the key generated for the added reply.
     * 
     * @require reply.getSignature() instanceof HostSignatureWrapper : "The signature is signed by a host.";
     * @require reply.getSignature() != null : "The signature of the given reply is not null.";
     * @require reply.getSignature().getSubject() != null : "The subject of the signature is not null.";
     */
    private static long addReply(@Nonnull Reply reply) throws FailedIdentityException, SQLException {
        assert reply.getSignature() instanceof HostSignatureWrapper : "The signature is signed by a host.";
        @Nullable HostSignatureWrapper signature = (HostSignatureWrapper) reply.getSignature();
        assert signature != null : "The signature of the given reply is not null.";
        @Nullable Identifier subject = signature.getSubject();
        assert subject != null : "The subject of the signature is not null.";
        
        @Nonnull Identity signer = signature.getSigner().getIdentity();
        @Nullable Identifier identifier = signature.getSubject();
        assert identifier != null : "The subject of a response is never null.";
        @Nonnull Identity subject = identifier.getIdentity();
        assert content != null : "The content of a signed statement is never null.";
        @Nonnull Identity type = content.getIdentifier().getIdentity();
        @Nonnull String SQL = "INSERT INTO response (time, signer, subject, type, signature) VALUES (?, ?, ?, ?, ?)";
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, signature.getTime());
            preparedStatement.setLong(2, signer.getNumber());
            preparedStatement.setLong(3, subject.getNumber());
            preparedStatement.setLong(4, type.getNumber());
            Database.setBlock(preparedStatement, 5, signature);
            preparedStatement.executeUpdate();
            return Database.getGeneratedKey(preparedStatement);
        } catch (@Nonnull SQLException exception) {
            if (subject.hasBeenMerged() || type.hasBeenMerged()) return addReply(connection, signature, content);
            else throw exception;
        }
    }

    
    /**
     * Stores the number that references this reply in the database.
     */
    private final @Nullable Long number;
    
    /**
     * Creates a new reply with the given connection, entity, signature, block and number.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     * @param number the number that references this reply in the database or null if it needs to be stored.
     * 
     * @require !connection.isOnBoth() || entity != null : "If the connection is site-specific, the entity is not null.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     */
    protected Reply(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull HostSiterapper signature, @Nonnull Block block, @Nullable Long number) throws FailedIdentityException, SQLException {
        super(connection, entity, signature, block);
        
        this.number = (number == null ? addReply(this) : number);
    }
    
    /**
     * Returns the number that references this reply in the database.
     * 
     * @return the number that references this reply in the database.
     */
    public @Nullable Long getNumber() {
        return number;
    }
    
    @Override
    public @Nonnull String toString() {
        return Objects.toString(number);
    }
    
    
    /**
     * Maps response types to their corresponding handler.
     */
    private static final @Nonnull Map<SemanticType, Class<? extends Reply>> handlers = new HashMap<SemanticType, Class<? extends Reply>>();
    
    /**
     * Adds the given handler.
     * 
     * @param handler the handler to add.
     */
    public static void add(@Nonnull Class<? extends Reply> handler) throws ServiceException {
        try {
            handlers.put((SemanticType) handler.getField("TYPE").get(null), handler);
        } catch (@Nonnull NoSuchFieldException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not declare a field with the name 'TYPE'.", exception);
        } catch (@Nonnull SecurityException | IllegalAccessException exception) {
            throw new ServiceException("The field with the name 'TYPE' in the handler '" + handler.getName() + "' cannot be accessed.", exception);
        } catch (@Nonnull NullPointerException | IllegalArgumentException exception) {
            throw new ServiceException("The field with the name 'TYPE' in the handler '" + handler.getName() + "' is not static.", exception);
        }
    }
    
    /**
     * Returns the handler for the given response type.
     * 
     * @param type the type of the response to be handled.
     * @param connection an open connection to the database.
     * @param entity the entity to which the handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     * 
     * @return the handler for the given response type.
     * 
     * @throws PacketException if no handler is found for the given response type.
     * 
     * @require !connection.isOnBoth() || entity != null : "If the connection is site-specific, the entity is not null.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     */
    public static @Nonnull Reply get(@Nonnull SemanticType type, @Nonnull Connection connection, @Nullable Entity entity, @Nonnull HostSignaSiter signature, @Nonnull Block block) throws PacketException, ServiceException, InvalidEncodingException, FailedIdentityException {
        @Nullable Class<? extends Reply> handler = handlers.get(type);
        if (handler == null) throw new PacketException(PacketError.REQUEST);
        try {
            @Nonnull Constructor<? extends Reply> constructor = handler.getConstructor(Connection.class, Entity.class, SignatureWrapper.class, Block.class);
  Sitereturn constructor.newInstance(connection, entity, signature, block);
        } catch (@Nonnull NoSuchMethodException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not declare a constructor with the required signature.", exception);
        } catch (@Nonnull SecurityException | IllegalAccessException exception) {
            throw new ServiceException("The required constructor in the handler '" + handler.getName() + "' cannot be accessed.", exception);
        } catch (@Nonnull InstantiationException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' is abstract and cannot be instantiated.", exception);
        } catch (@Nonnull IllegalArgumentException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not accept the given parameters.", exception);
        } catch (@Nonnull InvocationTargetException exception) {
            @Nullable Throwable cause = exception.getCause();
            if (cause == null) throw new ShouldNeverHappenError("The cause of the targe exception should never be null.");
            if (cause instanceof InvalidEncodingException) throw (InvalidEncodingException) cause;
            if (cause instanceof FailedIdentityException) throw (FailedIdentityException) cause;
            // TODO: Probably extend with other exceptions.
            throw new ServiceException("The constructor of the handler '" + handler.getName() + "' throws an unspecified exception.", exception);
        }
    }
    
}
