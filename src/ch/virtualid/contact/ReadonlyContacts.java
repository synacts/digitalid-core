package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.util.ReadonlySet;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Contacts contacts} and should <em>never</em> be cast away.
 * 
 * @see Contacts
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ReadonlyContacts extends ReadonlySet<Contact>, Blockable, SQLizable{
    
    @Pure
    @Override
    public @Capturable @Nonnull Contacts clone();
    
}
