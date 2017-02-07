package net.digitalid.core.encryption;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.interfaces.GenericTypeConverter;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeConverter;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementConverter;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifierConverter;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;
import net.digitalid.core.symmetrickey.SymmetricKeyConverter;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * This class converts and recovers encrypted objects.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class EncryptionConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, Encryption<OBJECT>, Void> {
    
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
    
    private static final @Nonnull FreezableArrayList<@Nonnull CustomField> fields;
    
    static {
        fields = FreezableArrayList.withElements(
                CustomField.with(TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(TUPLE.of(HostIdentifierConverter.INSTANCE), "recipient", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(TUPLE.of(SymmetricKeyConverter.INSTANCE), "symmetricKey", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(TUPLE.of(InitializationVectorConverter.INSTANCE), "initializationVector", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
        );
    }
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull Encryption<OBJECT> encryption, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(TimeConverter.INSTANCE, encryption.getTime());
        encoder.encodeObject(HostIdentifierConverter.INSTANCE, encryption.getRecipient());
        final @Nonnull Element encryptedSymmetricKey = encryption.getPublicKey().getCompositeGroup().getElement(encryption.getSymmetricKey().getValue()).pow(encryption.getPublicKey().getE());
        encoder.encodeObject(ElementConverter.INSTANCE, encryptedSymmetricKey);
        encoder.encodeObject(InitializationVectorConverter.INSTANCE, encryption.getInitializationVector());
        
        encoder.startEncrypting(encryption.getSymmetricKey().getCipher(encryption.getInitializationVector(), Cipher.ENCRYPT_MODE));
        encoder.encodeObject(getObjectConverter(), encryption.getObject());
        encoder.stopEncrypting();
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull Encryption<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nullable Void provided) throws EXCEPTION, RecoveryException {
        final @Nonnull Time time = decoder.decodeObject(TimeConverter.INSTANCE, null);
        final @Nonnull HostIdentifier recipient = decoder.decodeObject(HostIdentifierConverter.INSTANCE, null);
        final @Nonnull PrivateKey privateKey;
        try {
            privateKey = PrivateKeyRetriever.retrieve(recipient, time);
        } catch (@Nonnull RequestException exception) {
            throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the private key of $.", recipient)).withCause(exception).build();
        }
        final @Nonnull Element encryptedSymmetricKeyValue = decoder.decodeObject(ElementConverter.INSTANCE, privateKey.getCompositeGroup());
        final @Nonnull SymmetricKey decryptedSymmetricKey = SymmetricKeyBuilder.buildWithValue(privateKey.powD(encryptedSymmetricKeyValue).getValue());
        final @Nonnull InitializationVector initializationVector = decoder.decodeObject(InitializationVectorConverter.INSTANCE, null);
        
        decoder.startDecrypting(decryptedSymmetricKey.getCipher(initializationVector, Cipher.DECRYPT_MODE));
        final OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        decoder.stopDecrypting();
        
        try {
            return EncryptionBuilder.withObject(object).withRecipient(recipient).withTime(time).withSymmetricKey(decryptedSymmetricKey).withInitializationVector(initializationVector).build();
        } catch (@Nonnull ExternalException exception) {
            throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the public key of $.", recipient)).withCause(exception).build();
        }
    }
    
}
