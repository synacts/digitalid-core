package net.digitalid.core.signature.client;

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
public class ClientSignatureConverter<@Unspecifiable TYPE> implements Converter<ClientSignature<TYPE>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<TYPE, ?> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private ClientSignatureConverter(@Nonnull Converter<TYPE, ?> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <T> @Nonnull ClientSignatureConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new ClientSignatureConverter<>(objectConverter);
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super ClientSignature<TYPE>> getType() {
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
        return "net.digitalid.core.encryption";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        return null;
    }
    
    @Pure
    @Override 
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified ClientSignature<TYPE> object, @Nonnull @NonCaptured @Modified Encoder<X> encoder) throws ExternalException {
        int i = 1;
        try {
            encoder.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        i *= InternalIdentifierConverter.INSTANCE.convert(object == null ? null : object.getSubject(), encoder);
        i *= TimeConverter.INSTANCE.convert(object == null ? null : object.getTime(), encoder);
        i *= objectConverter.convert(object == null ? null : object.getObject(), encoder);
        
        final @Nonnull DigestOutputStream digestOutputStream = encoder.popSignatureDigest();
        
        if (object != null) {
            final @Nonnull SecretCommitment commitment = object.getSecretCommitment();
            // secret commitment
            i *= SecretCommitmentConverter.INSTANCE.convert(commitment, encoder);
            
//            final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT.get());
            final @Nonnull Exponent r = ExponentBuilder.withValue(BigInteger.ONE).build();
            
            final @Nonnull BigInteger t = object.getHash(commitment.getPublicKey().getAu().pow(r));
            encoder.encodeInteger(t);
            
            final @Nonnull BigInteger hash = new BigInteger(1, digestOutputStream.getMessageDigest().digest());
            final @Nonnull Exponent h = ExponentBuilder.withValue(t.xor(hash)).build();
            
            final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
            i *= ExponentConverter.INSTANCE.convert(s, encoder);
        } else {
            i *= SecretCommitmentConverter.INSTANCE.convert(null, encoder);
            encoder.encodeInteger(null);
            i *= ExponentConverter.INSTANCE.convert(null, encoder);
        }
        
        return i;
    }
    
    @Pure
    @Override
    public <X extends ExternalException> @Nullable ClientSignature<TYPE> recover(@Nonnull @NonCaptured @Modified Decoder<X> decoder, @Nullable Void externallyProvided) throws ExternalException {
        try {
            decoder.setSignatureDigest(MessageDigest.getInstance("SHA-256"));
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
        final @Nullable InternalIdentifier subject = InternalIdentifierConverter.INSTANCE.recover(decoder, null);
        final @Nullable Time time = TimeConverter.INSTANCE.recover(decoder, null);
        final @Nullable TYPE object = objectConverter.recover(decoder, null);
        
        final @Nonnull DigestInputStream digestInputStream = decoder.popSignatureDigest();
    
        // secret commitment
        final @Nullable SecretCommitment commitment = SecretCommitmentConverter.INSTANCE.recover(decoder, null);
        final @Nullable BigInteger t = decoder.getInteger();
        final @Nullable Exponent s = ExponentConverter.INSTANCE.recover(decoder, null);
        
        final @Nonnull ClientSignature<TYPE> clientSignature = ClientSignatureBuilder.withObject(object).withSubject(subject).withSecretCommitment(commitment).withTime(time).build();
        final @Nonnull BigInteger hash = new BigInteger(1, digestInputStream.getMessageDigest().digest());
        clientSignature.verifySignature(t, s, hash);
        return clientSignature;
    }
    
}
