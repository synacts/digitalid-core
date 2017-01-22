package net.digitalid.core.entity;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.unit.Unit;

/**
 * A core unit is either a host or a client.
 * 
 * @invariant isHost() != isClient() : "This unit is either a host or a client.";
 */
@Immutable
@TODO(task = "Change back the exception back to External Exception as soon as the builder can handle it.", date = "2016-12-12", author = Author.KASPAR_ETTER)
public abstract class CoreUnit extends RootClassWithException<RuntimeException /* ExternalException */> implements Unit {
    
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
