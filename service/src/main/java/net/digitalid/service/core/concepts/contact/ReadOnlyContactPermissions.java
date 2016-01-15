package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.reference.Capturable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This interface provides read-only access to {@link FreezableContactPermissions contact permissions} and should <em>never</em> be cast away.
 * 
 * @see FreezableContactPermissions
 */
public interface ReadOnlyContactPermissions extends ReadOnlyAttributeTypeSet {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContactPermissions clone();
    
}
