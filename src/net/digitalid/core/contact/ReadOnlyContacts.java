package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlySet;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This interface provides read-only access to {@link FreezableContacts contacts} and should <em>never</em> be cast away.
 * 
 * @see FreezableContacts
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 2.0
 */
public interface ReadOnlyContacts extends ReadOnlySet<Contact>, Blockable, SQLizable {
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableContacts clone();
    
}
