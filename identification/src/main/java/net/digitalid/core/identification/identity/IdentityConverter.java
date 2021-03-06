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

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.ownership.Shared;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.recovery.Check;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;

import static net.digitalid.utility.conversion.model.CustomType.INTEGER64;
import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * This class converts and recovers an identity to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class IdentityConverter<@Unspecifiable IDENTITY extends Identity> implements Converter<IDENTITY, Void> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull IdentityConverter<Identity> INSTANCE = new IdentityConverterSubclass<>(Identity.class);
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull Class<IDENTITY> getType();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return getType().getSimpleName();
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.identification.identity";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull @NonNullableElements ImmutableList<CustomField> internalFields = ImmutableList.withElements(CustomField.with(INTEGER64, "key"));
    
    private static final @Nonnull @NonNullableElements ImmutableList<CustomField> externalFields = ImmutableList.withElements(CustomField.with(TUPLE.of(IdentifierConverter.INSTANCE), "address", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))));
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        if (representation == Representation.INTERNAL) {
            return internalFields;
        } else if (representation == Representation.EXTERNAL) {
            return externalFields;
        } else {
            throw CaseExceptionBuilder.withVariable("representation").withValue(representation).build();
        }
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull IDENTITY identity, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        final @Nonnull Representation representation = encoder.getRepresentation();
        if (representation == Representation.INTERNAL) {
            encoder.encodeInteger64(identity.getKey());
        } else if (representation == Representation.EXTERNAL) {
            encoder.encodeObject(IdentifierConverter.INSTANCE, identity.getAddress());
        } else {
            throw CaseExceptionBuilder.withVariable("representation").withValue(representation).build();
        }
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull IDENTITY recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Shared Void provided) throws EXCEPTION, RecoveryException {
        final @Nonnull Identity identity;
        final @Nonnull Representation representation = decoder.getRepresentation();
        if (representation == Representation.INTERNAL) {
            final long key = decoder.decodeInteger64();
            try {
                identity = IdentifierResolver.configuration.get().load(key);
            } catch (@Nonnull DatabaseException exception) {
                throw RecoveryExceptionBuilder.withMessage("A problem occurred while resolving the identity with the key " + key + ".").withCause(exception).build();
            }
        } else if (representation == Representation.EXTERNAL) {
            final @Nonnull Identifier address = decoder.decodeObject(IdentifierConverter.INSTANCE, null);
            try {
                identity = IdentifierResolver.configuration.get().resolve(address);
            } catch (@Nonnull ExternalException exception) {
                throw RecoveryExceptionBuilder.withMessage("A problem occurred while resolving the identity with the address '" + address.getString() + "'.").withCause(exception).build();
            }
        } else {
            throw CaseExceptionBuilder.withVariable("representation").withValue(representation).build();
        }
        Check.that(getType().isInstance(identity)).orThrow("The recovered identity $ has to be an instance of $.", identity, getType());
        return getType().cast(identity);
    }
    
}
