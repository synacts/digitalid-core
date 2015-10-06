package net.digitalid.service.core.contact;

import javax.annotation.Nonnull;
import net.digitalid.service.core.agent.FreezableAgentPermissions;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * This interface provides read-only access to {@link FreezableAttributeTypeSet attribute type sets} and should <em>never</em> be cast away.
 * 
 * @see FreezableAttributeTypeSet
 * @see ReadonlyAuthentications
 * @see ReadonlyContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface ReadOnlyAttributeTypeSet extends ReadOnlySet<SemanticType>, Blockable {
    
    /**
     * Returns this attribute type set as agent as agent permissions for reading.
     * 
     * @return this attribute type set as agent as agent permissions for reading.
     */
    @Pure
    public @Capturable @Nonnull FreezableAgentPermissions toAgentPermissions();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAttributeTypeSet clone();
    
}
