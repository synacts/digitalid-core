package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Objects of classes that implement this interface can be encoded as a {@link Block block}.
 * 
 * @param <O> the type of the objects that the converter can encode and decode, which is typically the declaring class itself.
 * @param <E> the type of the external object that is needed to decode an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of an object, declare it as an {@link Object}.
 */
public interface XDF<O, E> {
    
    /**
     * Returns the converter to encode and decode objects of this class.
     * 
     * @return the converter to encode and decode objects of this class.
     */
    @Pure
    public @Nonnull AbstractXDFConverter<O, E> getXDFConverter();
    
}
