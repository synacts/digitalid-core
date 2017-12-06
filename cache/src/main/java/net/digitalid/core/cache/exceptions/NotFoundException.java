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
package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.exceptions.response.ResponseException;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This exception is thrown when something could not be found.
 * 
 * @see AttributeNotFoundException
 * @see CertificateNotFoundException
 */
@Immutable
public abstract class NotFoundException extends ResponseException {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Returns the type of the something that could not be found.
     */
    @Pure
    public abstract @Nonnull @AttributeType SemanticType getType();
    
    /* -------------------------------------------------- Message -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getMessage() {
        return getType().getAddress() + " of " + getIdentity().getAddress() + " could not be found.";
    }
    
}
