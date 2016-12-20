package net.digitalid.core.pack;

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
 * A pack combines the serialization of its content with its type.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class Pack {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the type of this pack.
     */
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    /**
     * Returns the bytes of the serialized content.
     */
    @Pure
    protected abstract @Nonnull byte[] getBytes();
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    /**
     * Unpacks this pack with the given converter and the provided object.
     */
    @Pure
    @TODO(task = "Throw a recovery and stream exception instead!", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public <TYPE, PROVIDED> @Nullable TYPE unpack(@Nonnull Converter<TYPE, PROVIDED> converter, PROVIDED provided) throws ExternalException {
        return XDF.recover(converter, provided, getBytes());
    }
    
    /**
     * Packs the given object with the given converter by serializing its content and deriving the type from the given converter.
     */
    @Pure
    @TODO(task = "Throw a stream exception instead!", date = "2016-10-31", author = Author.KASPAR_ETTER)
    public static <TYPE> @Nonnull Pack pack(@Nullable TYPE object, @Nonnull Converter<TYPE, ?> converter) throws ExternalException {
        return new PackSubclass(SemanticType.map(converter), XDF.convert(object, converter));
    }
    
    /* -------------------------------------------------- Load -------------------------------------------------- */
    
    /**
     * Loads a pack from the given input stream.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@NonCaptured @Modified @Nonnull InputStream inputStream) throws ExternalException {
        final @Nullable Pack pack = XDF.recover(PackConverter.INSTANCE, null, inputStream);
        if (pack == null) { throw ExternalException.with("The recovered object should not be null."); }
        return pack;
    }
    
    /**
     * Loads a pack from the given file.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@Nonnull @Existent File file) throws ExternalException {
        try {
            return loadFrom(new FileInputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw ExternalException.with(exception);
        }
    }
    
    /* -------------------------------------------------- Store -------------------------------------------------- */
    
    /**
     * Stores this pack to the given output stream.
     */
    @Pure
    public void storeTo(@NonCaptured @Modified @Nonnull OutputStream outputStream) throws ExternalException {
        XDF.convert(this, PackConverter.INSTANCE, outputStream);
    }
    
    /**
     * Stores this pack to the given file.
     */
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
        return "Pack";
    }
    
}
