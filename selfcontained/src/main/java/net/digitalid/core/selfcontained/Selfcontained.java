package net.digitalid.core.selfcontained;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.file.existence.Existent;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * A selfcontained object also contains its type.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class Selfcontained {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    @Pure
    protected abstract @Nonnull byte[] getObject();
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Throw a more specific exception? ", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public <T, E> @Nullable T recover(@Nonnull Converter<T, E> converter, E externallyProvided) throws ExternalException {
        return XDF.recover(converter, externallyProvided, getObject());
    }
    
    @Pure
    @TODO(task = "Throw a more specific exception?", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public static <T> @Nonnull Selfcontained convert(@Nullable T object, @Nonnull Converter<T, ?> converter) throws ExternalException {
        final /* @Nonnull */ SemanticType semanticType = null; // TODO: Derive from the given converter.
        return new SelfcontainedSubclass(semanticType, XDF.convert(object, converter));
    }
    
    /* -------------------------------------------------- Load -------------------------------------------------- */
    
    @Pure
    public static @Nonnull Selfcontained loadFrom(@NonCaptured @Modified @Nonnull InputStream inputStream) throws ExternalException {
        final @Nullable Selfcontained selfcontained = XDF.recover(SelfcontainedConverter.INSTANCE, null, inputStream);
        if (selfcontained == null) { throw ExternalException.with("The recovered object should not be null."); }
        return selfcontained;
    }
    
    @Pure
    public static @Nonnull Selfcontained loadFrom(@Nonnull @Existent File file) throws ExternalException {
        try {
            return loadFrom(new FileInputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw ExternalException.with(exception);
        }
    }
    
    /* -------------------------------------------------- Store -------------------------------------------------- */
    
    @Pure
    public void storeTo(@NonCaptured @Modified @Nonnull OutputStream outputStream) throws ExternalException {
        XDF.convert(this, SelfcontainedConverter.INSTANCE, outputStream);
    }
    
    @Pure
    public void storeTo(@Nonnull File file) throws ExternalException {
        try {
            storeTo(new FileOutputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw ExternalException.with(exception);
        }
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
