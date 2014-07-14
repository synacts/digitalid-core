package ch.virtualid.concepts;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.client.Roles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models the roles of clients.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.1
 */
public final class Role implements Immutable, SQLizable {
    
    /**
     * Stores the client that can assume this role.
     */
    private final @Nonnull Client client;
    
    /**
     * Stores the agent with the authorization for this role.
     */
    private final @Nonnull Agent agent;
    
    /**
     * Stores the number that references this role in the database.
     */
    private final long number;
    
    /**
     * Stores the issuer of this role.
     */
    private final @Nonnull NonHostIdentity issuer;
    
    /**
     * Stores the relation of this role.
     * 
     * @invariant relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    private final @Nullable SemanticType relation;
    
    /**
     * Stores the recipient of this role.
     */
    private final @Nullable Role recipient;
    
    /**
     * Creates a new role for the given client with the given issuer, relation and recipient.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param relation the relation of the new role.
     * @param recipient the recipient of the new role.
     */
    private Role(@Nonnull Client client, long number, @Nonnull NonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient) {
        this.client = client;
        this.number = number;
        this.issuer = issuer;
        this.relation = relation;
        this.recipient = recipient;
    }
    
    /**
     * Returns the client that can assume this role.
     * 
     * @return the client that can assume this role.
     */
    @Pure
    public @Nonnull Client getClient() {
        return client;
    }
    
    /**
     * Returns the number that references this role in the database.
     * 
     * @return the number that references this role in the database.
     */
    @Pure
    public long getNumber() {
        return number;
    }
    
    /**
     * Returns the issuer of this role.
     * 
     * @return the issuer of this role.
     */
    @Pure
    public @Nonnull NonHostIdentity getIssuer() {
        return issuer;
    }
    
    /**
     * Returns the relation of this role.
     * 
     * @return the relation of this role.
     * 
     * @ensure relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    @Pure
    public @Nullable SemanticType getRelation() {
        return relation;
    }
    
    /**
     * Returns the recipient of this role.
     * 
     * @return the recipient of this role.
     */
    @Pure
    public @Nullable Role getRecipient() {
        return recipient;
    }
    
    
    @Pure
    public @Nonnull List<Role> getRoles() {
        return new LinkedList<>(); // TODO
    }
    
    @Pure
    public static @Nonnull List<Role> getRoles(@Nonnull Client client) {
        return new LinkedList<>(); // TODO (Once completed, move to client class.)
    }
    
    // TODO: Implement equals()!
    
    
    /**
     * Caches the roles given a client and their number.
     */
    private static final @Nonnull Map<Pair<Client, Long>, Role> roles = Collections.synchronizedMap(new HashMap<Pair<Client, Long>, Role>());
    
    
    public static @Nonnull Role get(@Nonnull Client client, @Nonnull NonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient) throws SQLException {
        long number = Roles.map(client, issuer, relation, recipient);
        @Nonnull Pair<Client, Long> pair = new Pair<Client, Long>(client, number);
        @Nullable Role role = roles.get(pair);
        if (role == null) {
            role = new Role(client, number, issuer, relation, recipient);
            roles.put(pair, role);
        }
        return role;
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param client the client that can assume the returned role.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Role get(@Nonnull Client client, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        long number = resultSet.getLong(columnIndex);
        @Nonnull Pair<Client, Long> pair = new Pair<Client, Long>(client, number);
        @Nullable Role role = roles.get(pair);
        if (role == null) {
            role = new Role();
            roles.put(pair, role);
        }
        return role;
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, number);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(number);
    }
    
}
