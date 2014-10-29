package ch.virtualid.module.client;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.util.FreezableList;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link Role roles} of the core service.
 * This class does not inherit from {@link ClientModule} and register itself at the
 * {@link CoreService} as its table needs be created in advance by a {@link Client}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.2
 */
public final class Roles {
    
    public static void createTable(@Nonnull Client client) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Use the reference field of the mapper class!
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + client + "role (role " + Database.getConfiguration().PRIMARY_KEY() + ", issuer BIGINT NOT NULL, relation BIGINT, recipient BIGINT, FOREIGN KEY (issuer) REFERENCES general_identity (identity), FOREIGN KEY (relation) REFERENCES general_identity (identity), FOREIGN KEY (recipient) REFERENCES general_identity (identity))");
            // TODO: Add the corresponding authorization ID? -> Yes, but now agent (ID).
            // -> the recipient should be another role, or not? -> I think so.
            // Maybe make an index on the issuer (and recipient)?
        }
        
//        Mapper.addReference("role", "issuer");
//        Mapper.addReference("role", "recipient");
    }
    
    public static void deleteTable(@Nonnull Client client) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Checks whether the given role is already mapped and returns the existing or newly mapped number.
     * 
     * @param client the client that can assume the given role.
     * @param issuer the issuer of the given role.
     * @param relation the relation of the given role.
     * @param recipient the recipient of the given role.
     * @param agentNumber the agent number of the given role.
     * 
     * @return the existing or newly mapped number for the given role.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static long map(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, long agentNumber) throws SQLException {
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
        
        // TODO: Use the constructor of the Role class.
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
