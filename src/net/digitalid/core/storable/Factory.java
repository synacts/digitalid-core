package net.digitalid.core.storable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.collections.ElementConverter;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.IterableConverter;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * The factory allows to store and restore objects.
 * 
 * @see Storable
 * @see GeneralConceptFactory
 * @see SimpleNonHostConceptFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Factory<O, E> {
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Casts the nullable object to the class of this factory.
     * 
     * @param object the nullable object which is to be casted.
     * 
     * @return the nullable object casted to the class of this factory.
     */
    @Pure
    @SuppressWarnings("unchecked")
    public final @Nullable O castNullable(@Nullable Object object) {
        return (O) object;
    }
    
    /**
     * Casts the non-nullable object to the class of this factory.
     * 
     * @param object the non-nullable object which is to be casted.
     * 
     * @return the non-nullable object casted to the class of this factory.
     */
    @Pure
    @SuppressWarnings("unchecked")
    public final @Nonnull O castNonNullable(@Nonnull Object object) {
        return (O) object;
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
    public abstract @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull O object);
    
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
    public final @Nullable @NonEncoding Block encodeNullable(@Nullable O object) {
        return object == null ? null : encodeNonNullable(object);
    }
    
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
    public final @Nullable @NonEncoding Block encodeNullableWithCast(@Nullable Object object) {
        return encodeNullable(castNullable(object));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param entity the entity needed to reconstruct the object.
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Decodes the given nullable block.
     * 
     * @param entity the entity needed to reconstruct the object.
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nullable O decodeNullable(@Nonnull E entity, @Nullable @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException {
        if (block != null) return decodeNonNullable(entity, block);
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
     * Returns the number of columns used to store objects of the storable class in the database.
     * 
     * @return the number of columns used to store objects of the storable class in the database.
     */
    @Pure
    public final int getNumberOfColumns() {
        return columns.size();
    }
    
    /**
     * Stores the maximum length of the column names.
     */
    private final int maximumColumnLength;
    
    /**
     * Returns the maximum length of the column names.
     * 
     * @return the maximum length of the column names.
     */
    @Pure
    public final int getMaximumColumnLength() {
        return columns.size();
    }
    
    /**
     * Returns whether the given prefix is valid.
     * 
     * @param prefix the prefix to be checked.
     * 
     * @return whether the given prefix is valid.
     */
    @Pure
    public final boolean isValidPrefix(@Nonnull String prefix) {
        return prefix.isEmpty() || prefix.length() + maximumColumnLength < 22 && Database.getConfiguration().isValidIdentifier(prefix);
    }
    
    /**
     * Returns the columns as declaration with the given prefix.
     * 
     * @param prefix the prefix to prepend to all column names.
     * 
     * @return the columns as declaration with the given prefix.
     */
    @Pure
    public final @Nonnull String getDeclaration(final @Nonnull @Validated String prefix) {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        return IterableConverter.toString(columns, new ElementConverter<Column>() { @Pure @Override public String toString(@Nullable Column column) { return prefix + "_" + String.valueOf(column); } });
    }
    
    /**
     * Returns the columns as declaration without a prefix.
     * 
     * @return the columns as declaration without a prefix.
     */
    @Pure
    public final @Nonnull String getDeclaration() {
        return IterableConverter.toString(columns);
    }
    
    /**
     * Returns the columns for selection with the given prefix.
     * 
     * @param prefix the prefix to prepend to all column names.
     * 
     * @return the columns for selection with the given prefix.
     */
    @Pure
    public final @Nonnull String getSelection(final @Nonnull @Validated String prefix) {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        return IterableConverter.toString(columns, new ElementConverter<Column>() { @Pure @Override public String toString(@Nullable Column column) { return prefix + "_" + (column == null ? "null" : column.getName()); } });
    }
    
    /**
     * Returns the columns for selection without a prefix.
     * 
     * @return the columns for selection without a prefix.
     */
    @Pure
    public final @Nonnull String getSelection() {
        return getSelection("");
    }
    
    /**
     * Returns the foreign key constraints of the columns with the given prefix.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param site the site at which the foreign key constraints are declared.
     * 
     * @return the foreign key constraints of the columns with the given prefix.
     * 
     * @ensure return.isEmpty() || return.startsWith(",") : "The returned string is either empty or starts with a comma.";
     */
    @Locked
    @NonCommitting
    public @Nonnull String getForeignKeys(@Nonnull @Validated String prefix, @Nonnull Site site) throws SQLException {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        final @Nonnull StringBuilder string = new StringBuilder();
        for (final @Nonnull Column column : columns) {
            string.append(column.getForeignKey(prefix, site));
        }
        return string.toString();
    }
    
    /**
     * Returns the foreign key constraints of the columns without a prefix.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param site the site at which the foreign key constraints are declared.
     * 
     * @return the foreign key constraints of the columns without a prefix.
     * 
     * @ensure return.isEmpty() || return.startsWith(",") : "The returned string is either empty or starts with a comma.";
     */
    @Locked
    @NonCommitting
    public final @Nonnull String getForeignKeys(@Nonnull Site site) throws SQLException {
        return getForeignKeys("", site);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing (with Statement) –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value of the given object for each column.
     * 
     * @param object the object whose values are to be returned.
     * 
     * @return the value of the given object for each column.
     * 
     * @ensure return.size() == getColumns().size() : "The returned array contains a value for each column.";
     */
    @Pure
    protected abstract @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object);
    
    /**
     * Returns the value of the given object or null for each column.
     * 
     * @param object the nullable object whose values are to be returned.
     * 
     * @return the value of the given object or null for each column.
     * 
     * @ensure return.size() == columns.size() : "The returned array contains a value for each column.";
     */
    @Pure
    private @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValuesOrNulls(@Nullable O object) {
        if (object == null) return FreezableArray.<String>get(columns.size()).setAll("NULL");
        else return getValues(object);
    }
    
    /**
     * Returns the values of the given object separated by commas.
     * 
     * @param object the object whose values are to be returned.
     * 
     * @return the values of the given object separated by commas.
     */
    @Pure
    public final @Nonnull String getInsertForStatement(@Nullable O object) {
        return IterableConverter.toString(getValuesOrNulls(object));
    }
    
    /**
     * Returns whether the given alias is valid.
     * 
     * @param alias the alias to be checked.
     * 
     * @return whether the given alias is valid.
     */
    @Pure
    public static boolean isValidAlias(@Nonnull String alias) {
        return alias.isEmpty() || Database.getConfiguration().isValidIdentifier(alias);
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object.
     * 
     * @param alias the table alias that is to be prepended to all columns.
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object.
     * 
     * @ensure return.size() == columns.size() : "The returned array contains a value for each column.";
     */
    @Pure
    private @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getColumnsEqualValues(@Nonnull @Validated String alias, @Nonnull @Validated String prefix, @Nullable O object) {
        assert isValidAlias(alias) : "The alias is valid.";
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        final @Nonnull FreezableArray<String> values = getValuesOrNulls(object);
        for (int i = 0; i < values.size(); i++) {
            values.set(i, (alias.isEmpty() ? "" : alias + ".") + prefix + columns.getNonNullable(i).getName() + " = " + values.getNonNullable(i));
        }
        return values;
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object separated by commas.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object separated by commas.
     */
    @Pure
    public final @Nonnull String getUpdateForStatement(@Nonnull @Validated String prefix, @Nullable O object) {
        return IterableConverter.toString(getColumnsEqualValues("", prefix, object));
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object separated by commas.
     * 
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object separated by commas.
     */
    @Pure
    public final @Nonnull String getUpdateForStatement(@Nullable O object) {
        return getUpdateForStatement("", object);
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     * 
     * @param alias the table alias that is to be prepended to all columns.
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForStatement(@Nonnull @Validated String alias, @Nonnull @Validated String prefix, @Nullable O object) {
        return IterableConverter.toString(getColumnsEqualValues(alias, prefix, object), " AND ");
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForStatement(@Nonnull @Validated String prefix, @Nullable O object) {
        return getConditionForStatement("", prefix, object);
    }
    
    /**
     * Returns the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     * 
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and the corresponding value of the given object separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForStatement(@Nullable O object) {
        return getConditionForStatement("", object);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing (with PreparedStatement) –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns as many question marks as columns separated by commas.
     * 
     * @return as many question marks as columns separated by commas.
     */
    @Pure
    public final @Nonnull String getInsertForPreparedStatement() {
        return IterableConverter.toString(FreezableArray.<String>get(columns.size()).setAll("?"));
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark.
     * 
     * @param alias the table alias that is to be prepended to all columns.
     * @param prefix the prefix that is to be prepended to all column names.
     * 
     * @return the name of each column followed by the equality sign and a question mark.
     * 
     * @ensure return.size() == columns.size() : "The returned array contains a value for each column.";
     */
    @Pure
    private @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getColumnsEqualQuestionMarks(@Nonnull @Validated String alias, @Nonnull @Validated String prefix) {
        assert isValidAlias(alias) : "The alias is valid.";
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        final @Nonnull FreezableArray<String> values = FreezableArray.get(columns.size());
        for (int i = 0; i < values.size(); i++) {
            values.set(i, (alias.isEmpty() ? "" : alias + ".") + prefix + columns.getNonNullable(i).getName() + " = ?");
        }
        return values;
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark separated by commas.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and a question mark separated by commas.
     */
    @Pure
    public final @Nonnull String getUpdateForPreparedStatement(@Nonnull @Validated String prefix) {
        return IterableConverter.toString(getColumnsEqualQuestionMarks("", prefix));
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark separated by commas.
     * 
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and a question mark separated by commas.
     */
    @Pure
    public final @Nonnull String getUpdateForPreparedStatement() {
        return getUpdateForPreparedStatement("");
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     * 
     * @param alias the table alias that is to be prepended to all columns.
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForPreparedStatement(@Nonnull @Validated String alias, @Nonnull @Validated String prefix) {
        return IterableConverter.toString(getColumnsEqualQuestionMarks(alias, prefix), " AND ");
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     * 
     * @param prefix the prefix that is to be prepended to all column names.
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForPreparedStatement(@Nonnull @Validated String prefix) {
        return getConditionForPreparedStatement("", prefix);
    }
    
    /**
     * Returns the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     * 
     * @param object the object whose values are to be used for equality.
     * 
     * @return the name of each column followed by the equality sign and a question mark separated by {@code AND}.
     */
    @Pure
    public final @Nonnull String getConditionForPreparedStatement() {
        return getConditionForPreparedStatement("");
    }
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given non-nullable object.
     * The number of parameters that are set is given by {@link #getNumberOfColumns()}.
     * 
     * @param object the non-nullable object which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public abstract void setNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to null.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public final void setNull(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            preparedStatement.setNull(parameterIndex + i, columns.getNonNullable(i).getType().getCode());
        }
    }
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given nullable object.
     * The number of parameters that are set is given by {@link #getNumberOfColumns()}.
     * 
     * @param object the nullable object which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public final void setNullable(@Nullable O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (object == null) setNull(preparedStatement, parameterIndex);
        else setNonNullable(object, preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the entity which is needed to reconstruct the object.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public abstract @Nullable O getNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Returns a non-nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the entity which is needed to reconstruct the object.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a non-nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public final @Nonnull O getNonNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = getNullable(entity, resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-host concept factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected Factory(@Nonnull @Loaded SemanticType type, @Nonnull @NonNullableElements Column... columns) {
        this.type = type;
        this.columns = FreezableArray.getNonNullable(columns).freeze();
        int maximumColumnLength = 0;
        for (final @Nonnull Column column : columns) {
            final int columnLength = column.getName().length();
            if (columnLength > maximumColumnLength) maximumColumnLength = columnLength;
        }
        this.maximumColumnLength = maximumColumnLength;
    }
    
}
