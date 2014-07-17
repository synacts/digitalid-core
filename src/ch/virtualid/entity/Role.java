package ch.virtualid.entity;

import ch.virtualid.agent.IncomingRole;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.database.Database;
import ch.virtualid.identity.NonHostIdentity;
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
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Role extends Entity implements Immutable, SQLizable {
    
    /**
     * Stores the aspect of a new role being added to the observed role.
     */
    public static final @Nonnull Aspect ADDED = new Aspect(Role.class, "added");
    
    /**
     * Stores the aspect of the observed role being removed from the database.
     */
    public static final @Nonnull Aspect REMOVED = new Aspect(Role.class, "removed");
    
    
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
     * Stores the incoming role with the authorization for this role.
     */
    private final @Nonnull IncomingRole authorization;
    
    /**
     * Creates a new role for the given client with the given number, issuer, relation, recipient and agent.
     * <p>
     * <em>Important:</em> This constructor should only be called from this class and the roles module.
     * For all other purposes, please use {@link #get(ch.virtualid.client.Client, ch.virtualid.identity.NonHostIdentity, ch.virtualid.identity.SemanticType, ch.virtualid.entity.Role, ch.virtualid.agent.Agent)} or {@link #get(ch.virtualid.client.Client, java.sql.ResultSet, int)}.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param relation the relation of the new role.
     * @param recipient the recipient of the new role.
     * @param authorization the incoming role with the authorization for this role.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public Role(@Nonnull Client client, long number, @Nonnull NonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, @Nonnull IncomingRole authorization) {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        this.client = client;
        this.number = number;
        this.issuer = issuer;
        this.relation = relation;
        this.recipient = recipient;
        this.authorization = authorization;
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
    
    /**
     * Returns the incoming role with the authorization for this role.
     * 
     * @return the incoming role with the authorization for this role.
     */
    @Pure
    public @Nonnull IncomingRole getAuthorization() {
        return authorization;
    }
    
    
    @Pure
    @Override
    public @Nonnull Client getSite() {
        return client;
    }
    
    @Pure
    @Override
    public @Nonnull NonHostIdentity getIdentity() {
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
     * @param authorization the incoming role with the authorization for role to add.
     */
    public void addRole(@Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull IncomingRole authorization) throws SQLException {
        getRoles();
        assert roles != null;
        roles.add(get(client, issuer, relation, this, authorization));
        notify(ADDED);
    }
    
    /**
     * Removes this role.
     */
    public void remove() throws SQLException {
        Roles.remove(this);
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
     * (Use {@link #addRole(ch.virtualid.identity.NonHostIdentity, ch.virtualid.identity.SemanticType, ch.virtualid.agent.IncomingRole)}
     * or {@link Client#addRole(ch.virtualid.identity.NonHostIdentity, ch.virtualid.agent.IncomingRole)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param authorization the incoming role with the authorization for the returned role.
     * 
     * @return a new or existing role with the given arguments.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static @Nonnull Role get(@Nonnull Client client, @Nonnull NonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, @Nonnull IncomingRole authorization) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        final long number = Roles.map(client, issuer, relation, recipient, authorization);
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Client, Long> pair = new Pair<Client, Long>(client, number);
                @Nullable Role role = index.get(pair);
                if (role == null) {
                    role = new Role(client, number, issuer, relation, recipient, authorization);
                    index.put(pair, role);
                }
                return role;
            }
        } else {
            return new Role(client, number, issuer, relation, recipient, authorization);
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
