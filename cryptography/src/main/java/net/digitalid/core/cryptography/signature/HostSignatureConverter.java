package net.digitalid.core.cryptography.signature;

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
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.conversion.exceptions.FailedValueRecoveryException;
import net.digitalid.utility.cryptography.key.PrivateKey;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.cryptography.key.chain.PrivateKeyChain;
import net.digitalid.utility.cryptography.key.chain.PublicKeyChain;
import net.digitalid.utility.exceptions.MissingSupportException;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;

import net.digitalid.core.cryptography.signature.exceptions.InvalidHostSignatureException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;

/**
 *
 */
public class HostSignatureConverter<T> implements Converter<HostSignature<T>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    private final @Nullable PublicKeyChain publicKeyChain;
    
    private final @Nullable PrivateKeyChain privateKeyChain;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private HostSignatureConverter(@Nonnull Converter<T, ?> objectConverter, @Nullable PublicKeyChain publicKeyChain, @Nullable PrivateKeyChain privateKeyChain) {
        this.objectConverter = objectConverter;
        Require.that(publicKeyChain != null || privateKeyChain != null);
        
        this.publicKeyChain = publicKeyChain;
        this.privateKeyChain = privateKeyChain;
    }
    
    @Pure
    public static <T> @Nonnull HostSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter, @Nonnull PublicKeyChain publicKeyChain) {
        return new HostSignatureConverter<>(objectConverter, publicKeyChain, null);
    }
    
    @Pure
    public static <T> @Nonnull HostSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter, @Nonnull PrivateKeyChain privateKeyChain) {
        return new HostSignatureConverter<>(objectConverter, null, privateKeyChain);
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
    public <X extends ExternalException> int convert(@NonCaptured @Unmodified HostSignature<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws X {
        Require.that(privateKeyChain != null);
        
        int i = 1;
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSubject(), valueCollector);
        i *= TimeConverter.INSTANCE.convert(object == null ? null : object.getTime(), valueCollector);
        i *= objectConverter.convert(object == null ? null : object.getElement(), valueCollector);
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSigner(), valueCollector);
        try {
            valueCollector.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        i *= objectConverter.convert(object.getElement(), valueCollector);
        final @Nullable DigestOutputStream digestOutputStream = valueCollector.popSignatureDigest();
        final @Nonnull BigInteger hash = new BigInteger(1, digestOutputStream.getMessageDigest().digest());
    
        final @Nonnull PrivateKey privateKey = privateKeyChain.getKey(object.getTime());
        final @Nonnull BigInteger signatureValue = privateKey.powD(hash).getValue();
        
        valueCollector.setNullableInteger(signatureValue);
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> @Nonnull HostSignature<T> recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, Void externallyProvided) throws X {
        try {
            selectionResult.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        final @Nullable InternalIdentifier subject = InternalIdentifierConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable Time time = TimeConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable T object = objectConverter.recover(selectionResult, null);
        final @Nullable InternalIdentifier signer = InternalIdentifierConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable BigInteger signatureValue = selectionResult.getInteger();
        final @Nonnull DigestInputStream digestInputStream = selectionResult.popSignatureDigest();
    
        final @Nonnull PublicKey publicKey = publicKeyChain.getKey(time);
        final @Nonnull BigInteger hash = new BigInteger(1, digestInputStream.getMessageDigest().digest());
        
        final @Nonnull HostSignature<T> hostSignature = HostSignatureBuilder.withElement(object).withSigner(signer).withSubject(subject).withTime(time).build();
    
        try {
            hostSignature.verifySignature(publicKey, signatureValue, hash);
        } catch (InvalidHostSignatureException e) {
            FailedValueRecoveryException.of(e);
        }
        return hostSignature;
    }
    
}
