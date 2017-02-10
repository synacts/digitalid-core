package net.digitalid.core.signature.host;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
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
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeConverter;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.SignatureConverterBuilder;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;

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
                CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.INTEGER, "value", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
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
        final @Nonnull PrivateKey privateKey;
        try {
            privateKey = PrivateKeyRetriever.retrieve(hostSignature.getSigner().getHostIdentifier(), hostSignature.getTime());
        } catch (@Nonnull RequestException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
        
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        encoder.startHashing(messageDigest);
        encoder.encodeObject(TimeConverter.INSTANCE, hostSignature.getTime());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, hostSignature.getSubject());
        encoder.encodeObject(InternalIdentifierConverter.INSTANCE, hostSignature.getSigner());
        encoder.encodeObject(getObjectConverter(), hostSignature.getObject());
        final @Nonnull BigInteger hash = new BigInteger(1, encoder.stopHashing());
        final @Nonnull BigInteger value = privateKey.powD(hash).getValue();
        encoder.encodeInteger(value);
        
        Log.debugging("$ signed the hash $ about $.", hostSignature.getSigner(), hash, hostSignature.getSubject());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "The signature should not be verified in the converter (because of endless recursion and repeated verification).", date = "2017-01-29", author = Author.KASPAR_ETTER)
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull HostSignature<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION, RecoveryException {
        decoder.startHashing(Parameters.HASH_FUNCTION.get().produce());
        final @Nonnull Time time = decoder.decodeObject(TimeConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier subject = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull InternalIdentifier signer = decoder.decodeObject(InternalIdentifierConverter.INSTANCE, null);
        final @Nonnull OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        final @Nonnull BigInteger hash = new BigInteger(1, decoder.stopHashing());
        final @Nonnull BigInteger value = decoder.decodeInteger();
        
        Log.debugging("Verified the hash $ by $ about $.", hash, signer, subject);
        
        final @Nonnull PublicKey publicKey;
        try {
            publicKey = PublicKeyRetriever.retrieve(signer.getHostIdentifier(), time);
        } catch (@Nonnull ExternalException exception) {
            throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the public key of $.", signer.getHostIdentifier())).withCause(exception).build();
        }
        
        final @Nonnull HostSignature<OBJECT> hostSignature = HostSignatureBuilder.withObject(object).withSubject(subject).withSigner(signer).withTime(time).build();
        try {
            hostSignature.verifySignature(publicKey, value, hash);
        } catch (@Nonnull InvalidSignatureException exception) {
            throw RecoveryExceptionBuilder.withMessage("The host signature is invalid.").withCause(exception).build();
        }
        
        return hostSignature;
    }
    
}
