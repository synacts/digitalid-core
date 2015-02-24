package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Blockable;

/**
 * This interface provides readonly access to {@link Authentications authentications} and should <em>never</em> be cast away.
 * 
 * @see Authentications
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadonlyAuthentications extends ReadonlyAttributeTypeSet, Blockable {
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone();
    
}
