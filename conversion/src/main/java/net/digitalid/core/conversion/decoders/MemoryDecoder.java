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
package net.digitalid.core.conversion.decoders;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.conversion.exceptions.MemoryExceptionBuilder;

/**
 * A memory decoder decodes values as XDF in memory.
 */
@Mutable
@GenerateSubclass
public abstract class MemoryDecoder extends XDFDecoder<MemoryException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull MemoryException createException(@Nonnull IOException exception) {
        return MemoryExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected MemoryDecoder(@Nonnull InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Returns a decoder for the given input stream.
     */
    @Pure
    public static @Nonnull MemoryDecoder of(@Nonnull InputStream inputStream)  {
        return new MemoryDecoderSubclass(inputStream);
    }
    
}
