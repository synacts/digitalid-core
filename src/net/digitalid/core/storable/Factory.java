package net.digitalid.core.storable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.collections.ElementConverter;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.IterableConverter;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.database.Column;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * The factory allows to store and restore objects.
 * 
 * @see Storable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Factory<O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type that corresponds to the storable class.
     */
    private final @Nonnull @Loaded SemanticType type;
    
    /**
     * Returns the semantic type that corresponds to the storable class.
     * 
     * @return the semantic type that corresponds to the storable class.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getType() {
        return type;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given non-nullable object as a new block.
     * 
     * @param object the non-nullable object to encode as a block.
     * 
     * @return the given non-nullable object encoded as a new block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    public abstract @Nonnull Block encodeNonNullable(@Nonnull O object);
    
    /**
     * Encodes the given nullable object as a new block.
     * 
     * @param object the nullable object to encode as a block.
     * 
     * @return the given nullable object encoded as a new block.
     * 
     * @ensure return == null || return.getType().equals(getType()) : "The returned block is either null or has the indicated type.";
     */
    @Pure
    public final @Nullable Block encodeNullable(@Nullable O object) {
        if (object != null) return encodeNonNullable(object);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param block the non-nullable block to decode.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    @Pure
    public abstract @Nonnull O decodeNonNullable(@Nonnull Block block);
    
    /**
     * Decodes the given nullable block.
     * 
     * @param block the nullable block to decode.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    public final @Nullable O decodeNullable(@Nullable Block block) {
        if (block != null) return decodeNonNullable(block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Columns –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the columns used to store objects of the storable class in the database.
     */
    private final @Nonnull @Frozen @NonNullableElements ReadOnlyArray<Column> columns;
    
    /**
     * Returns the columns used to store objects of the storable class in the database.
     * 
     * @return the columns used to store objects of the storable class in the database.
     */
    @Pure
    public final @Nonnull @Frozen ReadOnlyArray<Column> getColumns() {
        return columns;
    }
    
    /**
     * Returns whether the given prefix is valid.
     * 
     * @param prefix the prefix to be checked.
     * 
     * @return whether the given prefix is valid.
     */
    @Pure
    public final boolean isValid(@Nonnull String prefix) {
        return true; // TODO: Write a real implementation.
    }
    
    /**
     * 
     * 
     * @param prefix
     * 
     * @return 
     */
    @Pure
    public @Nonnull String getDeclaration(final @Nonnull @Validated String prefix) {
        // TODO: Make a precondition for the value of the prefix.

        return IterableConverter.toString(columns, new ElementConverter<Column>() { @Pure @Override public String toString(@Nullable Column column) { return prefix + String.valueOf(column); } });
    }
    
    @Pure
    public @Nonnull String getDeclaration() {
        return getDeclaration("");
    }
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = new String("REFERENCES general_identity (identity) ON DELETE RESCTRICT ON UPDATE RESCTRICT");
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
//    @NonCommitting
//    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    @NonCommitting
    public abstract void setNull(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given reply.
     * 
     * @param reply the reply to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
//    @NonCommitting
//    public final void set(@Nullable Storable<O> storable, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
//        if (storable == null) setNull(preparedStatement, parameterIndex);
//        else storable.set(preparedStatement, parameterIndex);
//    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public abstract @Nonnull O getNonNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * 
     * 
     * @param columns 
     */
    public Factory(@Nonnull @Loaded SemanticType type, @NonNullableElements Column... columns) {
        this.type = type;
        this.columns = new FreezableArray<>(columns).freeze();
    }
    
}
