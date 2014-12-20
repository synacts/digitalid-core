package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Blockable;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link ContactPermissions contact permissions} and should <em>never</em> be cast away.
 * 
 * @see ContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ReadonlyContactPermissions extends ReadonlyAttributeTypeSet, Blockable {
    
    @Pure
    @Override
    public @Capturable @Nonnull ContactPermissions clone();
    
}
