package net.digitalid.core.signature.client;

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
import net.digitalid.utility.exceptions.MissingSupportException;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.TimeConverter;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.ExponentConverter;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;

/**
 * TODO: Think about nullable and non-nullable cases.
 */
public class ClientSignatureConverter<T> implements Converter<ClientSignature<T>,Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private ClientSignatureConverter(@Nonnull Converter<T, ?> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <T> @Nonnull ClientSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new ClientSignatureConverter<>(objectConverter);
    }
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "clientsignature";
    }
    
    @Pure
    @Override
    public @Nonnull ImmutableList<CustomField> getFields() {
        return null;
    }
    
    @Pure
    @Override 
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified ClientSignature<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws ExternalException {
        int i = 1;
        try {
            valueCollector.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSubject(), valueCollector);
        i *= TimeConverter.INSTANCE.convert(object == null ? null : object.getTime(), valueCollector);
        i *= objectConverter.convert(object == null ? null : object.getElement(), valueCollector);
        
        final @Nonnull DigestOutputStream digestOutputStream = valueCollector.popSignatureDigest();
        
        if (object != null) {
            final @Nonnull SecretCommitment commitment = object.getSecretCommitment();
            // secret commitment
            i *= SecretCommitmentConverter.INSTANCE.convert(commitment, valueCollector);
            
//            final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT.get());
            final @Nonnull Exponent r = ExponentBuilder.withValue(BigInteger.ONE).build();
            
            final @Nonnull BigInteger t = object.getHash(commitment.getPublicKey().getAu().pow(r));
            valueCollector.setNullableInteger(t);
            
            final @Nonnull BigInteger hash = new BigInteger(1, digestOutputStream.getMessageDigest().digest());
            final @Nonnull Exponent h = ExponentBuilder.withValue(t.xor(hash)).build();
            
            final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
            i *= ExponentConverter.INSTANCE.convert(s, valueCollector);
        } else {
            i *= SecretCommitmentConverter.INSTANCE.convert(null, valueCollector);
            valueCollector.setNullableInteger(null);
            i *= ExponentConverter.INSTANCE.convert(null, valueCollector);
        }
        
        return i;
    }
    
    @Pure
    @Override
    public <X extends ExternalException> @Nullable ClientSignature<T> recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, @Nullable Void externallyProvided) throws ExternalException {
        try {
            selectionResult.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        final @Nullable InternalIdentifier subject = InternalIdentifierConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable Time time = TimeConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable T object = objectConverter.recover(selectionResult, null);
        
        final @Nonnull DigestInputStream digestInputStream = selectionResult.popSignatureDigest();
    
        // secret commitment
        final @Nullable SecretCommitment commitment = SecretCommitmentConverter.INSTANCE.recover(selectionResult, null);
        final @Nullable BigInteger t = selectionResult.getInteger();
        final @Nullable Exponent s = ExponentConverter.INSTANCE.recover(selectionResult, null);
        
        final @Nonnull ClientSignature<T> clientSignature = ClientSignatureBuilder.withElement(object).withSecretCommitment(commitment).withSubject(subject).withTime(time).build();
        final @Nonnull BigInteger hash = new BigInteger(1, digestInputStream.getMessageDigest().digest());
        clientSignature.verifySignature(t, s, hash);
        return clientSignature;
    }
    
}
