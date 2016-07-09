package net.digitalid.core.typeset.authentications;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.typeset.ReadOnlyAttributeTypeSet;

/**
 * This interface provides read-only access to {@link FreezableAuthentications authentications} and should <em>never</em> be cast away.
 */
@ReadOnly(FreezableAuthentications.class)
public interface ReadOnlyAuthentications extends ReadOnlyAttributeTypeSet {
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAuthentications clone();
    
}
