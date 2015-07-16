package net.digitalid.core.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.collections.ElementConverter;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.IterableConverter;
import net.digitalid.core.collections.ReadOnlyArray;

/**
 * Objects of classes that implement this interface can be stored in and retrieved from the database.
 * Unfortunately, the static fields and methods cannot be made mandatory in implementing classes and
 * are thus only indicated with comments.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface SQLizable {
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Returns the string that represents this object in the database.
     * 
     * @return the string that represents this object in the database.
     */
    @Pure
    @Override
    public @Nonnull String toString();
    
    
    /**
     * 
     */
    @Immutable
    public static abstract class Factory {
        
        /**
         * Stores the columns used to store instances of the surrounding class in the database.
         */
        private final @Nonnull @Frozen @NonNullableElements ReadOnlyArray<Column> columns;
        
        /**
         * 
         * 
         * @param columns 
         */
        public Factory(@NonNullableElements Column... columns) {
            this.columns = new FreezableArray<>(columns).freeze();
        }
        
        @Pure
        public @Nonnull @Frozen ReadOnlyArray<Column> getColumns() {
            return columns;
        }
        
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
        public abstract @Nonnull SQLizable getNonNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
        
        @NonCommitting
        public abstract void setNull(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
        
        /**
         * Sets the parameter at the given index of the prepared statement to the given reply.
         * 
         * @param reply the reply to which the parameter at the given index is to be set.
         * @param preparedStatement the prepared statement whose parameter is to be set.
         * @param parameterIndex the index of the parameter to set.
         */
        @NonCommitting
        public final void set(@Nullable SQLizable sqlizable, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            if (sqlizable == null) setNull(preparedStatement, parameterIndex);
            else sqlizable.set(preparedStatement, parameterIndex);
        }
        
    }
    
}
