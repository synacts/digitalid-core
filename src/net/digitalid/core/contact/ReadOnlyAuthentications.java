package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Blockable;

/**
 * This interface provides read-only access to {@link Authentications authentications} and should <em>never</em> be cast away.
 * 
 * @see Authentications
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyAuthentications extends ReadOnlyAttributeTypeSet, Blockable {
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone();
    
}
