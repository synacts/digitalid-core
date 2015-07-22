package net.digitalid.core.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;

/**
 * Objects of classes that implement this interface can be stored in and retrieved from the database.
 * Unfortunately, the static fields and methods cannot be made mandatory in implementing classes and
 * are thus only indicated with comments.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Deprecated
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
    
}
