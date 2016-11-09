package net.digitalid.core.handler.method;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;

/**
 * This class makes sure that the validation method in the method class is properly called.
 */
@Immutable
public abstract class MethodImplementation<E extends Entity> extends RootClass implements Method<E> {
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Method.super.validate();
    }
    
}
