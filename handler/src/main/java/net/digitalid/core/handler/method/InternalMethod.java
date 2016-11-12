package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 */
@Immutable
public interface InternalMethod extends Method<NonHostEntity> {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    @Default("signature == null ? null : null /* Find a way to derive it from signature.getSubject(), probably make it injectable. */")
    public @Nonnull NonHostEntity getEntity();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    @Derive("getEntity().getIdentity().getAddress()")
    public @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * Returns the restrictions required for this internal method.
     */
    @Pure
    public default @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return Restrictions.MIN;
    }
    
}
