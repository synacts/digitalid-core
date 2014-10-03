package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.contact.Context;
import ch.virtualid.credential.Credential;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.exceptions.packet.PacketException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models an outgoing role that acts as an agent on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class OutgoingRole extends Agent implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the aspect of the relation being changed at the observed outgoing role.
     */
    public static final @Nonnull Aspect RELATION = new Aspect(OutgoingRole.class, "relation changed");
    
    /**
     * Stores the aspect of the context being changed at the observed outgoing role.
     */
    public static final @Nonnull Aspect CONTEXT = new Aspect(OutgoingRole.class, "context changed");
    
    
    /**
     * Stores the relation between the issuing and the receiving identity.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private @Nullable SemanticType relation;
    
    /**
     * Stores the context to which this outgoing role is assigned.
     */
    private @Nullable Context context;
    
    /**
     * Stores whether this outgoing role can be restricted.
     */
    private final boolean restrictable;
    
    /**
     * Creates a new outgoing role with the given entity and number.
     * 
     * @param entity the entity to which this outgoing role belongs.
     * @param number the number that references this outgoing role.
     * @param removed whether this outgoing role has been removed.
     * @param restrictable whether the outgoing role can be restricted.
     */
    private OutgoingRole(@Nonnull Entity entity, long number, boolean removed, boolean restrictable) {
        super(entity, number, removed);
        
        this.restrictable = restrictable;
    }
    
    /**
     * Creates a new outgoing role at the given role.
     * 
     * @param role the role to which the outgoing role belongs.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which the outgoing role is assigned.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nonnull OutgoingRole create(@Nonnull Role role, @Nonnull SemanticType relation, @Nonnull Context context) {
        assert relation.isRoleType() : "The relation is a role type.";
        
        final @Nonnull OutgoingRole outgoingRole = get(role, new SecureRandom().nextLong(), false);
        Synchronizer.execute(new OutgoingRoleCreate(outgoingRole, relation, context));
        return outgoingRole;
    }
    
    /**
     * Creates the given outgoing role in the database.
     * 
     * @param outgoingRole the outgoing role to create in the database.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which the outgoing role is assigned.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @OnlyForActions
    public static void createForActions(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType relation, @Nonnull Context context) {
        assert relation.isRoleType() : "The relation is a role type.";
        
        Agents.create(outgoingRole, relation, context);
        outgoingRole.notify(Agent.CREATED);
    }
    
    
    /**
     * Returns the relation between the issuing and the receiving identity.
     * 
     * @return the relation between the issuing and the receiving identity.
     * 
     * @ensure relation.isRoleType() : "The relation is a role type.";
     */
    @Pure
    public @Nonnull SemanticType getRelation() {
        if (relation == null) {
            relation = Agents.getRelation(this);
        }
        return relation;
    }
    
    /**
     * Sets the relation of this outgoing role.
     * 
     * @param newRelation the new relation of this outgoing role.
     * 
     * @require newRelation.isRoleType() : "The new relation is a role type.";
     */
    public void setRelation(@Nonnull SemanticType newRelation) throws SQLException {
        assert newRelation.isRoleType() : "The new relation is a role type.";
        
        final @Nonnull SemanticType oldRelation = getRelation();
        if (!newRelation.equals(oldRelation)) {
            Synchronizer.execute(new OutgoingRoleRelationReplace(this, oldRelation, newRelation));
        }
    }
    
    /**
     * Replaces the relation of this outgoing role.
     * 
     * @param oldRelation the old relation of this outgoing role.
     * @param newRelation the new relation of this outgoing role.
     * 
     * @require oldRelation.isRoleType() : "The old relation is a role type.";
     * @require newRelation.isRoleType() : "The new relation is a role type.";
     */
    @OnlyForActions
    public void replaceRelation(@Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) throws SQLException {
        assert oldRelation.isRoleType() : "The old relation is a role type.";
        assert newRelation.isRoleType() : "The new relation is a role type.";
        
        Agents.replaceRelation(this, oldRelation, newRelation);
        relation = newRelation;
        notify(RELATION);
    }
    
    
    /**
     * Returns the context to which this outgoing role is assigned.
     * 
     * @return the context to which this outgoing role is assigned.
     */
    @Pure
    public @Nonnull Context getContext() {
        if (context == null) {
            context = Agents.getContext(this);
        }
        return context;
    }
    
    /**
     * Sets the context of this outgoing role.
     * 
     * @param newContext the new context of this outgoing role.
     */
    public void setContext(@Nonnull Context newContext) throws SQLException {
        final @Nonnull Context oldContext = getContext();
        if (!newContext.equals(oldContext)) {
            Synchronizer.execute(new OutgoingRoleContextReplace(this, oldContext, newContext));
        }
    }
    
    /**
     * Replaces the context of this outgoing role.
     * 
     * @param oldContext the old context of this outgoing role.
     * @param newContext the new context of this outgoing role.
     */
    @OnlyForActions
    public void replaceContext(@Nonnull Context oldContext, @Nonnull Context newContext) throws SQLException {
        Agents.replaceContext(this, oldContext, newContext);
        context = newContext;
        notify(CONTEXT);
    }
    
    
    @Override
    public boolean isClient() {
        return false;
    }
    
    
    /**
     * Caches outgoing roles given their entity and number.
     */
    private static final @Nonnull Map<Pair<Entity, Long>, OutgoingRole> index = new HashMap<Pair<Entity, Long>, OutgoingRole>();
    
    // TODO: Make similar changes as in the Context class.
    
    /**
     * Returns a (locally cached) outgoing role that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the outgoing role belongs.
     * @param number the number that denotes the outgoing role.
     * @param removed whether the outgoing role has been removed.
     * @param restrictable whether the outgoing role can be restricted.
     * 
     * @return a new or existing outgoing role with the given entity and number.
     */
    @Pure
    public static @Nonnull OutgoingRole get(@Nonnull Entity entity, long number, boolean removed, boolean restrictable) {
        if (!restrictable && Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Entity, Long> pair = new Pair<Entity, Long>(entity, number);
                @Nullable OutgoingRole outgoingRole = index.get(pair);
                if (outgoingRole == null) {
                    outgoingRole = new OutgoingRole(entity, number, removed, restrictable);
                    index.put(pair, outgoingRole);
                }
                return outgoingRole;
            }
        } else {
            return new OutgoingRole(entity, number, removed, restrictable);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the outgoing role belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * @param removed whether the outgoing role has been removed.
     * @param restrictable whether the outgoing role can be restricted.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull OutgoingRole get(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex, boolean removed, boolean restrictable) throws SQLException {
        return get(entity, resultSet.getLong(columnIndex), removed, restrictable);
    }
    
    
    /**
     * Checks whether this outgoing role covers the given credential and throws a {@link PacketException} if not.
     * 
     * @param credential the credential that needs to be covered.
     * 
     * @require credential.getPermissions() != null : "The permissions of the credential are not null.";
     * @require credential.getRestrictions() != null : "The restrictions of the credential are not null.";
     */
    public void checkCovers(@Nonnull Credential credential) throws SQLException, PacketException {
        final @Nullable ReadonlyAgentPermissions permissions = credential.getPermissions();
        assert permissions != null : "The permissions of the credential are not null.";
        final @Nullable Restrictions restrictions = credential.getRestrictions();
        assert restrictions != null : "The restrictions of the credential are not null.";
        
        getPermissions().checkCover(permissions);
        getRestrictions().checkCover(restrictions);
    }
    
    /**
     * Returns whether this outgoing role can be restricted.
     * 
     * @return whether this outgoing role can be restricted.
     */
    public boolean isRestrictable() {
        return restrictable;
    }
    
    /**
     * Restricts this outgoing role to the permissions and restrictions of the given credential.
     * 
     * @param credential the credential with which to restrict this outgoing role.
     * 
     * @require isRestrictable() : "This outgoing role can be restricted.";
     * @require credential.isRoleBased() : "The credential is role-based.";
     */
    public void restrictTo(@Nonnull Credential credential) throws SQLException {
        assert isRestrictable() : "This outgoing role can be restricted.";
        assert credential.isRoleBased() : "The credential is role-based.";
        
        if (permissions == null) getPermissions();
        assert permissions != null;
        permissions.restrictTo(credential.getPermissionsNotNull());
        
        if (restrictions == null) getRestrictions();
        assert restrictions != null;
        restrictions = restrictions.restrictTo(credential.getRestrictionsNotNull());
    }
    
}
