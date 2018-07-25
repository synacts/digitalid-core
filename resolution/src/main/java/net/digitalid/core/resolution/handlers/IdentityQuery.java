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
package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Queries the identity of the given subject.
 * 
 * @see IdentityReply
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class IdentityQuery extends ExternalQuery<Entity> implements CoreMethod<Entity> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type of this query.
     */
    public static final @Nonnull @Loaded SemanticType TYPE = SemanticType.map(IdentityQueryConverter.INSTANCE);
    
    /* -------------------------------------------------- Request Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAuthentications getAuthentications() {
        return FreezableAuthentications.NONE;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nonnull @Matching IdentityReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        final @Nullable Identity identity = IdentifierResolver.configuration.get().load(getSubject());
        if (identity == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("There exists no identity with the identifier '" + getSubject() + "'.").build(); }
        return IdentityReplyBuilder.withEntity(getEntity()).withCategory(identity.getCategory()).build();
    }
    
}
