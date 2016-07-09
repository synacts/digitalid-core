package net.digitalid.core.typeset.permissions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.freezable.annotations.NonFrozen;

import net.digitalid.core.typeset.ReadOnlyAttributeTypeSet;

/**
 * This interface provides read-only access to {@link FreezableNodePermissions node permissions} and should <em>never</em> be cast away.
 */
public interface ReadOnlyNodePermissions extends ReadOnlyAttributeTypeSet {
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableNodePermissions clone();
    
}
