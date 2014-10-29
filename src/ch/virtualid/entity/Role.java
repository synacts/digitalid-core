package ch.virtualid.entity;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.client.Roles;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models a role on the client-side.
 * 
 * @see Roles
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Role extends Entity implements Immutable, SQLizable, Observer {
    
    // TODO: Make sure to also issue the CREATED notification!
    
    /**
     * Stores the aspect of a new role being added to the observed role.
     */
    public static final @Nonnull Aspect ADDED = new Aspect(Role.class, "added");
    
    
    /**
     * Stores the client that can assume this role.
     */
    private final @Nonnull Client client;
    
    /**
     * Stores the number that references this role in the database.
     */
    private final long number;
    
    /**
     * Stores the issuer of this role.
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
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
     * Stores the agent of this role.
     */
    private final @Nonnull Agent agent;
    
    /**
     * Creates a new role for the given client with the given number, issuer, relation, recipient and agent.
     * <p>
     * <em>Important:</em> This constructor should only be called from this class and the roles module.
     * For all other purposes, please use {@link #add(ch.virtualid.client.Client, ch.virtualid.identity.InternalNonHostIdentity, ch.virtualid.identity.SemanticType, ch.virtualid.entity.Role, boolean, long)} or {@link #get(ch.virtualid.client.Client, java.sql.ResultSet, int)}.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param relation the relation of the new role.
     * @param recipient the recipient of the new role.
     * @param isClient whether the agent number denotes a client.
     * @param agentNumber the agent number of the new role.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public Role(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, boolean isClient, long agentNumber) {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        this.client = client;
        this.number = number;
        this.issuer = issuer;
        this.relation = relation;
        this.recipient = recipient;
        this.agent = isClient ? ClientAgent.get(this, agentNumber, true) : OutgoingRole.get(this, agentNumber, false, false);
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
     * Returns the issuer of this role.
     * 
     * @return the issuer of this role.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity getIssuer() {
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
    
    /**
     * Returns the agent of this role.
     * 
     * @return the agent of this role.
     */
    @Pure
    public @Nonnull Agent getAgent() {
        return agent;
    }
    
    /**
     * Returns whether this role is native.
     * 
     * @return whether this role is native.
     */
    @Pure
    public boolean isNative() {
        return recipient == null;
    }
    
    
    @Pure
    @Override
    public @Nonnull Client getSite() {
        return client;
    }
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity() {
        return issuer;
    }
    
    @Pure
    @Override
    public long getNumber() {
        return number;
    }
    
    
    /**
     * Stores the roles of this role.
     */
    private @Nullable FreezableList<Role> roles;
    
    /**
     * Returns the roles of this role.
     * 
     * @return the roles of this role.
     */
    @Pure
    public @Nonnull ReadonlyList<Role> getRoles() throws SQLException {
        if (roles == null) {
            roles = Roles.getRoles(this);
        }
        return roles;
    }
    
    /**
     * Adds the given role to the roles of this role.
     * 
     * @param issuer the issuer of the role to add.
     * @param relation the relation of the role to add.
     * @param agentNumber the agent number of the role to add.
     */
    @OnlyForActions
    public void addRole(@Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws SQLException {
        final @Nonnull Role role = add(client, issuer, relation, this, false, agentNumber);
        role.observe(this, REMOVED);
        
        if (roles != null) roles.add(role);
        notify(ADDED);
    }
    
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
        if (aspect.equals(REMOVED) && roles != null) {
            roles.remove(instance);
        }
    }
    
    /**
     * Removes this role.
     */
    public void remove() throws SQLException {
        remove(this);
        notify(REMOVED);
    }
    
    
    /**
     * Caches the roles given their client and number.
     */
    private static final @Nonnull Map<Pair<Client, Long>, Role> index = new HashMap<Pair<Client, Long>, Role>();
    
    /**
     * Returns a new or existing role with the given arguments.
     * <p>
     * <em>Important:</em> This method should not be called directly.
     * (Use {@link #addRole(ch.virtualid.identity.InternalNonHostIdentity, ch.virtualid.identity.SemanticType, long)}
     * or {@link Client#addRole(ch.virtualid.identity.InternalNonHostIdentity)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param isClient whether the agent number denotes a client.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing role with the given arguments.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static @Nonnull Role add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, boolean isClient, long agentNumber) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        final long number = Roles.map(client, issuer, relation, recipient, agentNumber);
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Client, Long> pair = new Pair<Client, Long>(client, number);
                @Nullable Role role = index.get(pair);
                if (role == null) {
                    role = new Role(client, number, issuer, relation, recipient, isClient, agentNumber);
                    index.put(pair, role);
                }
                return role;
            }
        } else {
            return new Role(client, number, issuer, relation, recipient, isClient, agentNumber);
        }
    }
    
    /**
     * Removes the given role from the database and index.
     * 
     * @param role the role to be removed.
     */
    private static void remove(@Nonnull Role role) throws SQLException {
        Roles.remove(role);
        if (Database.isSingleAccess()) {
            synchronized(index) {
                index.remove(new Pair<Client, Long>(role.getClient(), role.getNumber()));
            }
        }
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
        final long number = resultSet.getLong(columnIndex);
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Client, Long> pair = new Pair<Client, Long>(client, number);
                @Nullable Role role = index.get(pair);
                if (role == null) {
                    role = Roles.load(client, number);
                    index.put(pair, role);
                }
                return role;
            }
        } else {
            return Roles.load(client, number);
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
     * @throws InvalidEncodingException if no such role can be found.
     */
    @Pure
    public static @Nonnull Role get(@Nonnull Client client, @Nonnull Person person) throws SQLException, InvalidEncodingException {
        throw new UnsupportedOperationException("Cannot yet return a role for the given person."); // TODO
    }
    
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + client.hashCode();
        hash = 41 * hash + (int) (number ^ (number >>> 32));
        return hash;
    }
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Role)) return false;
        final @Nonnull Role other = (Role) object;
        return this.client.equals(other.client) && this.number == other.number;
    }
    
}
