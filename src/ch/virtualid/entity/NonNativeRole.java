package ch.virtualid.entity;

import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import static ch.virtualid.entity.Entity.CREATED;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a non-native role on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class NonNativeRole extends Role implements Immutable {
    
    /**
     * Stores the relation of this role.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private final @Nonnull SemanticType relation;
    
    /**
     * Stores the recipient of this role.
     */
    private final @Nonnull Role recipient;
    
    /**
     * Stores the outgoing role of this role.
     */
    private final @Nonnull OutgoingRole outgoingRole;
    
    /**
     * Creates a new non-native role for the given client with the given number, issuer, relation, recipient and agent number.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param relation the relation of the new role.
     * @param recipient the recipient of the new role.
     * @param agentNumber the agent number of the new role.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    private NonNativeRole(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull Role recipient, long agentNumber) {
        super(client, number, issuer);
        
        assert relation.isRoleType() : "The relation is a role type.";
        
        this.relation = relation;
        this.recipient = recipient;
        this.outgoingRole = OutgoingRole.get(this, agentNumber, false, false);
    }
    
    /**
     * Returns the relation of this role.
     * 
     * @return the relation of this role.
     * 
     * @ensure return.isRoleType() : "The relation is a role type.";
     */
    @Pure
    public @Nonnull SemanticType getRelation() {
        return relation;
    }
    
    /**
     * Returns the recipient of this role.
     * 
     * @return the recipient of this role.
     */
    @Pure
    public @Nonnull Role getRecipient() {
        return recipient;
    }
    
    
    @Pure
    @Override
    public @Nonnull OutgoingRole getAgent() {
        return outgoingRole;
    }
    
    
    /**
     * Caches the non-native roles given their client and number.
     */
    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, NonNativeRole>> index = new ConcurrentHashMap<Client, ConcurrentMap<Long, NonNativeRole>>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Client.DELETED);
        }
    }
    
    /**
     * Returns the potentially locally cached non-native role with the given arguments.
     * 
     * @param client the client that can assume the returned role.
     * @param number the number that references the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing non-native role with the given arguments.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nonnull NonNativeRole get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull Role recipient, long agentNumber) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, NonNativeRole> map = index.get(client);
            if (map == null) map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, NonNativeRole>());
            @Nullable NonNativeRole role = map.get(number);
            if (role == null) role = map.putIfAbsentElseReturnPresent(number, new NonNativeRole(client, number, issuer, relation, recipient, agentNumber));
            return role;
        } else {
            return new NonNativeRole(client, number, issuer, relation, recipient, agentNumber);
        }
    }
    
    /**
     * Returns a new or existing non-native role with the given arguments.
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing non-native role with the given arguments.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    static @Nonnull NonNativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, @Nonnull Role recipient, long agentNumber) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        final @Nonnull NonNativeRole role = get(client, RoleModule.map(client, issuer, relation, recipient, agentNumber), issuer, relation, recipient, agentNumber);
        role.notify(CREATED);
        return role;
    }
    
    @Override
    public void remove() throws SQLException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, NonNativeRole> map = index.get(getClient());
            if (map != null) map.remove(getNumber());
        }
        super.remove();
    }
    
}
