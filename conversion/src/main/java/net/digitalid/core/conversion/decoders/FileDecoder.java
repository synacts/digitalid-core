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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.conversion.exceptions.FileExceptionBuilder;

/**
 * A file decoder decodes values as XDF from a file.
 */
@Mutable
@GenerateSubclass
public abstract class FileDecoder extends XDFDecoder<FileException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull FileException createException(@Nonnull IOException exception) {
        return FileExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected FileDecoder(@Nonnull InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Returns a decoder for the given file.
     */
    @Pure
    public static @Nonnull FileDecoder of(@Nonnull File file) throws FileException {
        try {
            return new FileDecoderSubclass(new FileInputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw FileExceptionBuilder.withCause(exception).build();
        }
    }
    
}
