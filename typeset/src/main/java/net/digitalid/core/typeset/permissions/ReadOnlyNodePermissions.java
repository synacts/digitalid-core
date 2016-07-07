package net.digitalid.core.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.annotations.method.Pure;

/**
 * This interface provides read-only access to {@link FreezableNodePermissions contact permissions} and should <em>never</em> be cast away.
 * 
 * @see FreezableNodePermissions
 */
public interface ReadOnlyNodePermissions extends ReadOnlyAttributeTypeSet {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableNodePermissions clone();
    
}
