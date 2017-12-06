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
package net.digitalid.core.conversion.streams.output;

import java.io.DataOutputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This data output stream wraps another output stream.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public class WrappedOutputStream extends DataOutputStream {
    
    /* -------------------------------------------------- Wrapped Stream -------------------------------------------------- */
    
    private final @Nonnull OutputStream wrappedStream;
    
    /**
     * Returns whether this stream wraps an instance of the given type.
     */
    @Pure
    public boolean wrapsInstanceOf(@Nonnull Class<? extends OutputStream> type) {
        return type.isInstance(wrappedStream);
    }
    
    /**
     * Returns the wrapped stream.
     * 
     * @require wrapsInstanceOf(type) : "This stream wraps an instance of the given type.";
     */
    @Pure
    @SuppressWarnings("null")
    public <@Unspecifiable STREAM extends OutputStream> @Nonnull STREAM getWrappedStream(@Nonnull Class<STREAM> type) {
        Require.that(wrapsInstanceOf(type)).orThrow("The wrapped stream $ has to be of the type $.", wrappedStream, type);
        
        return type.cast(wrappedStream);
    }
    
    /* -------------------------------------------------- Previous Stream -------------------------------------------------- */
    
    private final @Nullable WrappedOutputStream previousStream;
    
    /**
     * Returns whether this stream contains a previous stream.
     */
    @Pure
    public boolean hasPreviousStream() {
        return previousStream != null;
    }
    
    /**
     * Returns the previous stream.
     * 
     * @require hasPreviousStream() : "This stream contains a previous stream.";
     * @require wrapsInstanceOf(type) : "This stream wraps an instance of the given type.";
     */
    @Pure
    @SuppressWarnings("null")
    public @Nonnull WrappedOutputStream getPreviousStream(@Nonnull Class<? extends OutputStream> type) {
        Require.that(hasPreviousStream()).orThrow("The previous stream may not be null.");
        Require.that(wrapsInstanceOf(type)).orThrow("The wrapped stream $ has to be of the type $.", wrappedStream, type);
        
        return previousStream;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    WrappedOutputStream(@Nonnull OutputStream wrappedStream, @Nullable WrappedOutputStream previousStream) {
        super(wrappedStream);
        
        this.wrappedStream = wrappedStream;
        this.previousStream = previousStream;
    }
    
}
