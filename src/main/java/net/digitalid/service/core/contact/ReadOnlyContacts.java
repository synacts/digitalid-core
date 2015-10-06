package net.digitalid.service.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * This interface provides read-only access to {@link FreezableContacts contacts} and should <em>never</em> be cast away.
 * 
 * @see FreezableContacts
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface ReadOnlyContacts extends ReadOnlySet<Contact>, Blockable, SQLizable {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContacts clone();
    
}
