package net.digitalid.service.core.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.castable.CastableObject;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.utility.database.site.Site;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class implements methods of the {@link Entity} interface.
 * 
 * @see Account
 * @see Role
 */
@Immutable
public abstract class EntityImplementation<S extends Site> extends CastableObject implements Entity {
    
    // TODO: Implement the getSite() here.
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param site the site that accommodates the returned entity.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws DatabaseException {
        if (site instanceof Host) {
            return Account.getNotNull((Host) site, resultSet, columnIndex);
        } else if (site instanceof Client) {
            return Role.getNotNull((Client) site, resultSet, columnIndex);
        } else {
            throw ShouldNeverHappenError.get("A site is either a host or a client.");
        }
    }
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws DatabaseException {
        preparedStatement.setLong(parameterIndex, getKey());
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given entity.
     * 
     * @param entity the entity to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Entity entity, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws DatabaseException {
        if (entity == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { entity.set(preparedStatement, parameterIndex); }
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(getKey());
    }
    
}
