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
package net.digitalid.core.compression;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Mutable;

@Mutable
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class LoggingByteArrayInputStream extends ByteArrayInputStream {
    
    private final boolean logging;
    
    public LoggingByteArrayInputStream(byte[] buffer, boolean logging) {
        super(buffer);
        
        this.logging = logging;
    }
    
    @Impure
    @Override
    public synchronized int read() {
        final int read = super.read();
        
        final @Nonnull byte[] bytes = new byte[1];
        bytes[0] = (byte) read;
        if (logging) { System.out.println("Read single " + Strings.hex(bytes)); }
        
        return read;
    }
    
    @Impure
    @Override
    public synchronized int read(byte b[], int off, int len) {
        final int length = super.read(b, off, len);
        
        if (length > 0) { 
            final @Nonnull byte[] bytes = new byte[length];
            System.arraycopy(b, off, bytes, 0, length);
            if (logging) { System.out.println("Read array  " + Strings.hexWithSpaces(bytes)); }
        } else if (length == 0) {
            if (logging) { System.out.println("Read nothing"); }
        } else if (length == -1) {
            if (logging) { System.out.println("End of input"); }
        }
        
        return length;
    }
    
    @Pure
    public int getPosition() {
        return pos;
    }
    
}
