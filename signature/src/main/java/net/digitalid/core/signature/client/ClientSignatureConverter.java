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
package net.digitalid.core.signature.client;

import java.math.BigInteger;

import javax.annotation.Nonnull;

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

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.commitment.CommitmentConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentConverter;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.signature.SignatureConverterBuilder;

/**
 * This class converts and recovers a {@link ClientSignature client signature}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ClientSignatureConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, ClientSignature<OBJECT>, Void> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super ClientSignature<OBJECT>> getType() {
        return ClientSignature.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "ClientSignature";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.signature.client";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(PrimaryKey.class))),
                CustomField.with(CustomType.TUPLE.of(InternalIdentifierConverter.INSTANCE), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(CommitmentConverter.INSTANCE), "commitment", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.INTEGER, "t"),
                CustomField.with(CustomType.TUPLE.of(ExponentConverter.INSTANCE), "s", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
        );
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Converter<? super ClientSignature<OBJECT>, Void> getSupertypeConverter() {
        return SignatureConverterBuilder.withObjectConverter(getObjectConverter()).build();
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull ClientSignature<OBJECT> clientSignature, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(TimeConverter.INSTANCE, clientSignature.getTime());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, clientSignature.getSubject());
        encoder.encodeObject(getObjectConverter(), clientSignature.getObject());
        encoder.encodeObject(CommitmentConverter.INSTANCE, clientSignature.getCommitment());
        encoder.encodeInteger(clientSignature.getT());
        encoder.encodeObject(ExponentConverter.INSTANCE, clientSignature.getS());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull ClientSignature<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION, RecoveryException {
        final @Nonnull Time time = decoder.decodeObject(TimeConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier subject = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        
        final @Nonnull Commitment commitment = decoder.decodeObject(CommitmentConverter.INSTANCE, null);
        final @Nonnull BigInteger t = decoder.decodeInteger();
        final @Nonnull Exponent s = decoder.decodeObject(ExponentConverter.INSTANCE, null);
        
        final @Nonnull ClientSignature<OBJECT> clientSignature = ClientSignatureBuilder.withObjectConverter(getObjectConverter()).withObject(object).withSubject(subject).withCommitment(commitment).withT(t).withS(s).withTime(time).build();
        return clientSignature;
    }
    
}
