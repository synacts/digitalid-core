package net.digitalid.core.unit.annotations;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This interface allows to use annotations on unit-based objects.
 * 
 * @see OnHost
 * @see OnClient
 * @see OnHostRecipient
 * @see OnClientRecipient
 */
@Immutable
public interface UnitBased extends RootInterface {
    
    /**
     * Returns whether this object is on a host.
     */
    @Pure
    public boolean isOnHost();
    
    /**
     * Returns whether this object is on a client.
     */
    @Pure
    public boolean isOnClient();
    
}
