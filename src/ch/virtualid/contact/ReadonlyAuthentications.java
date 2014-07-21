package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlySet;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Authentications authentications} and should <em>never</em> be cast away.
 * 
 * @see Authentications
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ReadonlyAuthentications extends ReadonlySet<SemanticType>, Blockable {
    
    /**
     * Returns whether these permissions contain a single one.
     * 
     * @return whether these permissions contain a single one.
     */
    @Pure
    public boolean areSingle();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone();
    
}
