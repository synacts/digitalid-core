package net.digitalid.core.restrictions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;

/**
 * This class models the restrictions of an agent's authorization.
 * 
 * TODO: Also allow to restrict the visible history (as in the concepts paper).
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class Restrictions extends RootClass {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores the weakest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MIN = RestrictionsBuilder.build();
    
    /**
     * Stores the strongest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MAX = RestrictionsBuilder.withOnlyForClients(true).withAssumeRoles(true).withWriteToNode(true).build();
    
    /**
     * Stores the restrictions required to modify clients.
     */
    public static final @Nonnull Restrictions ONLY_FOR_CLIENTS = RestrictionsBuilder.withOnlyForClients(true).build();
    
    /**
     * Stores the restrictions required to assume roles.
     */
    public static final @Nonnull Restrictions CAN_ASSUME_ROLES = RestrictionsBuilder.withAssumeRoles(true).build();
    
    /**
     * Stores the restrictions required to write to the node.
     */
    public static final @Nonnull Restrictions CAN_WRITE_TO_NODE = RestrictionsBuilder.withWriteToNode(true).build();
    
    /* -------------------------------------------------- Only for Clients -------------------------------------------------- */
    
    /**
     * Returns whether the authorization is restricted to clients (instead of roles).
     */
    @Pure
    @Default("false")
    public abstract boolean isOnlyForClients();
    
    /**
     * Checks that the authorization is restricted to clients and throws a {@link RequestException} otherwise.
     */
    @Pure
    public void checkIsOnlyForClients() throws RequestException {
        if (!isOnlyForClients()) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage("The authorization is restricted to clients.").build(); }
    }
    
    /* -------------------------------------------------- Assume Roles -------------------------------------------------- */
    
    /**
     * Returns whether the authorization is restricted to agents that can assume roles.
     */
    @Pure
    @Default("false")
    public abstract boolean canAssumeRoles();
    
    /**
     * Checks that the authorization is restricted to agents that can assume roles and throws a {@link RequestException} otherwise.
     */
    @Pure
    public void checkCanAssumeRoles() throws RequestException {
        if (!canAssumeRoles()) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage("The authorization is restricted to agents that can assume roles.").build(); }
    }
    
    /* -------------------------------------------------- Write to Node -------------------------------------------------- */
    
    /**
     * Returns whether the authorization is restricted to agents that can write to the node.
     */
    @Pure
    @Default("false")
    public abstract boolean canWriteToNode();
    
    /**
     * Checks that the authorization is restricted to agents that can write to the node and throws a {@link RequestException} otherwise.
     */
    @Pure
    public void checkCanWriteToNode() throws RequestException {
        if (!canWriteToNode()) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage("The authorization is restricted to agents that can write to the node.").build(); }
    }
    
    /* -------------------------------------------------- Node -------------------------------------------------- */
    
    /**
     * Returns the node to which the authorization is restricted (or null).
     */
    @Pure
    public abstract @Nullable Node getNode();
    
    /**
     * Returns whether these restrictions cover the given node.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Node node) throws DatabaseException {
        final @Nullable Node thisNode = getNode();
        return thisNode != null && thisNode.isSupernodeOf(node);
    }
    
    /**
     * Checks that these restrictions cover the given node and throws a {@link RequestException} otherwise.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Node node) throws DatabaseException, RequestException {
        if (!cover(node)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("The authorization is restricted to agents that cover the node $.", node)).build(); }
    }
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether these restrictions cover the given restrictions.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Restrictions restrictions) throws DatabaseException {
        if (restrictions.isOnlyForClients() && !isOnlyForClients()) { return false; }
        if (restrictions.canAssumeRoles() && !canAssumeRoles()) { return false; }
        if (restrictions.canWriteToNode() && !canWriteToNode()) { return false; }
        final @Nullable Node node = restrictions.getNode();
        return node == null || cover(node);
    }
    
    /**
     * Checks whether these restrictions cover the given restrictions and throws a {@link RequestException} otherwise.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Restrictions restrictions) throws DatabaseException, RequestException {
        if (!cover(restrictions)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("The authorization is restricted to agents that cover the restrictions $.", restrictions)).build(); }
    }
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Restricts these restrictions to the given restrictions (except the node, which is left unaffected).
     */
    @Pure
    public @Nonnull Restrictions restrictTo(@Nonnull Restrictions restrictions) {
        return RestrictionsBuilder.withOnlyForClients(isOnlyForClients() && restrictions.isOnlyForClients()).withAssumeRoles(canAssumeRoles() && restrictions.canAssumeRoles()).withWriteToNode(canWriteToNode() && restrictions.canWriteToNode()).withNode(getNode()).build();
    }
    
}
