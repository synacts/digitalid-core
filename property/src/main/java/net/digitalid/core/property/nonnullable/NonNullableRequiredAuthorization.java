package net.digitalid.core.property.nonnullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the authorization required to modify a non-nullable concept property.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class NonNullableRequiredAuthorization<C extends Concept<?, ?>, V> {
    
    @Pure
    @Default("(concept, value) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nonnull ReadOnlyAgentPermissions> getRequiredPermissionsToExecuteMethod();
    
    @Pure
    @Default("(concept, value) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nonnull Restrictions> getRequiredRestrictionsToExecuteMethod();
    
    @Pure
    @Default("(concept, value) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nullable Agent> getRequiredAgentToExecuteMethod();
    
    @Pure
    @Default("(concept, value) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nonnull ReadOnlyAgentPermissions> getRequiredPermissionsToSeeMethod();
    
    @Pure
    @Default("(concept, value) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nonnull Restrictions> getRequiredRestrictionsToSeeMethod();
    
    @Pure
    @Default("(concept, value) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nullable Agent> getRequiredAgentToSeeMethod();
    
}
