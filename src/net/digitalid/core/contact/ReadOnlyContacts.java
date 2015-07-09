package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadonlySet;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This interface provides readonly access to {@link Contacts contacts} and should <em>never</em> be cast away.
 * 
 * @see Contacts
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 2.0
 */
public interface ReadonlyContacts extends ReadonlySet<Contact>, Blockable, SQLizable{
    
    @Pure
    @Override
    public @Capturable @Nonnull Contacts clone();
    
}
