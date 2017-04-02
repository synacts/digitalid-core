package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.functional.interfaces.UnaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.tuples.Triplet;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.dialect.expression.bool.SQLBooleanExpression;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.subject.CoreSubject;

/**
 * This type models the authorization required to modify a synchronized property and retrieve its state.
 * Please note that in case of map properties, the authorization may only depend on the key of the map because an
 * agent might not realize otherwise, due to its limited authorization, when it replaces an already existing entry.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface RequiredAuthorization<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, VALUE> {
    
    /**
     * Returns a function that determines the required permissions to set or add the given value to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nonnull @Frozen ReadOnlyAgentPermissions> getRequiredPermissionsToExecuteMethod();
    
    /**
     * Returns a function that determines the required restrictions to set or add the given value to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nonnull Restrictions> getRequiredRestrictionsToExecuteMethod();
    
    /**
     * Returns a function that determines the required agent to set or add the given value to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nullable Agent> getRequiredAgentToExecuteMethod();
    
    /**
     * Returns a function that determines the required permissions to see the given value being set or added to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nonnull @Frozen ReadOnlyAgentPermissions> getRequiredPermissionsToSeeMethod();
    
    /**
     * Returns a function that determines the required restrictions to see the given value being set or added to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nonnull Restrictions> getRequiredRestrictionsToSeeMethod();
    
    /**
     * Returns a function that determines the required agent to see the given value being set or added to the property of the given concept.
     */
    @Pure
    @Default("(concept, value) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull SUBJECT, @Nonnull VALUE, @Nullable Agent> getRequiredAgentToSeeMethod();
    
    /**
     * Returns a function that determines, based on the given permissions, restrictions and agent, the condition with which the returned state is restricted.
     */
    @Pure
    @Default("triplet -> net.digitalid.database.dialect.expression.bool.SQLBooleanLiteral.TRUE")
    @TODO(task = "Improve the flexibility so that tables can also be joined.", date = "2017-01-19", author = Author.KASPAR_ETTER)
    public abstract @Nonnull UnaryFunction<@Nonnull Triplet<@Nonnull ReadOnlyAgentPermissions, @Nonnull Restrictions, @Nullable Agent>, @Nonnull SQLBooleanExpression> getStateFilter();
    
}
