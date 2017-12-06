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

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This class wraps an output stream and does not close the stream with the normal method call.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class NonClosingOutputStream extends OutputStream {
    
    @Pure
    protected abstract @Nonnull OutputStream getOutputStream();
    
    @Impure
    @Override
    public void write(int i) throws IOException {
        getOutputStream().write(i);
    }
    
    @Pure
    @Override
    public void close() throws IOException {
        // intentionally left blank
    }
    
    @Impure
    public void actualClose() throws IOException {
        getOutputStream().close();
    }
    
}
