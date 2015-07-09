package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.core.agent.AgentPermissions;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlySet;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;

/**
 * This interface provides read-only access to {@link AttributeTypeSet attribute type sets} and should <em>never</em> be cast away.
 * 
 * @see AttributeTypeSet
 * @see ReadonlyAuthentications
 * @see ReadonlyContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyAttributeTypeSet extends ReadOnlySet<SemanticType>, Blockable {
    
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
