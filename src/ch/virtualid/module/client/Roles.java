package ch.virtualid.module.client;

import ch.virtualid.agent.IncomingRole;
import ch.virtualid.annotations.Capturable;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.Module;
import ch.virtualid.util.FreezableList;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the roles of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.2
 */
public final class Roles extends ClientModule {
    
    static { Module.add(new Roles()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS role (role " + Database.getConfiguration().PRIMARY_KEY() + ", issuer BIGINT NOT NULL, relation BIGINT, recipient BIGINT, FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (relation) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity))");
            // TODO: Add the corresponding authorization ID? -> Yes, but now agent (ID).
            // -> the recipient should be another role, or not? -> I think so.
        }
        
        Mapper.addReference("role", "issuer");
        Mapper.addReference("role", "recipient");
    }
    
    /**
     * Checks whether the given role is already mapped and returns the existing or newly mapped number.
     * 
     * @param client the client that can assume the given role.
     * @param issuer the issuer of the given role.
     * @param relation the relation of the given role.
     * @param recipient the recipient of the given role.
     * @param authorization the incoming role with the authorization for the given role.
     * 
     * @return the existing or newly mapped number for the given role.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static long map(@Nonnull Client client, @Nonnull NonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, @Nonnull IncomingRole authorization) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Returns the role of the given client with the given number.
     * 
     * @param client the client whose role is to be returned.
     * @param number the number of the role to be returned.
     * 
     * @return the role of the given client with the given number.
     */
    public static @Nonnull Role load(@Nonnull Client client, long number) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
        
        // TODO: If the recipient is not null, use the Role.get(client, resultSet, columnIndex) method to retrieve it.
    }
    
    /**
     * Removes the given role, which triggers the removal of all associated concepts.
     */
    public static void remove(@Nonnull Role role) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Returns the roles of the given role.
     * 
     * @param role the role whose roles are to be returned.
     * 
     * @return the roles of the given role.
     */
    public static @Capturable @Nonnull FreezableList<Role> getRoles(@Nonnull Role role) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Returns the roles of the given client.
     * 
     * @param client the client whose roles are to be returned.
     * 
     * @return the roles of the given client.
     */
    public static @Capturable @Nonnull FreezableList<Role> getRoles(@Nonnull Client client) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
