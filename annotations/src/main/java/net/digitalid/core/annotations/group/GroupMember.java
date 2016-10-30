package net.digitalid.core.annotations.group;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A group member belongs to a mathematical {@link GroupInterface group}.
 */
@Immutable
public interface GroupMember extends RootInterface {
    
    /**
     * Returns the group of this member.
     */
    @Pure
    public @Nonnull GroupInterface getGroup();
    
    /**
     * Returns whether this member is in the given group.
     */
    @Pure
    public default boolean isIn(@Nonnull GroupInterface group) {
        return getGroup().equals(group);
    }
    
}
