package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.functional.interfaces.UnaryFunction;
import net.digitalid.utility.tuples.Triplet;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.dialect.ast.expression.bool.SQLBooleanExpression;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorization;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the authorization required to modify a synchronized property and retrieve its state.
 * 
 * @see ValuePropertyRequiredAuthorization
 */
@Immutable
@TODO(task = "Maybe remove the generic parameters as they are not needed (at the moment).", date = "2016-11-12", author = Author.KASPAR_ETTER)
public abstract class PropertyRequiredAuthorization<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>> {
    
    /**
     * Returns the condition with which the returned state is restricted.
     */
    @Pure
    // TODO: Improve the flexibility so that tables can also be joined.
    @Default("null") // TODO: Make the return value non-null.
    public abstract @Nullable UnaryFunction<@Nonnull Triplet<@Nonnull ReadOnlyAgentPermissions, @Nonnull Restrictions, @Nullable Agent>, @Nonnull SQLBooleanExpression<?>> getStateFilter();
    
}
