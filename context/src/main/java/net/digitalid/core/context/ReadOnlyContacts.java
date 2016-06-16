package net.digitalid.core.context;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.core.contact.Contact;

import net.digitalid.service.core.block.wrappers.Blockable;
import net.digitalid.service.core.database.SQLizable;

/**
 * This interface provides read-only access to {@link FreezableContacts contacts} and should <em>never</em> be cast away.
 * 
 * @see FreezableContacts
 */
public interface ReadOnlyContacts extends ReadOnlySet<Contact>, Blockable, SQLizable {
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContacts clone();
    
}
