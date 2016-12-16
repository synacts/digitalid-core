package net.digitalid.core.property.map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.PropertyRequiredAuthorization;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the authorization required to modify a synchronized map property.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class MapPropertyRequiredAuthorization<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, MAP_KEY, MAP_VALUE> extends PropertyRequiredAuthorization<ENTITY, KEY, CONCEPT> {
    
    @Pure
    @Default("(concept, pair) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nonnull @Frozen ReadOnlyAgentPermissions> getRequiredPermissionsToExecuteMethod();
    
    @Pure
    @Default("(concept, pair) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nonnull Restrictions> getRequiredRestrictionsToExecuteMethod();
    
    @Pure
    @Default("(concept, pair) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nullable Agent> getRequiredAgentToExecuteMethod();
    
    @Pure
    @Default("(concept, pair) -> ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nonnull @Frozen ReadOnlyAgentPermissions> getRequiredPermissionsToSeeMethod();
    
    @Pure
    @Default("(concept, pair) -> Restrictions.MIN")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nonnull Restrictions> getRequiredRestrictionsToSeeMethod();
    
    @Pure
    @Default("(concept, pair) -> null")
    public abstract @Nonnull BinaryFunction<@Nonnull CONCEPT, @Nonnull Pair<@Nonnull MAP_KEY, @Nonnull MAP_VALUE>, @Nullable Agent> getRequiredAgentToSeeMethod();
    
}
