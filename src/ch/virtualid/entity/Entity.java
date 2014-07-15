package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Concept;
import ch.virtualid.client.Role;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept}.
 * 
 * @see HostEntity
 * @see ClientEntity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public abstract class Entity implements Immutable, SQLizable {
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public abstract @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public abstract @Nonnull Identity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public abstract long getNumber();
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Entity get(@Nonnull Site site, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        if (site instanceof Client) {
            return new ClientEntity((Client) site, Role.get((Client) site, resultSet, columnIndex));
        } else if (site instanceof Host) {
            return new HostEntity((Host) site, Identity.get(resultSet, columnIndex));
        } else {
            throw new ShouldNeverHappenError("A site is either a client or a host.");
        }
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    @Pure
    @Override
    public abstract @Nonnull String toString();
    
}
