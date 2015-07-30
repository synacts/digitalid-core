package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlySet;
import net.digitalid.core.database.SQLizable;
import net.digitalid.core.wrappers.Blockable;

/**
 * This interface provides read-only access to {@link FreezableContacts contacts} and should <em>never</em> be cast away.
 * 
 * @see FreezableContacts
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyContacts extends ReadOnlySet<Contact>, Blockable, SQLizable {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContacts clone();
    
}
