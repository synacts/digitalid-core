package net.digitalid.core.entity;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.subject.site.Site;

/**
 * A core site is either a host or a client.
 * 
 * @invariant isHost() != isClient() : "This site is either a host or a client.";
 */
@Immutable
@TODO(task = "Change back the exception back to External Exception as soon as the builder can handle it.", date = "2016-12-12", author = Author.KASPAR_ETTER)
public abstract class CoreSite<SITE extends CoreSite<?>> extends RootClassWithException<RuntimeException /* ExternalException */> implements Site<SITE> {
    
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
    public abstract boolean isClient();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(isHost() != isClient()).orThrow("This site $ has to be either a host or a client.", this);
    }
    
}
