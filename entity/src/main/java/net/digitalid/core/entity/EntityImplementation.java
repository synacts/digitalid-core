package net.digitalid.core.entity;

import java.sql.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.system.castable.CastableObject;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.client.Client;
import net.digitalid.core.host.Host;

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
    public static @Nonnull Entity get(@Nonnull Site site, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
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
    public final void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
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
    public static void set(@Nullable Entity entity, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        if (entity == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { entity.set(preparedStatement, parameterIndex); }
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(getKey());
    }
    
}
