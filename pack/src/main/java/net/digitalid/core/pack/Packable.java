package net.digitalid.core.pack;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.pack.annotations.GeneratePacking;

/**
 * This interface is implemented by all types that can be {@link Pack packed}.
 */
@Stateless
public interface Packable extends RootInterface {
    
    /* -------------------------------------------------- Packable -------------------------------------------------- */
    
    /**
     * Packs this object.
     */
    @Pure
    @GeneratePacking
    @TODO(task = "Throw a StreamException instead.", date = "2016-12-20", author = Author.KASPAR_ETTER)
    public @Nonnull Pack pack() throws ExternalException;
    
}
