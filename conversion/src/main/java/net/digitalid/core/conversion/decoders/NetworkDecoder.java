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
import java.net.Socket;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.conversion.exceptions.NetworkExceptionBuilder;

/**
 * A network decoder decodes values as XDF from a socket.
 */
@Mutable
@GenerateSubclass
public abstract class NetworkDecoder extends XDFDecoder<NetworkException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull NetworkException createException(@Nonnull IOException exception) {
        return NetworkExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected NetworkDecoder(@Nonnull InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Returns a decoder for the given socket.
     */
    @Pure
    public static @Nonnull NetworkDecoder of(@Nonnull Socket socket) throws NetworkException {
        try {
            return new NetworkDecoderSubclass(socket.getInputStream());
        } catch (@Nonnull IOException exception) {
            throw NetworkExceptionBuilder.withCause(exception).build();
        }
    }
    
}
