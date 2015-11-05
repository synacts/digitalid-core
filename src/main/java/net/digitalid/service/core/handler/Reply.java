package net.digitalid.service.core.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.CompressionWrapper;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.IdentityNotFoundException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.packet.Packet;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnlyForHosts;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.errors.InitializationError;

/**
 * This class models replies to {@link Method methods} and stores them in the {@link Database database}.
 * All replies have to extend this class and {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Reply.Factory) register} themselves as handlers.
 * 
 * @invariant !hasSignature() || getSignature() instanceof HostSignatureWrapper : "If this reply has a signature, it is signed by a host.";
 * 
 * @see ActionReply
 * @see QueryReply
 */
@Immutable
public abstract class Reply extends Handler implements SQLizable {
    
    /**
     * Stores the number that references this reply in the database.
     */
    private final @Nullable Long number;
    
    /**
     * Creates a reply that encodes the content of a packet.
     * 
     * @param account the account to which this reply belongs.
     * @param subject the subject of this handler.
     */
    @OnlyForHosts
    protected Reply(@Nullable Account account, @Nonnull InternalIdentifier subject) {
        super(account, subject);
        
        this.number = null;
    }
    
    /**
     * Creates a reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected Reply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) {
        super(entity, signature);
        
        this.number = number;
    }
    
    /**
     * Returns the number that references this reply in the database.
     * 
     * @return the number that references this reply in the database.
     * 
     * @require hasSignature() : "This reply has a signature.";
     */
    @Pure
    public long getNumber() {
        assert hasSignature() : "This reply has a signature.";
        
        assert number != null : "This follows from the constructor.";
        return number;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Objects.toString(number);
    }
    
    
    /**
     * Each reply needs to {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Reply.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory {
        
        /**
         * Creates a reply that handles contents of the indicated type.
         * 
         * @param entity the entity to which the returned reply belongs
         * @param signature the signature of the returned reply.
         * @param number the number that references the reply.
         * @param block the content which is to be decoded.
         * 
         * @return a new reply that decodes the given block.
         * 
         * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
         * 
         * @ensure return.hasSignature() : "The returned reply has a signature.";
         */
        @Pure
        @NonCommitting
        protected abstract Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException;
        
    }
    
    
    /**
     * Maps reply types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Factory> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given factory that creates handlers for the given type.
     * 
     * @param type the type to handle.
     * @param factory the factory to add.
     */
    protected static void add(@Nonnull SemanticType type, @Nonnull Factory factory) {
        converters.put(type, factory);
    }
    
    /**
     * Returns a reply that handles the given block.
     * 
     * @param entity the entity to which the returned reply belongs.
     * @param signature the signature of the returned reply.
     * @param number the number that references the reply.
     * @param block the content which is to be decoded.
     * 
     * @return a reply that decodes the given block.
     * 
     * @throws PacketException if no handler is found for the given content type.
     * 
     * @ensure return.hasSignature() : "The returned reply has a signature.";
     */
    @Pure
    @NonCommitting
    private static @Nonnull Reply get(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        final @Nullable Reply.Factory factory = converters.get(block.getType());
        if (factory == null) throw new PacketException(PacketErrorCode.REPLY, "No reply could be found for the type " + block.getType().getAddress() + ".", null, true);
        else return factory.create(entity, signature, number, block);
    }
    
    /**
     * Returns a reply that handles the given block.
     * 
     * @param entity the entity to which the returned reply belongs.
     * @param signature the signature of the returned reply.
     * @param block the content which is to be decoded.
     * 
     * @return a reply that decodes the given block.
     * 
     * @throws PacketException if no handler is found for the given content type.
     * 
     * @ensure return.hasSignature() : "The returned reply has a signature.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Reply get(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        return get(entity, signature, store(signature), block);
    }
    
    
    /**
     * Returns this reply as an {@link ActionReply}.
     * 
     * @return this reply as an {@link ActionReply}.
     * 
     * @throws PacketException if this reply is not an instance of {@link ActionReply}.
     */
    @Pure
    public final @Nonnull ActionReply toActionReply() throws PacketException {
        if (this instanceof ActionReply) return (ActionReply) this;
        throw new PacketException(PacketErrorCode.REPLY, "An action reply was expected but a query reply was found.", null, true);
    }
    
    /**
     * Returns this reply as a {@link QueryReply}.
     * 
     * @return this reply as a {@link QueryReply}.
     * 
     * @throws PacketException if this reply is not an instance of {@link QueryReply}.
     */
    @Pure
    public final @Nonnull QueryReply toQueryReply() throws PacketException {
        if (this instanceof QueryReply) return (QueryReply) this;
        throw new PacketException(PacketErrorCode.REPLY, "A query reply was expected but an action reply was found.", null, true);
    }
    
    
    @Pure
    @Override
    protected final boolean protectedEquals(@Nullable Object object) {
        return super.protectedEquals(object) && object instanceof Reply;
    }
    
    @Pure
    @Override
    protected final int protectedHashCode() {
        return super.protectedHashCode();
    }
    
    
    /**
     * Stores the data type used to store replies in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference replies.
     */
    public static final @Nonnull String REFERENCE = new String("REFERENCES general_reply (reply) ON DELETE SET NULL ON UPDATE CASCADE");
    
    
    /**
     * Initializes the reply logger by creating the corresponding database tables if necessary.
     */
    static {
        assert Database.isMainThread() : "This method block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_reply (reply " + Database.getConfiguration().PRIMARY_KEY() + ", time " + Time.FORMAT + " NOT NULL, signature " + Block.FORMAT + " NOT NULL)");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database table of the reply logger could not be created.", exception);
        }
    }
    
    /**
     * Returns the reply with the given number or null if no such reply is available.
     * Please note that the signature is only read from the database and not verified.
     * 
     * @param entity the entity to which the returned reply belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nullable Reply get(@Nullable NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws AbortException, PacketException, ExternalException, NetworkException {
        final long number = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) return null;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet rs = statement.executeQuery("SELECT signature FROM general_reply WHERE reply = " + number)) {
            if (rs.next()) {
                final @Nonnull Block block = Block.getNotNull(Packet.SIGNATURE, rs, 1);
                final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, entity);
                final @Nonnull CompressionWrapper compression = new CompressionWrapper(signature.getNonNullableElement());
                final @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(compression.getElement());
                return get(entity, signature.toHostSignatureWrapper(), number, content.getElement());
            } else {
                throw new SQLException("There exists no reply with the number " + number + ".");
            }
        }
    }
    
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws AbortException {
        if (number == null) preparedStatement.setNull(parameterIndex, java.sql.Types.BIGINT);
        else preparedStatement.setLong(parameterIndex, number);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given reply.
     * 
     * @param reply the reply to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Reply reply, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws AbortException {
        if (reply == null) preparedStatement.setNull(parameterIndex, Types.BIGINT);
        else reply.set(preparedStatement, parameterIndex);
    }
    
    /**
     * Stores the given signature in the database.
     * 
     * @param signature the signature to store in the database.
     * 
     * @return the key generated for the stored signature.
     */
    @NonCommitting
    private static long store(@Nonnull HostSignatureWrapper signature) throws IdentityNotFoundException, SQLException {
        final @Nonnull String SQL = "INSERT INTO general_reply (time, signature) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(SQL)) {
            signature.getNonNullableTime().set(preparedStatement, 1);
            signature.toBlock().set(preparedStatement, 2);
            preparedStatement.executeUpdate();
            return Database.getGeneratedKey(preparedStatement);
        }
    }
    
}
