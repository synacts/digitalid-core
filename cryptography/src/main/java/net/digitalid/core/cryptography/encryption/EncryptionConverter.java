package net.digitalid.core.cryptography.encryption;

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
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomAnnotation;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.conversion.converter.types.CustomType;
import net.digitalid.utility.cryptography.InitializationVector;
import net.digitalid.utility.cryptography.InitializationVectorConverter;
import net.digitalid.utility.cryptography.SymmetricKey;
import net.digitalid.utility.cryptography.SymmetricKeyBuilder;
import net.digitalid.utility.cryptography.SymmetricKeyConverter;
import net.digitalid.utility.cryptography.key.PrivateKey;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.cryptography.key.chain.PrivateKeyChain;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.immutable.ImmutableMap;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.math.Element;
import net.digitalid.utility.math.ElementConverter;
import net.digitalid.utility.math.Group;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;

import net.digitalid.core.identification.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifierConverter;

import static net.digitalid.utility.conversion.converter.types.CustomType.*;

/**
 *
 */
public class EncryptionConverter<T> implements Converter<Encryption<T>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    /* -------------------------------------------------- Private Key -------------------------------------------------- */
    
    /**
     * The private key chain of this host instance.
     */
    private final @Nonnull PrivateKeyChain privateKeyChain;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private EncryptionConverter(@Nonnull Converter<T, ?> objectConverter, @Nonnull PrivateKeyChain privateKeyChain) {
        this.objectConverter = objectConverter;
        this.privateKeyChain = privateKeyChain;
    }
    
    @Pure
    public static <T> @Nonnull EncryptionConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter, @Nonnull PrivateKeyChain privateKeyChain) {
        return new EncryptionConverter<>(objectConverter, privateKeyChain);
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull FreezableArrayList<@Nonnull CustomField> fields;
    
    static {
        final @Nonnull Map<@Nonnull String, @Nullable Object> time = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> recipient = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> symmetricKey = new HashMap<>();
        final @Nonnull Map<@Nonnull String, @Nullable Object> initializationVector = new HashMap<>();
        
        fields = FreezableArrayList.withElements(CustomField.with(TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(time)))), CustomField.with(TUPLE.of(HostIdentifierConverter.INSTANCE), "recipient", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(recipient)))), CustomField.with(TUPLE.of(SymmetricKeyConverter.INSTANCE), "symmetricKey", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(symmetricKey)))), CustomField.with(TUPLE.of(InitializationVectorConverter.INSTANCE), "initializationVector", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(initializationVector)))));
    }
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields() {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(objectConverter), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withNoEntries()))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "encryption";
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Encryption<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws ExternalException {
        if (object == null) {
            throw UnexpectedFailureException.with("Cannot convert encryption object that is null");
        }
        int i = 1;
        
        final @Nonnull SymmetricKey symmetricKey = object.getSymmetricKey();
        final @Nonnull InitializationVector initializationVector = object.getInitializationVector();
        
        final @Nonnull Time time = object.getTime();
        final @Nonnull HostIdentifier recipient = object.getRecipient();
        i *= TimeConverter.INSTANCE.convert(time, valueCollector);
        i *= HostIdentifierConverter.INSTANCE.convert(recipient, valueCollector);
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(recipient, time);
        final @Nonnull Element encryptedSymmetricKey = publicKey.getCompositeGroup().getElement(symmetricKey.getValue()).pow(publicKey.getE());
        i *= ElementConverter.INSTANCE.convert(encryptedSymmetricKey, valueCollector);
        i *= InitializationVectorConverter.INSTANCE.convert(initializationVector, valueCollector);
        
        valueCollector.setEncryptionCipher(symmetricKey.getCipher(initializationVector, Cipher.ENCRYPT_MODE));
        i *= objectConverter.convert(object.getObject(), valueCollector);
        valueCollector.popEncryptionCipher();
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <X extends ExternalException> @Nonnull Encryption<T> recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, @Nullable Void externallyProvided) throws ExternalException {
        final @Nonnull Time time = TimeConverter.INSTANCE.recover(selectionResult, externallyProvided);
        final @Nonnull HostIdentifier recipient = HostIdentifierConverter.INSTANCE.recover(selectionResult, externallyProvided);
        final @Nonnull PrivateKey privateKey = privateKeyChain.getKey(time);
        final @Nonnull Group compositeGroup = privateKey.getCompositeGroup();
        final @Nonnull Element encryptedSymmetricKeyValue = ElementConverter.INSTANCE.recover(selectionResult, compositeGroup);
        final @Nonnull SymmetricKey decryptedSymmetricKey = SymmetricKeyBuilder.buildWithValue(encryptedSymmetricKeyValue.pow(privateKey.getD()).getValue());
        final @Nonnull InitializationVector initializationVector = InitializationVectorConverter.INSTANCE.recover(selectionResult, externallyProvided);
    
        selectionResult.setDecryptionCipher(decryptedSymmetricKey.getCipher(initializationVector, Cipher.DECRYPT_MODE));
        // TODO: do we need to hand the externally provided element here?
        final T object = objectConverter.recover(selectionResult, null);
        selectionResult.popDecryptionCipher();
        return EncryptionBuilder.<T>withTime(time).withRecipient(recipient).withSymmetricKey(decryptedSymmetricKey).withInitializationVector(initializationVector).withObject(object).build();
    }
    
}
