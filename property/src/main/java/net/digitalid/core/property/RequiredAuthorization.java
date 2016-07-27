package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.dialect.ast.expression.bool.SQLBooleanExpression;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.nonnullable.NonNullableRequiredAuthorization;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the authorization required to modify a concept property and retrieve its state.
 * 
 * @see NonNullableRequiredAuthorization
 */
@Immutable
public abstract class RequiredAuthorization<C extends Concept<?, ?>, V> {
    
    /**
     * Returns the condition with which the returned state is restricted.
     */
    @Pure
    // TODO: Improve the flexibility so that tables can also be joined.
    public abstract @Nonnull SQLBooleanExpression getStateFilter(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent);
    
}
