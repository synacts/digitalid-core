package net.digitalid.core.unit;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.unit.Unit;

/**
 * A core unit is either a host or a client.
 * 
 * @invariant isHost() != isClient() : "This unit is either a host or a client.";
 */
@Immutable
public abstract class CoreUnit extends RootClassWithException<ExternalException> implements Unit {
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this unit is a host.
     */
    @Pure
    public abstract boolean isHost();
    
    /**
     * Returns whether this unit is a client.
     */
    @Pure
    public abstract boolean isClient();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(isHost() != isClient()).orThrow("This unit $ has to be either a host or a client.", this);
    }
    
}
