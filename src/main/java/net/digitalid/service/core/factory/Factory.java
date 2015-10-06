package net.digitalid.service.core.factory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class Factory {
    
    // TODO: Also move the block conversions here? Or call this class completely differently?
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given non-nullable storable.
     * 
     * @param storable the non-nullable storable which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    public static <V extends Storable<V, ?>> void setNonNullable(@Nonnull V storable, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        storable.getFactory().setNonNullable(storable, preparedStatement, parameterIndex);
    }
    
}
