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
package net.digitalid.core.signature.host;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converters.IntegerConverter;
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
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.signature.SignatureConverterBuilder;

/**
 * This class converts and recovers a {@link HostSignature host signature}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class HostSignatureConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, HostSignature<OBJECT>, Void> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super HostSignature<OBJECT>> getType() {
        return HostSignature.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "HostSignature";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.signature.host";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(PrimaryKey.class))),
                CustomField.with(CustomType.TUPLE.of(InternalIdentifierConverter.INSTANCE), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(InternalIdentifierConverter.INSTANCE), "signer", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(IntegerConverter.INSTANCE), "signatureValue", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
        );
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Converter<? super HostSignature<OBJECT>, Void> getSupertypeConverter() {
        return SignatureConverterBuilder.withObjectConverter(getObjectConverter()).build();
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull HostSignature<OBJECT> hostSignature, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(TimeConverter.INSTANCE, hostSignature.getTime());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, hostSignature.getSubject());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, hostSignature.getSigner());
        encoder.encodeObject(getObjectConverter(), hostSignature.getObject());
        encoder.encodeInteger(hostSignature.getSignatureValue());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull HostSignature<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION, RecoveryException {
        final @Nonnull Time time = decoder.decodeObject(TimeConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier subject = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier signer = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        final @Nonnull BigInteger signatureValue = decoder.decodeInteger();
        
        final @Nonnull HostSignature<OBJECT> hostSignature = HostSignatureBuilder.withObjectConverter(getObjectConverter()).withObject(object).withSubject(subject).withSigner(signer).withSignatureValue(signatureValue).withTime(time).build();
        return hostSignature;
    }
    
}
