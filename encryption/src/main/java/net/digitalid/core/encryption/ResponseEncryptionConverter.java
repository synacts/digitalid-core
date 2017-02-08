package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
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
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * This class converts and recovers a {@link ResponseEncryption response encryption}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ResponseEncryptionConverter<@Unspecifiable OBJECT> implements GenericTypeConverter<OBJECT, ResponseEncryption<OBJECT>, @Nullable SymmetricKey> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super ResponseEncryption<OBJECT>> getType() {
        return ResponseEncryption.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "ResponseEncryption";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.encryption";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull @NonNullableElements ImmutableList<CustomField> fields;
    
    static {
        fields = ImmutableList.withElements(
                CustomField.with(TUPLE.of(InitializationVectorConverter.INSTANCE), "initializationVector", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
        );
    }
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(getObjectConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Converter<? super ResponseEncryption<OBJECT>, @Nullable SymmetricKey> getSupertypeConverter() {
        return EncryptionConverterBuilder.withObjectConverter(getObjectConverter()).build();
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull ResponseEncryption<OBJECT> encryption, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(InitializationVectorConverter.INSTANCE, encryption.getInitializationVector());
        
        encoder.startEncrypting(encryption.getSymmetricKey().getCipher(encryption.getInitializationVector(), Cipher.ENCRYPT_MODE));
        encoder.encodeObject(getObjectConverter(), encryption.getObject());
        encoder.stopEncrypting();
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override 
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull ResponseEncryption<OBJECT> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nullable SymmetricKey symmetricKey) throws EXCEPTION, RecoveryException {
        if (symmetricKey == null) { throw RecoveryExceptionBuilder.withMessage("In order to recover a response encryption, the symmetric key has to be provided.").build(); }
        
        final @Nonnull InitializationVector initializationVector = decoder.decodeObject(InitializationVectorConverter.INSTANCE, null);
        
        decoder.startDecrypting(symmetricKey.getCipher(initializationVector, Cipher.DECRYPT_MODE));
        final OBJECT object = decoder.decodeObject(getObjectConverter(), null);
        decoder.stopDecrypting();
        
        return ResponseEncryptionBuilder.withObject(object).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).build();
    }
    
}
