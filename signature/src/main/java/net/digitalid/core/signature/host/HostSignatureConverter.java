package net.digitalid.core.signature.host;

import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.conversion.exceptions.FailedValueRecoveryException;
import net.digitalid.utility.exceptions.MissingSupportException;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.TimeConverter;

import net.digitalid.database.auxiliary.Time;

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
public class HostSignatureConverter<T> implements Converter<HostSignature<T>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private HostSignatureConverter(@Nonnull Converter<T, ?> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <T> @Nonnull HostSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new HostSignatureConverter<>(objectConverter);
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "hostsignature";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields() {
        return null;
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified HostSignature<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws ExternalException {
        // TODO: how do we handle multiple hosts? In the privateKeyRetriever itself?
        @Nullable PrivateKey privateKey = null;
        if (object != null && object.getTime() != null) {
            privateKey = PrivateKeyRetriever.retrieve(object.getTime());
        }
        
        try {
            valueCollector.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        int i = 1;
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSubject(), valueCollector);
//        System.out.println("object: " + object.getSubject());
        i *= TimeConverter.INSTANCE.convert(object == null ? null : object.getTime(), valueCollector);
//        System.out.println("time: " + object.getTime());
        i *= objectConverter.convert(object == null ? null : object.getElement(), valueCollector);
//        System.out.println("element: " + object.getElement());
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSigner(), valueCollector);
//        System.out.println("signer: " + object.getSigner());
        
        if (privateKey != null) {
            final @Nullable DigestOutputStream digestOutputStream = valueCollector.popSignatureDigest();
            final @Nonnull BigInteger hash = new BigInteger(1, digestOutputStream.getMessageDigest().digest());
//            System.out.println("hash: " + hash);
            final @Nonnull BigInteger signatureValue = privateKey.powD(hash).getValue();
//            System.out.println("signature value: " + signatureValue);
            valueCollector.setNullableInteger(signatureValue);
        } else {
            valueCollector.setNullableInteger(null);
        }
        
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> @Nonnull HostSignature<T> recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, Void externallyProvided) throws ExternalException {
        try {
            selectionResult.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        final @Nullable InternalIdentifier subject = InternalIdentifierConverter.INSTANCE.recover(selectionResult, null);
//        System.out.println("subject: " + subject);
        final @Nullable Time time = TimeConverter.INSTANCE.recover(selectionResult, null);
//        System.out.println("time: " + time);
        final @Nullable T object = objectConverter.recover(selectionResult, null);
//        System.out.println("element: " + object);
        final @Nullable InternalIdentifier signer = InternalIdentifierConverter.INSTANCE.recover(selectionResult, null);
//        System.out.println("signer: " + signer);
        final @Nonnull DigestInputStream digestInputStream = selectionResult.popSignatureDigest();
        
        final @Nullable BigInteger signatureValue = selectionResult.getInteger();
//        System.out.println("signature value: " + signatureValue);
        
        @Nullable PublicKey publicKey = null;
        if (signatureValue != null && signer != null && time != null) {
            publicKey = PublicKeyRetriever.retrieve(signer, time);
        }
        final @Nonnull BigInteger hash = new BigInteger(1, digestInputStream.getMessageDigest().digest());
//        System.out.println("hash: " + hash);
    
        final @Nonnull HostSignature<T> hostSignature = HostSignatureBuilder.withElement(object).withSigner(signer).withSubject(subject).withTime(time).build();
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
