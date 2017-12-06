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
package net.digitalid.core.conversion.streams.input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Mutable;

@Mutable
@Deprecated
@TODO(task = "This class is currently only used in tests and can be removed in the future.", date = "2017-02-10", author = Author.KASPAR_ETTER)
public class BufferedInflaterInputStream extends InflaterInputStream {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater, int size) {
        super(new BufferedInputStream(inputStream), inflater, size);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater) {
        this(inputStream, inflater, 512);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, new Inflater());
    }
    
    /* -------------------------------------------------- Reading -------------------------------------------------- */
    
    @Impure
    @Override
    @TODO(task = "Why is the offset subtracted from the length on the first line? And what about input streams that don't support resetting?", date = "2017-02-10", author = Author.KASPAR_ETTER)
    public int read(byte[] bytes, int offset, int length) throws IOException {
        super.in.mark(length - offset);
        int inflatedBytes = super.read(bytes, offset, length);
        super.in.reset();
        long compressedBytesRead = super.inf.getBytesRead();
        super.in.skip(compressedBytesRead);
        return inflatedBytes;
    }
    
    @Pure
    @TODO(task = "I think this method should be renamed to a getter.", date = "2017-02-10", author = Author.KASPAR_ETTER)
    public @Nonnull InputStream finish() {
        return super.in;
    }
    
}
