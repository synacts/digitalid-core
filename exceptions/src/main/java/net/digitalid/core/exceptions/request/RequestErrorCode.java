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

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

/**
 * This class enumerates the various request error codes.
 * 
 * @see RequestException
 */
@Immutable
@GenerateConverter
public enum RequestErrorCode {
    
    /* -------------------------------------------------- Error Codes -------------------------------------------------- */
    
    /**
     * The error code for an internal problem.
     */
    INTERNAL(0),
    
    /**
     * The error code for an external problem.
     */
    EXTERNAL(1),
    
    /**
     * The error code for a request problem.
     */
    REQUEST(2),
    
    /**
     * The error code for an invalid packet.
     */
    PACKET(3),
    
    /**
     * The error code for a replayed packet.
     */
    REPLAY(4),
    
    /**
     * The error code for an invalid encryption.
     */
    ENCRYPTION(5),
    
    /**
     * The error code for invalid elements.
     */
    ELEMENTS(6),
    
    /**
     * The error code for an invalid audit.
     */
    AUDIT(7),
    
    /**
     * The error code for an invalid signature.
     */
    SIGNATURE(8),
    
    /**
     * The error code for a required key rotation.
     */
    KEYROTATION(9),
    
    /**
     * The error code for an invalid compression.
     */
    COMPRESSION(10),
    
    /**
     * The error code for an invalid content.
     */
    CONTENT(11),
    
    /**
     * The error code for an invalid method type.
     */
    METHOD(12),
    
    /**
     * The error code for a wrong recipient.
     */
    RECIPIENT(13),
    
    /**
     * The error code for a non-existent identity.
     */
    IDENTITY(14),
    
    /**
     * The error code for a relocated identity.
     */
    RELOCATION(15),
    
    /**
     * The error code for a relocated service provider.
     */
    SERVICE(16),
    
    /**
     * The error code for an insufficient authorization.
     */
    AUTHORIZATION(17);
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns whether the given value is a valid request error code.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 19;
    }
    
    private final @Valid byte value;
    
    /**
     * Returns the value of this request error code.
     */
    @Pure
    public @Valid byte getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private RequestErrorCode(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the request error code denoted by the given value.
     */
    @Pure
    @Recover
    public static @Nonnull RequestErrorCode of(@Valid byte value) {
        Require.that(isValid(value)).orThrow("The value has to be a valid request error code but was $.", value);
        
        for (@Nonnull RequestErrorCode code : values()) {
            if (code.value == value) { return code; }
        }
        
        throw CaseExceptionBuilder.withVariable("value").withValue(value).build();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Strings.capitalizeFirstLetters(Strings.desnake(name()));
    }
    
}
