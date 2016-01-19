package net.digitalid.core.conversion.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Pure;

/**
 * Objects of classes that implement this interface can be converted.
 * 
 * @param <O> the type of the objects that the converter can store and restore, which is typically the declaring class itself.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an Entity.
 *            In case no external information is needed for the restoration of an object, declare it as an Object.
 */
public interface Convertible<O, E> {
    
    /**
     * Returns the factory to consume and produce objects of this class.
     * 
     * @return the factory to consume and produce objects of this class.
     */
    @Pure
    public @Nonnull Factory<O, E> getFactory();
    
}
