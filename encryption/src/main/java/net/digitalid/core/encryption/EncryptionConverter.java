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
package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.interfaces.GenericTypeConverter;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifierConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * This class converts and recovers an {@link Encryption encryption}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class EncryptionConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, Encryption<OBJECT>, @Nullable SymmetricKey> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super Encryption<OBJECT>> getType() {
        return Encryption.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "Encryption";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.encryption";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull @NonNullableElements ImmutableList<CustomField> fields;
    
    static {
        fields = ImmutableList.withElements(
                CustomField.with(TUPLE.of(HostIdentifierConverter.INSTANCE), "recipient", ImmutableList.withElements(CustomAnnotation.with(Nullable.class)))
        );
    }
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements @NonEmpty ImmutableList<@Nonnull Converter<? extends Encryption<OBJECT>, @Nullable SymmetricKey>> getSubtypeConverters() {
        return ImmutableList.withElements(RequestEncryptionConverterBuilder.withObjectConverter(getObjectConverter()).build(), ResponseEncryptionConverterBuilder.withObjectConverter(getObjectConverter()).build());
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull Encryption<OBJECT> encryption, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeNullableObject(HostIdentifierConverter.INSTANCE, encryption.getRecipient());
        encoder.encodeObject(getObjectConverter(), encryption.getObject());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull Encryption<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nullable SymmetricKey symmetricKey) throws EXCEPTION, RecoveryException {
        final @Nullable HostIdentifier recipient = decoder.decodeNullableObject(HostIdentifierConverter.INSTANCE, null);
        final OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        return EncryptionBuilder.withObject(object).withRecipient(recipient).build();
    }
    
}
