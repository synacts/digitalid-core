package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * A state selector returns the SQL condition with which the returned state is restricted.
 * 
 * @see ConceptPropertyTable
 */
@Stateless
public abstract class RequiredAuthorization<C extends Concept<?, ?>, V> {
    
    /**
     * Returns the SQL condition with which the returned state is restricted.
     */
    @Pure
    // TODO: Return an SQLNode instead. Improve the flexibility so that tables can also be joined.
    public abstract @Nonnull String getStateFilter(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent);
    
    @Pure
    public abstract @Nonnull ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull C concept);
    
    /**
     * Returns the agent that is required to execute the method.
     */
    @Pure
    public @Nullable Agent getRequiredAgentToExecuteMethod(@Nonnull C concept) {
        return null;
    }
    
    // Possible alternative:
    
    @Pure
    @Default("(concept, value) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull C, @Nonnull V, @Nullable Agent> getRequiredAgentToExecuteMethod();
    
}
