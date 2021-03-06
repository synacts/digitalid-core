/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.pack;

import java.io.File;
import java.io.InputStream;
import java.net.Socket;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.ownership.Shared;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.file.existence.Existent;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SyntacticType;

/**
 * A pack combines the serialization of its content with its type.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class Pack extends RootClass {
    
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
    public <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE unpack(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided) throws RecoveryException {
        return XDF.recover(converter, provided, getBytes());
    }
    
    /**
     * Packs the given object with the given converter by serializing its content and using the given semantic type.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Nonnull Pack pack(@Nonnull Converter<TYPE, ?> converter, @Nonnull TYPE object, @Nonnull SemanticType type) {
        return new PackSubclass(type, XDF.convert(converter, object));
    }
    
    /**
     * Packs the given object with the given converter by serializing its content and deriving the type from the given converter.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Nonnull Pack pack(@Nonnull Converter<TYPE, ?> converter, @Nonnull TYPE object) {
        return pack(converter, object, SemanticType.mapWithoutPersistingResult(converter));
    }
    
    /* -------------------------------------------------- Load -------------------------------------------------- */
    
    /**
     * Loads a pack from the given input stream.
     * <p>
     * If the input stream comes from a file, use {@link #loadFrom(java.io.File)} instead.
     * If the input stream comes from a socket, use {@link #loadFrom(java.net.Socket)} instead.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@NonCaptured @Modified @Nonnull InputStream inputStream) throws RecoveryException, MemoryException {
        return XDF.recover(PackConverter.INSTANCE, null, inputStream);
    }
    
    /**
     * Loads a pack from the given bytes.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@NonCaptured @Unmodified @Nonnull byte[] bytes) throws RecoveryException {
        return XDF.recover(PackConverter.INSTANCE, null, bytes);
    }
    
    /**
     * Loads a pack from the given file.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@Nonnull @Existent File file) throws RecoveryException, FileException {
        return XDF.recover(PackConverter.INSTANCE, null, file);
    }
    
    /**
     * Loads a pack from the given socket.
     */
    @Pure
    public static @Nonnull Pack loadFrom(@Nonnull Socket socket) throws RecoveryException, NetworkException {
        return XDF.recover(PackConverter.INSTANCE, null, socket);
    }
    
    /* -------------------------------------------------- Store -------------------------------------------------- */
    
    /**
     * Returns this pack as a byte array.
     */
    @Pure
    public @Capturable @Nonnull byte[] store() {
        return XDF.convert(PackConverter.INSTANCE, this);
    }
    
    /**
     * Stores this pack to the given file.
     */
    @Pure
    public void storeTo(@Nonnull File file) throws FileException {
        XDF.convert(PackConverter.INSTANCE, this, file);
    }
    
    /**
     * Stores this pack to the given socket.
     */
    @Pure
    public void storeTo(@Nonnull Socket socket) throws NetworkException {
        XDF.convert(PackConverter.INSTANCE, this, socket);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("Pack(type: ").append(getType().getAddress());
        if (getType().isBasedOn(SyntacticType.STRING) || getType().isBasedOn(SyntacticType.STRING64)) {
            try { string.append(", bytes: ").append(Quotes.inDouble(unpack(StringConverter.INSTANCE, null))); }
            catch (@Nonnull RecoveryException exception) { Log.warning("Could not recover a string.", exception); }
        }
        return string.append(")").toString();
    }
    
}
