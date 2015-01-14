package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Instance;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.host.Host;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class implements methods of the {@link Entity} interface.
 * 
 * @see Account
 * @see Role
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class EntityClass extends Instance implements Entity, Immutable, SQLizable {
    
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
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        if (site instanceof Host) {
            return Account.getNotNull((Host) site, resultSet, columnIndex);
        } else if (site instanceof Client) {
            return Role.getNotNull((Client) site, resultSet, columnIndex);
        } else {
            throw new ShouldNeverHappenError("A site is either a host or a client.");
        }
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given entity.
     * 
     * @param entity the entity to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    public static void set(@Nullable Entity entity, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (entity == null) preparedStatement.setNull(parameterIndex, Types.BIGINT);
        else entity.set(preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(getNumber());
    }
    
    
    @Pure
    @Override
    public final @Nonnull HostEntity toHostEntity() throws InvalidEncodingException {
        if (this instanceof HostEntity) return (HostEntity) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to HostEntity.");
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostEntity toNonHostEntity() throws InvalidEncodingException {
        if (this instanceof NonHostEntity) return (NonHostEntity) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostEntity.");
    }
    
    @Pure
    @Override
    public final @Nonnull Account toAccount() throws InvalidEncodingException {
        if (this instanceof Account) return (Account) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to Account.");
    }
    
    @Pure
    @Override
    public final @Nonnull HostAccount toHostAccount() throws InvalidEncodingException {
        if (this instanceof HostAccount) return (HostAccount) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to HostAccount.");
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostAccount toNonHostAccount() throws InvalidEncodingException {
        if (this instanceof NonHostAccount) return (NonHostAccount) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostAccount.");
    }
    
    @Pure
    @Override
    public final @Nonnull Role toRole() throws InvalidEncodingException {
        if (this instanceof Role) return (Role) this;
        throw new InvalidEncodingException("This entity is a " + this.getClass().getSimpleName() + " and cannot be cast to Role.");
    }
    
}
