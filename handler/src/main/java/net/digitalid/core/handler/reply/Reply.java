package net.digitalid.core.handler.reply;

import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.annotations.type.Referenced;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.Handler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class models replies to {@link Method methods} and stores them in the {@link Database database}.
 * All replies have to extend this class and {@link ReplyIndex#add(net.digitalid.core.identification.identity.SemanticType, net.digitalid.core.handler.reply.ReplyIndex.Factory) register} themselves as handlers.
 * 
 * @see ActionReply
 * @see QueryReply
 */
@Immutable
@Referenced
public abstract class Reply<ENTITY extends Entity<?>> extends RootClass implements Handler<ENTITY> {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    @Default("null")
    public abstract @Nullable HostSignature<Compression<Pack>> getSignature();
    
    /* -------------------------------------------------- Number -------------------------------------------------- */
    
    /**
     * Returns the number that references this reply in the database.
     */
    @Pure
    @PrimaryKey
    @Default("null")
    @NonRepresentative // TODO: Load this number correctly.
    public abstract @Nullable Long getNumber();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(!hasBeenReceived() || isOnClient()).orThrow("Replies can only be received on clients.");
    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    // TODO: The following code should no longer be necessary.
    
//    /**
//     * Stores the data type used to store replies in the database.
//     */
//    public static final @Nonnull String FORMAT = "BIGINT";
//    
//    /**
//     * Stores the foreign key constraint used to reference replies.
//     */
//    public static final @Nonnull String REFERENCE = new String("REFERENCES general_reply (reply) ON DELETE SET NULL ON UPDATE CASCADE");
//    
//    
//    /**
//     * Initializes the reply logger by creating the corresponding database tables if necessary.
//     */
//    static {
//        Require.that(Threading.isMainThread()).orThrow("This method block is called in the main thread.");
//        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_reply (reply " + Database.getConfiguration().PRIMARY_KEY() + ", time " + Time.FORMAT + " NOT NULL, signature " + Block.FORMAT + " NOT NULL)");
//        } catch (@Nonnull SQLException exception) {
//            throw InitializationError.get("The database table of the reply logger could not be created.", exception);
//        }
//    }
//    
//    /**
//     * Returns the reply with the given number or null if no such reply is available.
//     * Please note that the signature is only read from the database and not verified.
//     * 
//     * @param entity the entity to which the returned reply belongs.
//     * @param resultSet the result set to retrieve the data from.
//     * @param columnIndex the index of the column containing the data.
//     * 
//     * @return the given column of the result set as an instance of this class.
//     */
//    @Pure
//    @NonCommitting
//    public static @Nullable Reply get(@Nullable NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws ExternalException {
//        final long number = resultSet.getLong(columnIndex);
//        if (resultSet.wasNull()) { return null; }
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet rs = statement.executeQuery("SELECT signature FROM general_reply WHERE reply = " + number)) {
//            if (rs.next()) {
//                final @Nonnull Block block = Block.getNotNull(Packet.SIGNATURE, rs, 1);
//                final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, entity);
//                final @Nonnull Block compression = CompressionWrapper.decompressNonNullable(signature.getNonNullableElement());
//                final @Nonnull Block content = SelfcontainedWrapper.decodeNonNullable(compression);
//                return get(entity, signature.toHostSignatureWrapper(), number, content);
//            } else {
//                throw new SQLException("There exists no reply with the number " + number + ".");
//            }
//        }
//    }
//    
//    @Override
//    @NonCommitting
//    public void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
//        if (number == null) { preparedStatement.setNull(parameterIndex, java.sql.Types.BIGINT); }
//        else { preparedStatement.setLong(parameterIndex, number); }
//    }
//    
//    /**
//     * Sets the parameter at the given index of the prepared statement to the given reply.
//     * 
//     * @param reply the reply to which the parameter at the given index is to be set.
//     * @param preparedStatement the prepared statement whose parameter is to be set.
//     * @param parameterIndex the index of the parameter to set.
//     */
//    @NonCommitting
//    public static void set(@Nullable Reply reply, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
//        if (reply == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
//        else { reply.set(preparedStatement, parameterIndex); }
//    }
//    
//    /**
//     * Stores the given signature in the database.
//     * 
//     * @param signature the signature to store in the database.
//     * 
//     * @return the key generated for the stored signature.
//     */
//    @NonCommitting
//    private static long store(@Nonnull HostSignatureWrapper signature) throws IdentityNotFoundException, SQLException {
//        final @Nonnull String SQL = "INSERT INTO general_reply (time, signature) VALUES (?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(SQL)) {
//            signature.getNonNullableTime().set(preparedStatement, 1);
//            signature.toBlock().set(preparedStatement, 2);
//            preparedStatement.executeUpdate();
//            return Database.getGeneratedKey(preparedStatement);
//        }
//    }
    
}
