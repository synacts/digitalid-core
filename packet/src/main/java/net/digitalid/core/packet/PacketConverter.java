package net.digitalid.core.packet;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionConverter;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.EncryptionConverter;
import net.digitalid.core.encryption.EncryptionConverterBuilder;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureConverter;
import net.digitalid.core.signature.SignatureConverterBuilder;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

/**
 * This class converts and recovers a {@link Packet packet}.
 * 
 * @see RequestConverter
 * @see ResponseConverter
 */
@Immutable
public abstract class PacketConverter<@Unspecifiable PACKET extends Packet> implements Converter<PACKET, Void> {
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.packet";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull CompressionConverter<Pack> compressionConverter = CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build();
    
    private static final @Nonnull SignatureConverter<Compression<Pack>> signatureConverter = SignatureConverterBuilder.withObjectConverter(compressionConverter).build();
    
    private static final @Nonnull EncryptionConverter<Signature<Compression<Pack>>> encryptionConverter = EncryptionConverterBuilder.withObjectConverter(signatureConverter).build();
    
    private static final @Nonnull @NonNullableElements ImmutableList<CustomField> fields = ImmutableList.withElements(CustomField.with(TUPLE.of(encryptionConverter), "encryption", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))));
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        return fields;
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull PACKET packet, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeObject(encryptionConverter, packet.getEncryption());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    public abstract @Capturable @Nonnull PACKET recover(@Nonnull Encryption<Signature<Compression<Pack>>> encryption);
    
    @Pure
    @Override
    public @Capturable <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull PACKET recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION, RecoveryException {
        final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = decoder.decodeObject(encryptionConverter, null);
        return recover(encryption);
    }
    
}
