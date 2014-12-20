package ch.virtualid.contact;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlySet;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link AttributeTypeSet attribute type sets} and should <em>never</em> be cast away.
 * 
 * @see AttributeTypeSet
 * @see ReadonlyAuthentications
 * @see ReadonlyContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ReadonlyAttributeTypeSet extends ReadonlySet<SemanticType>, Blockable {
    
    /**
     * Returns whether this attribute type set contains a single element.
     * 
     * @return whether this attribute type set contains a single element.
     */
    @Pure
    public boolean areSingle();
    
    /**
     * Returns this attribute type set as agent as agent permissions for reading.
     * 
     * @return this attribute type set as agent as agent permissions for reading.
     */
    @Pure
    public @Capturable @Nonnull AgentPermissions toAgentPermissions();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull AttributeTypeSet clone();
    
}
