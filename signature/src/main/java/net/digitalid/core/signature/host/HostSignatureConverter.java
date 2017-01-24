package net.digitalid.core.signature.host;

import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.FailedValueRecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.MissingSupportException;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeConverter;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.signature.exceptions.InvalidHostSignatureException;

/**
 *
 */
public class HostSignatureConverter<@Unspecifiable TYPE> implements Converter<HostSignature<TYPE>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<TYPE, ?> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private HostSignatureConverter(@Nonnull Converter<TYPE, ?> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <T> @Nonnull HostSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new HostSignatureConverter<>(objectConverter);
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super HostSignature<TYPE>> getType() {
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
        return "net.digitalid.core.encryption";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return null;
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified HostSignature<TYPE> signature, @Nonnull @NonCaptured @Modified Encoder<X> encoder) throws ExternalException {
        // TODO: how do we handle multiple hosts? In the privateKeyRetriever itself?
        @Nullable PrivateKey privateKey = null;
        if (signature != null && signature.getTime() != null) {
            privateKey = PrivateKeyRetriever.retrieve(signature.getSigner().getHostIdentifier(), signature.getTime());
//            System.out.println("private key: " + privateKey);
        }
        
        try {
            encoder.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        int i = 1;
        i *= InternalIdentifierConverter.INSTANCE.convert(signature == null ? null : signature.getSubject(), encoder);
//        System.out.println("object: " + signature.getSubject());
        i *= TimeConverter.INSTANCE.convert(signature == null ? null : signature.getTime(), encoder);
//        System.out.println("time: " + signature.getTime());
        i *= objectConverter.convert(signature == null ? null : signature.getObject(), encoder);
//        System.out.println("element: " + signature.getElement());
        i *= InternalIdentifierConverter.INSTANCE.convert(signature == null ? null : signature.getSigner(), encoder);
//        System.out.println("signer: " + signature.getSigner());
        
        if (privateKey != null) {
            final @Nullable DigestOutputStream digestOutputStream = encoder.popSignatureDigest();
            final @Nonnull BigInteger hash = new BigInteger(1, digestOutputStream.getMessageDigest().digest());
//            System.out.println("hash: " + hash);
            final @Nonnull BigInteger signatureValue = privateKey.powD(hash).getValue();
//            System.out.println("signature value: " + signatureValue);
            encoder.encodeInteger(signatureValue);
        } else {
            encoder.encodeInteger(null);
        }
        
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> @Nonnull HostSignature<TYPE> recover(@Nonnull @NonCaptured @Modified Decoder<X> decoder, Void externallyProvided) throws ExternalException {
        try {
            decoder.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        final @Nullable InternalIdentifier subject = InternalIdentifierConverter.INSTANCE.recover(decoder, null);
//        System.out.println("subject: " + subject);
        final @Nullable Time time = TimeConverter.INSTANCE.recover(decoder, null);
//        System.out.println("time: " + time);
        final @Nullable TYPE object = objectConverter.recover(decoder, null);
//        System.out.println("element: " + object);
        final @Nullable InternalIdentifier signer = InternalIdentifierConverter.INSTANCE.recover(decoder, null);
//        System.out.println("signer: " + signer);
        final @Nonnull DigestInputStream digestInputStream = decoder.popSignatureDigest();
        
        final @Nullable BigInteger signatureValue = decoder.getInteger();
//        System.out.println("signature value: " + signatureValue);
        
        @Nullable PublicKey publicKey = null;
        if (signatureValue != null && signer != null && time != null) {
            publicKey = PublicKeyRetriever.retrieve(signer.getHostIdentifier(), time);
//            System.out.println("public key: " + publicKey);
        }
        final @Nonnull BigInteger hash = new BigInteger(1, digestInputStream.getMessageDigest().digest());
//        System.out.println("hash: " + hash);
    
        final @Nonnull HostSignature<TYPE> hostSignature = HostSignatureBuilder.withObject(object).withSubject(subject).withSigner(signer).withTime(time).build();
        if (publicKey != null) {
            try {
                hostSignature.verifySignature(publicKey, signatureValue, hash);
            } catch (InvalidHostSignatureException e) {
                throw FailedValueRecoveryException.of(e);
            }
        }
        return hostSignature;
    }
    
}
