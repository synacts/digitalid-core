package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.collectors.CollectionCollector;
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
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
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

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentConverter;
import net.digitalid.core.group.Group;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsConverter;
import net.digitalid.core.signature.SignatureConverterBuilder;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.signature.attribute.CertifiedAttributeValueConverter;

/**
 *
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class CredentialsSignatureConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, CredentialsSignature<OBJECT>, Void> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super CredentialsSignature<OBJECT>> getType() {
        return CredentialsSignature.class;
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
                CustomField.with(CustomType.TUPLE.of(ExponentConverter.INSTANCE), "t", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(ExponentConverter.INSTANCE), "su", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.LIST.of(CustomType.TUPLE.of(PublicClientCredentialConverter.INSTANCE)), "credentials", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.LIST.of(CustomType.TUPLE.of(CertifiedAttributeValueConverter.INSTANCE)), "certificates", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.LIST.of(CustomType.TUPLE.of(RestrictionsConverter.INSTANCE)), "restrictions", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(ExponentConverter.INSTANCE), "sv", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(ElementConverter.INSTANCE), "f_prime", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(ExponentConverter.INSTANCE), "sb_prime", ImmutableList.withElements(CustomAnnotation.with(Nullable.class)))
        );
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Converter<? super CredentialsSignature<OBJECT>, Void> getSupertypeConverter() {
        return SignatureConverterBuilder.withObjectConverter(getObjectConverter()).build();
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull CredentialsSignature<OBJECT> clientSignature, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(TimeConverter.INSTANCE, clientSignature.getTime());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, clientSignature.getSubject());
        encoder.encodeObject(getObjectConverter(), clientSignature.getObject());
        encoder.encodeObject(ExponentConverter.INSTANCE, clientSignature.getT());
        encoder.encodeObject(ExponentConverter.INSTANCE, clientSignature.getSU());
        encoder.encodeOrderedIterable(PublicClientCredentialConverter.INSTANCE, clientSignature.getCredentials());
        encoder.encodeOrderedIterable(CertifiedAttributeValueConverter.INSTANCE, clientSignature.getCertificates());
        encoder.encodeNullableObject(RestrictionsConverter.INSTANCE, clientSignature.getRestrictions());
        encoder.encodeNullableObject(ExponentConverter.INSTANCE, clientSignature.getSV());
        encoder.encodeNullableObject(ElementConverter.INSTANCE, clientSignature.getFPrime());
        encoder.encodeNullableObject(ExponentConverter.INSTANCE, clientSignature.getSBPrime());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull CredentialsSignature<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nullable Void none) throws EXCEPTION, RecoveryException {
        final @Nonnull Time time = decoder.decodeObject(TimeConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier subject = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull OBJECT object = decoder.decodeObject(getObjectConverter(), null);
    
        final @Nonnull Exponent t = decoder.decodeObject(ExponentConverter.INSTANCE, null);
        final @Nonnull Exponent su = decoder.decodeObject(ExponentConverter.INSTANCE, null);
        final @Nonnull ReadOnlyList<@Nonnull PublicClientCredential> credentials = decoder.decodeOrderedIterable(PublicClientCredentialConverter.INSTANCE, null, size -> CollectionCollector.with(FreezableArrayList.withInitialCapacity(size)));
        final @Nonnull ReadOnlyList<@Nonnull CertifiedAttributeValue> certificates = decoder.decodeOrderedIterable(CertifiedAttributeValueConverter.INSTANCE, null, size -> CollectionCollector.with(FreezableArrayList.withInitialCapacity(size)));
        final @Nullable Restrictions restrictions = decoder.decodeNullableObject(RestrictionsConverter.INSTANCE, null);
        final @Nullable Exponent sv = decoder.decodeNullableObject(ExponentConverter.INSTANCE, null);
        final @Nonnull PublicKey publicKey;
            try {
                publicKey = PublicKeyRetriever.retrieve(subject.getHostIdentifier(), time);
            } catch (@Nonnull ExternalException exception) {
                // TODO: RecoverException
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
        final @Nullable Element fPrime = decoder.decodeNullableObject(ElementConverter.INSTANCE, publicKey.getCompositeGroup());
        final @Nullable Exponent sbPrime = decoder.decodeNullableObject(ExponentConverter.INSTANCE, null);
        
        final @Nonnull CredentialsSignature<OBJECT> clientSignature = CredentialsSignatureBuilder.withObjectConverter(getObjectConverter()).withObject(object).withSubject(subject).withT(t).withSU(su).withCredentials(credentials).withCertificates(certificates).withSV(sv).withFPrime(fPrime).withSBPrime(sbPrime).withTime(time).build();
        return clientSignature;
    }
    
}
