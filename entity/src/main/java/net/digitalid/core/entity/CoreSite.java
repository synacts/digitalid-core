package net.digitalid.core.entity;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.core.Site;

/**
 * This class allows the same code to work on both hosts and clients.
 */
@Mutable
public abstract class CoreSite implements Site {
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this site is a host.
     */
    @Pure
    public abstract boolean isHost();
    
    /**
     * Returns whether this site is a client.
     */
    @Pure
    public final boolean isClient() {
        return !isHost();
    }
    
}
