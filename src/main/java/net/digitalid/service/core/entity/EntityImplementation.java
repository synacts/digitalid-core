package net.digitalid.service.core.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.site.Site;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class implements methods of the {@link Entity} interface.
 * 
 * @see Account
 * @see Role
 */
@Immutable
public abstract class EntityImplementation<S extends Site> implements Entity {
    
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
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws AbortException {
        if (site instanceof Host) {
            return Account.getNotNull((Host) site, resultSet, columnIndex);
        } else if (site instanceof Client) {
            return Role.getNotNull((Client) site, resultSet, columnIndex);
        } else {
            throw new ShouldNeverHappenError("A site is either a host or a client.");
        }
    }
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws AbortException {
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
    public static void set(@Nullable Entity entity, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws AbortException {
        if (entity == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { entity.set(preparedStatement, parameterIndex); }
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(getKey());
    }
    
    /* -------------------------------------------------- Casting -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull HostEntity toHostEntity() throws InvalidEncodingException {
        if (this instanceof HostEntity) { return (HostEntity) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to HostEntity.");
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostEntity toNonHostEntity() throws InvalidEncodingException {
        if (this instanceof NonHostEntity) { return (NonHostEntity) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostEntity.");
    }
    
    @Pure
    @Override
    public final @Nonnull Account toAccount() throws InvalidEncodingException {
        if (this instanceof Account) { return (Account) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to Account.");
    }
    
    @Pure
    @Override
    public final @Nonnull HostAccount toHostAccount() throws InvalidEncodingException {
        if (this instanceof HostAccount) { return (HostAccount) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to HostAccount.");
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostAccount toNonHostAccount() throws InvalidEncodingException {
        if (this instanceof NonHostAccount) { return (NonHostAccount) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostAccount.");
    }
    
    @Pure
    @Override
    public final @Nonnull Role toRole() throws InvalidEncodingException {
        if (this instanceof Role) { return (Role) this; }
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to Role.");
    }
    
}
