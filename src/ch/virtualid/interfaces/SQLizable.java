package ch.virtualid.interfaces;

import ch.virtualid.annotations.Pure;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Objects of classes that implement this interface can be stored in and retrieved from the database.
 * Unfortunately, the static fields and methods cannot be made mandatory in implementing classes and
 * are thus only indicated with comments.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface SQLizable {
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    // public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    // public static final @Nonnull String REFERENCE = "REFERENCES map_identity (identity) ON DELETE CASCADE ON UPDATE CASCADE";
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    // @Pure
    // public static @Nonnull SQLizable get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
    
    /**
     * Returns the string that represents this object in the database.
     * 
     * @return the string that represents this object in the database.
     */
    @Pure
    @Override
    public @Nonnull String toString();
    
}
