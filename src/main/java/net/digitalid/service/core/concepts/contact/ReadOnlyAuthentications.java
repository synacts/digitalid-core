package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;

/**
 * This interface provides read-only access to {@link FreezableAuthentications authentications} and should <em>never</em> be cast away.
 * 
 * @see FreezableAuthentications
 */
public interface ReadOnlyAuthentications extends ReadOnlyAttributeTypeSet {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAuthentications clone();
    
}
