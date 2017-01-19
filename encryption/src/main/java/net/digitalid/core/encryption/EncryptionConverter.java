package net.digitalid.core.encryption;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.exceptions.UncheckedException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.immutable.ImmutableMap;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeConverter;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementConverter;
import net.digitalid.core.group.Group;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifierConverter;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;
import net.digitalid.core.symmetrickey.SymmetricKeyConverter;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * 
 */
public class EncryptionConverter<TYPE> implements Converter<Encryption<TYPE>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<TYPE, Void> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private EncryptionConverter(@Nonnull Converter<TYPE, Void> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <TYPE> @Nonnull EncryptionConverter<TYPE> getInstance(@Nonnull Converter<TYPE, Void> objectConverter) {
        return new EncryptionConverter<>(objectConverter);
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super Encryption<TYPE>> getType() {
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
        final @Nonnull Map<@Nonnull String, @Nullable Object> time = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> recipient = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> symmetricKey = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> initializationVector = new HashMap<>();
        
        fields = FreezableArrayList.withElements(
                CustomField.with(TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(time)))),
                CustomField.with(TUPLE.of(HostIdentifierConverter.INSTANCE), "recipient", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(recipient)))),
                CustomField.with(TUPLE.of(SymmetricKeyConverter.INSTANCE), "symmetricKey", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(symmetricKey)))),
                CustomField.with(TUPLE.of(InitializationVectorConverter.INSTANCE), "initializationVector", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(initializationVector))))
        );
    }
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(objectConverter), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withNoEntries()))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Encryption<TYPE> object, @Nonnull @NonCaptured @Modified Encoder<X> encoder) throws ExternalException {
        if (object == null) {
            throw UncheckedException.with("Cannot convert encryption object that is null"); // TODO: Why not? Just encode it especially.
        }
        int i = 1;
        
        final @Nonnull SymmetricKey symmetricKey = object.getSymmetricKey();
        final @Nonnull InitializationVector initializationVector = object.getInitializationVector();
        
        final @Nonnull Time time = object.getTime();
        final @Nonnull HostIdentifier recipient = object.getRecipient();
        i *= TimeConverter.INSTANCE.convert(time, encoder);
        i *= HostIdentifierConverter.INSTANCE.convert(recipient, encoder);
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(recipient, time);
        final @Nonnull Element encryptedSymmetricKey = publicKey.getCompositeGroup().getElement(symmetricKey.getValue()).pow(publicKey.getE());
        i *= ElementConverter.INSTANCE.convert(encryptedSymmetricKey, encoder);
        i *= InitializationVectorConverter.INSTANCE.convert(initializationVector, encoder);
        
        encoder.setEncryptionCipher(symmetricKey.getCipher(initializationVector, Cipher.ENCRYPT_MODE));
        i *= objectConverter.convert(object.getObject(), encoder);
        encoder.popEncryptionCipher();
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <X extends ExternalException> @Nonnull Encryption<TYPE> recover(@Nonnull @NonCaptured @Modified Decoder<X> decoder, @Nullable Void provided) throws ExternalException {
        final @Nonnull Time time = TimeConverter.INSTANCE.recover(decoder, null);
        final @Nonnull HostIdentifier recipient = HostIdentifierConverter.INSTANCE.recover(decoder, null);
        final @Nonnull PrivateKey privateKey = PrivateKeyRetriever.retrieve(recipient, time);
        final @Nonnull Group compositeGroup = privateKey.getCompositeGroup();
        final @Nonnull Element encryptedSymmetricKeyValue = ElementConverter.INSTANCE.recover(decoder, compositeGroup);
        final @Nonnull SymmetricKey decryptedSymmetricKey = SymmetricKeyBuilder.buildWithValue(encryptedSymmetricKeyValue.pow(privateKey.getD()).getValue());
        final @Nonnull InitializationVector initializationVector = InitializationVectorConverter.INSTANCE.recover(decoder, null);
    
        decoder.setDecryptionCipher(decryptedSymmetricKey.getCipher(initializationVector, Cipher.DECRYPT_MODE));
        final TYPE object = objectConverter.recover(decoder, null);
        decoder.popDecryptionCipher();
        return EncryptionBuilder.<TYPE>withTime(time).withRecipient(recipient).withSymmetricKey(decryptedSymmetricKey).withInitializationVector(initializationVector).withObject(object).build();
    }
    
}
