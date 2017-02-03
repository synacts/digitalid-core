package net.digitalid.core.typeset;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;

/**
 * This interface provides read-only access to {@link FreezableAttributeTypeSet attribute type set} and should <em>never</em> be cast away.
 * 
 * @see ReadOnlyAuthentications
 * @see ReadOnlyNodePermissions
 */
@ReadOnly(FreezableAttributeTypeSet.class)
public interface ReadOnlyAttributeTypeSet extends ReadOnlySet<@Nonnull @AttributeType SemanticType> {
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAttributeTypeSet clone();
    
    /* -------------------------------------------------- Exports -------------------------------------------------- */
    
    /**
     * Returns this attribute type set as agent permissions with read access to all attributes.
     */
    @Pure
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions toAgentPermissions();
    
}
