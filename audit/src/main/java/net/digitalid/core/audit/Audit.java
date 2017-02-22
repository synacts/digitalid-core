package net.digitalid.core.audit;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models an audit with a time and trail.
 * 
 * @see RequestAudit
 * @see ResponseAudit
 */
@Immutable
public abstract class Audit extends RootClass {
    
    /* -------------------------------------------------- Last Time -------------------------------------------------- */
    
    /**
     * Returns the time of the last audit.
     */
    @Pure
    public abstract @Nonnull Time getLastTime();
    
}
