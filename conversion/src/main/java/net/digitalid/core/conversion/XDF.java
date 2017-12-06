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
package net.digitalid.core.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.ownership.Shared;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.validation.annotations.file.existence.Existent;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.conversion.decoders.FileDecoder;
import net.digitalid.core.conversion.decoders.MemoryDecoder;
import net.digitalid.core.conversion.decoders.NetworkDecoder;
import net.digitalid.core.conversion.encoders.FileEncoder;
import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.encoders.NetworkEncoder;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.parameters.Parameters;

/**
 * This utility class helps converting and recovering objects to and from XDF.
 */
@Utility
public abstract class XDF {
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    /**
     * Returns the given object converted with the given converter as a byte array.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Capturable @Nonnull byte[] convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object) {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
            encoder.encodeObject(converter, object);
        } catch (@Nonnull MemoryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
        return outputStream.toByteArray();
    }
    
    /**
     * Converts the given object with the given converter to the given file.
     */
    @Pure
    public static <@Unspecifiable TYPE> void convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object, @Nonnull File file) throws FileException {
        try (@Nonnull FileEncoder encoder = FileEncoder.of(file)) {
            encoder.encodeObject(converter, object);
        }
    }
    
    /**
     * Converts the given object with the given converter to the given socket.
     */
    @Pure
    public static <@Unspecifiable TYPE> void convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object, @Nonnull Socket socket) throws NetworkException {
        NetworkEncoder.of(socket).encodeObject(converter, object);
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Recovers and returns an object with the given converter and provided object from the given input stream.
     */
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @NonCaptured @Modified @Nonnull InputStream inputStream) throws RecoveryException, MemoryException {
        try (@Nonnull MemoryDecoder decoder = MemoryDecoder.of(inputStream)) {
            return decoder.decodeObject(converter, provided);
        }
    }
    
    /**
     * Recovers and returns an object with the given converter and provided object from the given byte array.
     */
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @NonCaptured @Unmodified @Nonnull byte[] bytes) throws RecoveryException {
        try {
            return recover(converter, provided, new ByteArrayInputStream(bytes));
        } catch (@Nonnull MemoryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /**
     * Recovers and returns an object with the given converter and provided object from the given file.
     */
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull @Existent File file) throws RecoveryException, FileException {
        try (@Nonnull FileDecoder decoder = FileDecoder.of(file)) {
            return decoder.decodeObject(converter, provided);
        }
    }
    
    /**
     * Recovers and returns an object with the given converter and provided object from the given socket.
     */
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull Socket socket) throws RecoveryException, NetworkException {
        return NetworkDecoder.of(socket).decodeObject(converter, provided);
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Stores an output stream that ignores all input.
     */
    public static final @Nonnull OutputStream NULL_OUTPUT_STREAM = new OutputStream() { @Override public void write(int b) {} };
    
    /**
     * Returns the hash of the given object converted with the given converter.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Nonnull @Size(32) byte[] hash(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        final @Nonnull DigestOutputStream outputStream = new DigestOutputStream(NULL_OUTPUT_STREAM, messageDigest);
        try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
            encoder.encodeObject(converter, object);
        } catch (@Nonnull MemoryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
        return messageDigest.digest();
    }
    
}
