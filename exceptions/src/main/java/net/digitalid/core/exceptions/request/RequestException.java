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
package net.digitalid.core.exceptions.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Normalize;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A request exception indicates an error in the encoding or content of a request.
 * 
 * @see RequestErrorCode
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class RequestException extends ExternalException {
    
    /* -------------------------------------------------- Code -------------------------------------------------- */
    
    /**
     * Returns the error code of this request exception.
     */
    @Pure
    public abstract @Nonnull RequestErrorCode getCode();
    
    /* -------------------------------------------------- Message -------------------------------------------------- */
    
    @Pure
    @Override
    @Normalize("\"(\" + code + \") \" + message")
    public abstract @Nonnull String getMessage();
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public abstract @Nullable Throwable getCause();
    
    /* -------------------------------------------------- Decoded -------------------------------------------------- */
    
    /**
     * Returns whether this exception was decoded from a block.
     */
    @Pure
    @NonRepresentative
    public abstract @Default("true") boolean isDecoded();
    
}
