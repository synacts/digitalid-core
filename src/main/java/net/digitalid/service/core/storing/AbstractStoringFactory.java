package net.digitalid.service.core.storing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.reference.Captured;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.converter.ElementConverter;
import net.digitalid.utility.collections.converter.IterableConverter;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.Site;
import net.digitalid.utility.database.configuration.Database;

/**
 * A storing factory allows to store and restore objects into and from the {@link Database database}.
 * 
 * @param <O> the type of the objects that this factory can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * 
 * @see Storable
 * @see BlockBasedStoringFactory
 * @see FactoryBasedStoringFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class AbstractStoringFactory<O, E> {
    
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
    public final @Nonnull @Frozen @NonNullableElements ReadOnlyArray<Column> getColumns() {
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
        return maximumColumnLength;
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
        return prefix.isEmpty() || prefix.length() + maximumColumnLength <= 62 && Database.getConfiguration().isValidIdentifier(prefix); // TODO: Adjust the number 62 to Database.getConfiguration().getMaximumIdentifierLength() - 1.
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
    public abstract @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object);
    
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
    public abstract void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to null.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    @NonCommitting
    public final void storeNull(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
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
    public final void storeNullable(@Nullable O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (object == null) storeNull(preparedStatement, parameterIndex);
        else storeNonNullable(object, preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the entisetNonNullablety which is needed to reconstruct the object.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public abstract @Nullable O restoreNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
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
    public @Nonnull O restoreNonNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = restoreNullable(entity, resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new storing factory with the given columns.
     * 
     * @param columns the columns used to store objects of the storable class.
     */
    protected AbstractStoringFactory(@Captured @Nonnull @NonNullableElements Column... columns) {
        this.columns = FreezableArray.getNonNullable(columns).freeze();
        int maximumColumnLength = 0;
        for (final @Nonnull Column column : columns) {
            final int columnLength = column.getName().length();
            if (columnLength > maximumColumnLength) maximumColumnLength = columnLength;
        }
        this.maximumColumnLength = maximumColumnLength;
    }
    
}
