package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlySet;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link AttributeSet attribute sets} and should <em>never</em> be cast away.
 * 
 * @see AttributeSet
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ReadonlyAttributeSet extends ReadonlySet<SemanticType>, Blockable {
    
    /**
     * Returns whether this attribute set contains a single element.
     * 
     * @return whether this attribute set contains a single element.
     */
    @Pure
    public boolean areSingle();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull AttributeSet clone();
    
}
