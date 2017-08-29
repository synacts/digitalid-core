package net.digitalid.core.packet;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionBuilder;
import net.digitalid.core.compression.CompressionConverter;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.EncryptionBuilder;
import net.digitalid.core.encryption.EncryptionConverter;
import net.digitalid.core.encryption.EncryptionConverterBuilder;
import net.digitalid.core.encryption.RequestEncryption;
import net.digitalid.core.encryption.RequestEncryptionBuilder;
import net.digitalid.core.encryption.ResponseEncryption;
import net.digitalid.core.encryption.ResponseEncryptionBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureBuilder;
import net.digitalid.core.signature.SignatureConverter;
import net.digitalid.core.signature.SignatureConverterBuilder;
import net.digitalid.core.signature.host.HostSignatureCreator;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class RequestTest extends CoreTest {
    
    private final @Nonnull InternalNonHostIdentifier subject = InternalNonHostIdentifier.with("subject@core.digitalid.net");
    
    private static final @Nonnull CompressionConverter<Pack> compressionConverter = CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build();
    
    private static final @Nonnull SignatureConverter<Compression<Pack>> signatureConverter = SignatureConverterBuilder.withObjectConverter(compressionConverter).build();
    
    private static final @Nonnull EncryptionConverter<Signature<Compression<Pack>>> encryptionConverter = EncryptionConverterBuilder.withObjectConverter(signatureConverter).build();
    
    @Test
    public void testRequestConverter() throws ExternalException {
        final @Nonnull String string = "Hello World!";
        final @Nonnull Pack content = Pack.pack(StringConverter.INSTANCE, string);
        final @Nonnull Compression<Pack> compression = CompressionBuilder.withObject(content).build();
        final @Nonnull Signature<Compression<Pack>> signature = SignatureBuilder.withObjectConverter(CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).withObject(compression).withSubject(subject).build();
        final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = EncryptionBuilder.withObject(signature).withRecipient(HostIdentifier.DIGITALID).build();
        final @Nonnull Request request = RequestBuilder.withEncryption(encryption).build();
        final @Nonnull Pack pack = request.pack();
        final @Nonnull byte[] bytes = pack.store();
        
        final @Nonnull Pack recoveredPack = Pack.loadFrom(bytes);
        final @Nonnull Request recoveredRequest = recoveredPack.unpack(RequestConverter.INSTANCE, null);
        final @Nonnull Encryption<Signature<Compression<Pack>>> recoveredEncryption = recoveredRequest.getEncryption();
        final @Nonnull Signature<Compression<Pack>> recoveredSignature = recoveredEncryption.getObject();
        final @Nonnull Compression<Pack> recoveredCompression = recoveredSignature.getObject();
        final @Nonnull Pack recoveredContent = recoveredCompression.getObject();
        final @Nonnull String recoveredString = recoveredContent.unpack(StringConverter.INSTANCE, null);
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
//    @Test
    @Pure
    public void testEncryptionConverter() throws ExternalException {
        final @Nonnull String string = "Hello World!";
        final @Nonnull Pack content = Pack.pack(StringConverter.INSTANCE, string);
        final @Nonnull Compression<Pack> compression = CompressionBuilder.withObject(content).build();
        final @Nonnull Signature<Compression<Pack>> signature = SignatureBuilder.withObjectConverter(CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).withObject(compression).withSubject(subject).build();
        final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = EncryptionBuilder.withObject(signature).withRecipient(HostIdentifier.DIGITALID).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        
        final @Nonnull Encryption<Signature<Compression<Pack>>> recoveredEncryption = XDF.recover(encryptionConverter, null, bytes);
        final @Nonnull Signature<Compression<Pack>> recoveredSignature = recoveredEncryption.getObject();
        final @Nonnull Compression<Pack> recoveredCompression = recoveredSignature.getObject();
        final @Nonnull Pack recoveredContent = recoveredCompression.getObject();
        final @Nonnull String recoveredString = recoveredContent.unpack(StringConverter.INSTANCE, null);
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
//    @Test
    @Pure
    public void testSignatureConverter() throws ExternalException {
        final @Nonnull String string = "Hello World!";
        final @Nonnull Pack content = Pack.pack(StringConverter.INSTANCE, string);
        final @Nonnull Compression<Pack> compression = CompressionBuilder.withObject(content).build();
        final @Nonnull Signature<Compression<Pack>> signature = SignatureBuilder.withObjectConverter(CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).withObject(compression).withSubject(subject).build();
        final @Nonnull byte[] bytes = XDF.convert(signatureConverter, signature);
        
        final @Nonnull Signature<Compression<Pack>> recoveredSignature = XDF.recover(signatureConverter, null, bytes);
        final @Nonnull Compression<Pack> recoveredCompression = recoveredSignature.getObject();
        final @Nonnull Pack recoveredContent = recoveredCompression.getObject();
        final @Nonnull String recoveredString = recoveredContent.unpack(StringConverter.INSTANCE, null);
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
    @Test
    public void testCompressionWithinRequestEncryption() throws ExternalException {
        final @Nonnull CompressionConverter<String> compressionConverter = CompressionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build();
        final @Nonnull EncryptionConverter<Compression<String>> encryptionConverter = EncryptionConverterBuilder.withObjectConverter(compressionConverter).build();
        
        final @Nonnull String string = "Hello World!";
        final @Nonnull Compression<String> compression = CompressionBuilder.withObject(string).build();
        final @Nonnull RequestEncryption<Compression<String>> encryption = RequestEncryptionBuilder.withObject(compression).withRecipient(HostIdentifier.DIGITALID).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        
        final @Nonnull Encryption<Compression<String>> recoveredEncryption = XDF.recover(encryptionConverter, null, bytes);
        final @Nonnull Compression<String> recoveredCompression = recoveredEncryption.getObject();
        final @Nonnull String recoveredString = recoveredCompression.getObject();
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
    @Test
    public void testCompressionWithinResponseEncryption() throws ExternalException {
        final @Nonnull CompressionConverter<String> compressionConverter = CompressionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build();
        final @Nonnull EncryptionConverter<Compression<String>> encryptionConverter = EncryptionConverterBuilder.withObjectConverter(compressionConverter).build();
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        
        final @Nonnull String string = "Hello World!";
        final @Nonnull Compression<String> compression = CompressionBuilder.withObject(string).build();
        final @Nonnull ResponseEncryption<Compression<String>> encryption = ResponseEncryptionBuilder.withObject(compression).withSymmetricKey(symmetricKey).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        
        final @Nonnull Encryption<Compression<String>> recoveredEncryption = XDF.recover(encryptionConverter, symmetricKey, bytes);
        final @Nonnull Compression<String> recoveredCompression = recoveredEncryption.getObject();
        final @Nonnull String recoveredString = recoveredCompression.getObject();
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
    @Test
    public void testCompressionWithinHostSignature() throws ExternalException {
        final @Nonnull CompressionConverter<String> compressionConverter = CompressionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build();
        final @Nonnull SignatureConverter<Compression<String>> signatureConverter = SignatureConverterBuilder.withObjectConverter(compressionConverter).build();
        
        final @Nonnull String string = "Hello World!";
        final @Nonnull Compression<String> compression = CompressionBuilder.withObject(string).build();
        final @Nonnull Signature<Compression<String>> signature = HostSignatureCreator.sign(compression, compressionConverter).to(subject).as(subject);
        final @Nonnull byte[] bytes = XDF.convert(signatureConverter, signature);
        
        final @Nonnull Signature<Compression<String>> recoveredSignature = XDF.recover(signatureConverter, null, bytes);
        final @Nonnull Compression<String> recoveredCompression = recoveredSignature.getObject();
        final @Nonnull String recoveredString = recoveredCompression.getObject();
        
        assertThat(recoveredString).isEqualTo(string);
    }
    
}
