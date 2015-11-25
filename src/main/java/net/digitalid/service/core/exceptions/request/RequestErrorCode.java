package net.digitalid.service.core.exceptions.request;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.Int8Wrapper;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class enumerates the various request error codes.
 * 
 * @see RequestException
 */
@Immutable
public enum RequestErrorCode implements XDF<RequestErrorCode, Object>, SQL<RequestErrorCode, Object> {
    
    /* -------------------------------------------------- Error Codes -------------------------------------------------- */
    
    /**
     * The error code for an internal problem.
     */
    INTERNAL(0),
    
    /**
     * The error code for an external problem.
     */
    EXTERNAL(1),
    
    /**
     * The error code for a database problem.
     */
    DATABASE(1),
    
    /**
     * The error code for a network problem.
     */
    NETWORK(1),
    
    /**
     * The error code for an invalid packet.
     */
    PACKET(2),
    
    /**
     * The error code for an invalid encryption.
     */
    ENCRYPTION(3),
    
    /**
     * The error code for invalid elements.
     */
    ELEMENTS(4),
    
    /**
     * The error code for an invalid signature.
     */
    SIGNATURE(5),
    
    /**
     * The error code for an invalid compression.
     */
    COMPRESSION(6),
    
    /**
     * The error code for an invalid content.
     */
    CONTENT(7),
    
    /**
     * The error code for an invalid service.
     */
    SERVICE(8),
    
    /**
     * The error code for an invalid method type.
     */
    METHOD(9),
    
    /**
     * The error code for an invalid audit.
     */
    AUDIT(10),
    
    /**
     * The error code for a replayed packet.
     */
    REPLAY(11),
    
    /**
     * The error code for a required key rotation.
     */
    KEYROTATION(15),
    
    /**
     * The error code for an invalid identifier as subject.
     */
    IDENTIFIER(12),
    
    /**
     * The error code for a relocated identity.
     */
    RELOCATION(13),
    
    /**
     * The error code for an insufficient authorization.
     */
    AUTHORIZATION(14);
    
    
    /**
     * Returns whether the given value is a valid request error.
     *
     * @param value the value to check.
     * 
     * @return whether the given value is a valid request error.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 15;
    }
    
    /**
     * Returns the request error encoded by the given value.
     * 
     * @param value the value encoding the request error.
     * 
     * @return the request error encoded by the given value.
     * 
     * @require isValid(value) : "The value is a valid request error.";
     */
    @Pure
    public static @Nonnull RequestErrorCode get(byte value) {
        assert isValid(value) : "The value is a valid request error.";
        
        for (final @Nonnull RequestErrorCode error : values()) {
            if (error.value == value) { return error; }
        }
        
        throw new ShouldNeverHappenError("The value '" + value + "' does not encode a request error.");
    }
    
    
    /**
     * Stores the semantic type {@code code.error.request@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("code.error.request@core.digitalid.net").load(Int8Wrapper.XDF_TYPE);
    
    /**
     * Returns the request error encoded by the given block.
     * 
     * @param block the block containing the request error.
     * 
     * @return the request error encoded by the given block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull RequestErrorCode get(@Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final byte value = Int8Wrapper.decode(block);
        if (!isValid(value)) { throw InvalidParameterValueException.get("value", value); }
        return get(value);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return Int8Wrapper.encode(TYPE, value);
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value of this request error.
     */
    private final @Validated byte value;
    
    /**
     * Returns the value of this request error.
     * 
     * @return the value of this request error.
     */
    @Pure
    public @Validated byte getValue() {
        return value;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Creates a new request error with the given value.
     * 
     * @param value the value encoding the request error.
     */
    private RequestErrorCode(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the name of this request error.
     * 
     * @return the name of this request error.
     */
    @Pure
    public @Nonnull String getName() {
        final @Nonnull String string = name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().TINYINT();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull RequestErrorCode get(@Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws DatabaseException {
        final @Nonnull byte value = resultSet.getByte(columnIndex);
        if (!isValid(value)) { throw new SQLException("'" + value + "' is not a valid request error."); }
        return get(value);
    }
    
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws DatabaseException {
        preparedStatement.setByte(parameterIndex, value);
    }
    
}
