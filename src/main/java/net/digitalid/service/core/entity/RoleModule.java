package net.digitalid.core.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.annotations.reference.Capturable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.client.Client;
import net.digitalid.collections.freezable.FreezableLinkedList;
import net.digitalid.collections.freezable.FreezableList;
import net.digitalid.database.configuration.Database;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.data.ClientModule;
import net.digitalid.core.service.CoreService;

/**
 * This class provides database access to the {@link Role roles} of the core service.
 * This class does not inherit from {@link ClientModule} and register itself at the
 * {@link CoreService} as its table needs be created in advance by a {@link Client}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class RoleModule {
    
    /**
     * Creates the database tables for the given client.
     * 
     * @param client the client for which to create the database tables.
     */
    @NonCommitting
    public static void createTable(@Nonnull Client client) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + client + "role (role " + Database.getConfiguration().PRIMARY_KEY() + ", issuer " + Mapper.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + ", recipient " + EntityClass.FORMAT + ", agent " + Agent.FORMAT + " NOT NULL, FOREIGN KEY (issuer) " + Mapper.REFERENCE + ", FOREIGN KEY (relation) " + Mapper.REFERENCE + ", FOREIGN KEY (recipient) " + client.getEntityReference() + ")");
            Mapper.addReference(client + "role", "issuer");
        }
    }
    
    /**
     * Deletes the database tables for the given client.
     * 
     * @param client the client for which to delete the database tables.
     */
    @NonCommitting
    public static void deleteTable(@Nonnull Client client) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Mapper.removeReference(client + "role", "issuer");
            statement.executeUpdate("DROP TABLE IF EXISTS " + client + "role");
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
     * @require (relation == null) == (recipient == null) : "The relation and the recipient are either both null or non-null.";
     */
    @NonCommitting
    public static long map(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, long agentNumber) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        assert (relation == null) == (recipient == null) : "The relation and the recipient are either both null or non-null.";
        
        final @Nonnull String SQL = "SELECT role FROM " + client + "role WHERE issuer = " + issuer + " AND relation = " + relation + " AND recipient = " + recipient + " AND agent = " + agentNumber;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return resultSet.getLong(1);
            else return Database.executeInsert(statement, "INSERT INTO " + client + "role (issuer, relation, recipient, agent) VALUES (" + issuer + ", " + relation + ", " + recipient + ", " + agentNumber + ")");
        }
    }
    
    /**
     * Returns the role of the given client with the given number.
     * 
     * @param client the client whose role is to be returned.
     * @param number the number of the role to be returned.
     * 
     * @return the role of the given client with the given number.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Role load(@Nonnull Client client, long number) throws SQLException {
        final @Nonnull String SQL = "SELECT issuer, relation, recipient, agent FROM " + client + "role WHERE role = " + number;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull InternalNonHostIdentity issuer = IdentityClass.getNotNull(resultSet, 1).toInternalNonHostIdentity();
                final @Nullable Identity identity = IdentityClass.get(resultSet, 2);
                final @Nullable SemanticType relation = identity != null ? identity.toSemanticType().checkIsRoleType() : null;
                final @Nullable Role recipient = Role.get(client, resultSet, 3);
                final long agentNumber = resultSet.getLong(4);
                if (relation == null && recipient == null) return NativeRole.get(client, number, issuer, agentNumber);
                if (relation != null && recipient != null) return NonNativeRole.get(client, number, issuer, relation, recipient, agentNumber);
                else throw new InvalidEncodingException("The relation and the recipient have to be either both null or non-null.");
            } else throw new SQLException("The role of the client '" + client + "' with the number" + number + " could not be found.");
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The encoding of a database entry is invalid.", exception);
        }
    }
    
    /**
     * Removes the given role, which triggers the removal of all associated concepts.
     */
    @NonCommitting
    public static void remove(@Nonnull Role role) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + role.getClient() + "role WHERE role = " + role);
        }
    }
    
    /**
     * Returns the non-native roles of the given role.
     * 
     * @param role the role whose roles are to be returned.
     * 
     * @return the non-native roles of the given role.
     * 
     * @ensure return.!isFrozen() : "The returned list is not frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull FreezableList<NonNativeRole> getRoles(@Nonnull Role role) throws SQLException {
        final @Nonnull Client client = role.getClient();
        final @Nonnull String SQL = "SELECT role, issuer, relation, agent FROM " + client + "role WHERE recipient = " + role;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<NonNativeRole> roles = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final long number = resultSet.getLong(1);
                final @Nonnull InternalNonHostIdentity issuer = IdentityClass.getNotNull(resultSet, 2).toInternalNonHostIdentity();
                final @Nonnull SemanticType relation = IdentityClass.getNotNull(resultSet, 3).toSemanticType();
                final long agentNumber = resultSet.getLong(4);
                roles.add(NonNativeRole.get(client, number, issuer, relation, role, agentNumber));
            }
            return roles;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The encoding of a database entry is invalid.", exception);
        }
    }
    
    /**
     * Returns the native roles of the given client.
     * 
     * @param client the client whose roles are to be returned.
     * 
     * @return the native roles of the given client.
     * 
     * @ensure return.!isFrozen() : "The returned list is not frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull FreezableList<NativeRole> getRoles(@Nonnull Client client) throws SQLException {
        final @Nonnull String SQL = "SELECT role, issuer, agent FROM " + client + "role WHERE recipient IS NULL";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<NativeRole> roles = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final long number = resultSet.getLong(1);
                final @Nonnull InternalNonHostIdentity issuer = IdentityClass.getNotNull(resultSet, 2).toInternalNonHostIdentity();
                final long agentNumber = resultSet.getLong(3);
                roles.add(NativeRole.get(client, number, issuer, agentNumber));
            }
            return roles;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The encoding of a database entry is invalid.", exception);
        }
    }
    
    /**
     * Returns the role that the given client has for the given person.
     * 
     * @param client the client for whom a role is to be returned.
     * @param person the person that issued the role to be returned.
     * 
     * @return the role that the given client has for the given person.
     * 
     * @throws PacketException if no such role can be found.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Role getRole(@Nonnull Client client, @Nonnull InternalPerson person) throws SQLException, PacketException {
        final @Nonnull String SQL = "SELECT role, relation, recipient, agent FROM " + client + "role WHERE issuer = " + person;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final long number = resultSet.getLong(1);
                final @Nullable Identity identity = IdentityClass.get(resultSet, 2);
                final @Nullable SemanticType relation = identity != null ? identity.toSemanticType().checkIsRoleType() : null;
                final @Nullable Role recipient = Role.get(client, resultSet, 3);
                final long agentNumber = resultSet.getLong(4);
                if (relation == null && recipient == null) return NativeRole.get(client, number, person, agentNumber);
                if (relation != null && recipient != null) return NonNativeRole.get(client, number, person, relation, recipient, agentNumber);
                else throw new InvalidEncodingException("The relation and the recipient have to be either both null or non-null.");
            } else throw new PacketException(PacketError.IDENTIFIER, "No role for the person " + person.getAddress() + " could be found.");
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The encoding of a database entry is invalid.", exception);
        }
    }
    
}
