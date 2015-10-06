package net.digitalid.core.factory;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.wrappers.Block;

/**
 * Objects of classes that implement this interface can be stored as a {@link Block block} or in the {@link Database database}.
 * 
 * @param <O> the type of the objects that the factory can store and restore, which is typically the declaring class itself.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface Storable<O, E> {
    
    /**
     * Returns the factory to store and restore objects of this class.
     * 
     * @return the factory to store and restore objects of this class.
     */
    @Pure
    public @Nonnull GlobalFactory<O, E> getFactory();
    
}
