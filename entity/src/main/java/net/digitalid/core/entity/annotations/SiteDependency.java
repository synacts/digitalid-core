package net.digitalid.core.entity.annotations;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;

/**
 * This interface allows to use annotations on site-dependent objects.
 * 
 * @see Entity
 * @see OnHost
 * @see OnClient
 * @see OnHostRecipient
 * @see OnClientRecipient
 */
@Immutable
public interface SiteDependency extends RootInterface {
    
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
