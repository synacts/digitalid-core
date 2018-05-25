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
package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.functional.failable.FailableBinaryFunction;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.handler.method.query.InternalQuery;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.signature.Signature;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 */
@Immutable
public interface InternalMethod extends Method<NonHostEntity> {
    
    /* -------------------------------------------------- Request Signature -------------------------------------------------- */
    
    /**
     * The configuration object that holds the method for signing internal method requests.
     * See {@code net.digitalid.core.client.method.InternalMethodSignatureInjector}.
     */
    public static final @Nonnull Configuration<FailableBinaryFunction<@Nonnull InternalMethod, @Nonnull Compression<Pack>, @Nonnull Signature<Compression<Pack>>, @Nonnull ExternalException>> configuration = Configuration.withUnknownProvider();
    
    @Pure
    @Override
    public default @Nonnull Signature<Compression<Pack>> getSignature(@Nonnull Compression<Pack> compression) throws ExternalException {
        return configuration.get().evaluate(this, compression);
    }
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    @NonRepresentative
    public @Nonnull Entity getProvidedEntity();
    
    @Pure
    @Override
    @Derive("castEntity(providedEntity)")
    public @Nonnull NonHostEntity getEntity();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nullable InternalIdentifier getProvidedSubject() {
        return null;
    }
    
    @Pure
    @Override
    @OrderOfAssignment(1)
    @Derive("getEntity().getIdentity().getAddress()")
    public @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * Returns the restrictions required for this internal method.
     */
    @Pure
    public default @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return Restrictions.MIN;
    }
    
}
