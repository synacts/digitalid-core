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
import net.digitalid.utility.cryptography.SymmetricKeyConverter;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.immutable.ImmutableMap;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifierConverter;

import static net.digitalid.utility.conversion.converter.types.CustomType.*;

/**
 *
 */
public class EncryptionConverter<T> implements Converter<Encryption<T>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private EncryptionConverter(@Nonnull Converter<T, ?> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    @Pure
    public static <T> @Nonnull EncryptionConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new EncryptionConverter<>(objectConverter);
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
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Encryption<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws X {
        if (object == null) {
            throw UnexpectedFailureException.with("Cannot convert encryption object that is null");
        }
        int i = 1;
        
        final @Nonnull SymmetricKey symmetricKey = object.getSymmetricKey();
        final @Nonnull InitializationVector initializationVector = object.getInitializationVector();
        
        i *= TimeConverter.INSTANCE.convert(object.getTime(), valueCollector);
        i *= HostIdentifierConverter.INSTANCE.convert(object.getRecipient(), valueCollector);
        // TODO: The symmetric key MUST be encrypted with the public key of the receiver (otherwise encryption is pointless).
        i *= SymmetricKeyConverter.INSTANCE.convert(symmetricKey, valueCollector);
        i *= InitializationVectorConverter.INSTANCE.convert(initializationVector, valueCollector);
        
        valueCollector.setEncryptionCipher(symmetricKey.getCipher(initializationVector, Cipher.ENCRYPT_MODE));
        i *= objectConverter.convert(object.getObject(), valueCollector);
        valueCollector.popEncryptionCipher();
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <X extends ExternalException> @Nonnull Encryption<T> recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, @Nullable Void externallyProvided) throws X {
        final @Nonnull Time time = TimeConverter.INSTANCE.recover(selectionResult, externallyProvided);
        final @Nonnull HostIdentifier recipient = HostIdentifierConverter.INSTANCE.recover(selectionResult, externallyProvided);
        final @Nonnull SymmetricKey encryptedSymmetricKey = SymmetricKeyConverter.INSTANCE.recover(selectionResult, externallyProvided);
        // TODO: See above: Symmetric key is expected to be encrypted and we need to decrypt it here.
        final @Nonnull InitializationVector initializationVector = InitializationVectorConverter.INSTANCE.recover(selectionResult, externallyProvided);
    
        selectionResult.setDecryptionCipher(encryptedSymmetricKey.getCipher(initializationVector, Cipher.DECRYPT_MODE));
        // TODO: do we need to hand the externally provided element here?
        final T object = objectConverter.recover(selectionResult, null);
        selectionResult.popDecryptionCipher();
        return EncryptionBuilder.<T>withTime(time).withRecipient(recipient).withSymmetricKey(encryptedSymmetricKey).withInitializationVector(initializationVector).withObject(object).build();
    }
    
}
