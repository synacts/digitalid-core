package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.reference.Capturable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.block.wrappers.Blockable;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.identity.SemanticType;

/**
 * This interface provides read-only access to {@link FreezableAttributeTypeSet attribute type sets} and should <em>never</em> be cast away.
 * 
 * @see FreezableAttributeTypeSet
 * @see ReadonlyAuthentications
 * @see ReadonlyContactPermissions
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
