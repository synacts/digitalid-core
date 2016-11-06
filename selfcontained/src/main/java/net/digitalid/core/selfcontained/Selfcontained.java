package net.digitalid.core.selfcontained;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identity.SemanticType;

/**
 *
 */
@GenerateSubclass
@GenerateConverter
public abstract class Selfcontained {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    public abstract @Nonnull SemanticType getSemanticType();
    
    @Pure
    protected abstract @Nonnull byte[] getObject();
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Throw a more specific exception? Introduce the possibility to provide an external object?", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public <T> @Nullable T recover(@Nonnull Converter<T, Void> converter) throws ExternalException {
        return XDF.recover(converter, getObject());
    }
    
    @Pure
    @TODO(task = "Throw a more specific exception?", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public static <T> @Nonnull Selfcontained convert(@Nullable T object, @Nonnull Converter<T, ?> converter) throws ExternalException {
        final /* @Nonnull */ SemanticType semanticType = null; // TODO: Derive from the given converter.
        return new SelfcontainedSubclass(semanticType, XDF.convert(object, converter));
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Print the object only if the semantic type is based on a string.", date = "2016-11-01", author = Author.KASPAR_ETTER)
    public @Nonnull String toString() {
//        string.append(attributeContent.getType().getAddress().getString());
//        if (attributeContent.getType().isBasedOn(StringWrapper.XDF_TYPE)) {
//            string.append(": ").append(StringWrapper.decodeNonNullable(attributeContent));
//        }
        return "Selfcontained";
    }
    
}
