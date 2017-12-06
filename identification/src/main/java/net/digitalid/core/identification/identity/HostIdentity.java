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
package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This interface models a host identity.
 */
@Immutable
@GenerateSubclass
public interface HostIdentity extends InternalIdentity {
    
    /* -------------------------------------------------- Digital ID Core Identity -------------------------------------------------- */
    
    /**
     * Maps the identity of the Digital ID core host.
     */
    @PureWithSideEffects
    public static @Nonnull HostIdentity mapDigitalIDCoreHostIdentity() {
        try {
            final @Nonnull IdentifierResolver identifierResolver = IdentifierResolver.configuration.get();
            final @Nullable Identity identity = identifierResolver.load(HostIdentifier.DIGITALID);
            if (identity != null) {
                if (identity instanceof HostIdentity) { return (HostIdentity) identity; }
                else { throw RecoveryExceptionBuilder.withMessage(Strings.format("The host identifier $ is not mapped to a host identity!", HostIdentifier.DIGITALID)).build(); }
            } else {
                return (HostIdentity) identifierResolver.map(Category.HOST, HostIdentifier.DIGITALID);
            }
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /**
     * Stores the host identity of {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentity DIGITALID = mapDigitalIDCoreHostIdentity();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull HostIdentifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
}
