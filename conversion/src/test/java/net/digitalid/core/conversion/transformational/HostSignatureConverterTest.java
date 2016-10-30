package net.digitalid.core.conversion.transformational;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
import net.digitalid.utility.conversion.converter.types.CustomType;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.conversion.converter.XDF;
import net.digitalid.core.cryptography.signature.HostSignatureBuilder;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.HostSignature;
import net.digitalid.core.signature.HostSignatureConverter;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class HostSignatureConverterTest extends CryptographyTestBase {
    
    private static class StringConverter implements Converter<String, Void> {
    
        public static @Nonnull StringConverter INSTANCE = new StringConverter();
        
        @Pure
        @Override
        public @Nonnull String getName() {
            return "string";
        }
        
        @Pure
        @Override
        public @Nonnull ImmutableList<CustomField> getFields() {
            return ImmutableList.withElements(CustomField.with(CustomType.STRING, "value", ImmutableList.withElements()));
        }
        
        @Pure
        @Override
        public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified String object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws ExternalException {
            valueCollector.setNullableString(object);
            return 1;
        }
        
        @Pure
        @Override
        public <X extends ExternalException> @Nullable String recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, @Nullable Void externallyProvided) throws ExternalException {
            return selectionResult.getString();
        }
    
    }
    
    @Test
    public void shouldSignAndVerify() throws Exception {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        final @Nonnull InternalIdentifier signer = InternalIdentifier.with("alice@digitalid.net");
        
        final @Nonnull HostSignature<@Nonnull String> signedIdentifier = HostSignatureBuilder.withElement(message).withSigner(signer).withSubject(subject).withTime(TimeBuilder.build()).build();
    
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(signedIdentifier, HostSignatureConverter.getInstance(StringConverter.INSTANCE), byteArrayOutputStream);
    
        final @Nonnull byte[] signedBytes = byteArrayOutputStream.toByteArray();
        Assert.assertTrue(signedBytes.length > 0);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(signedBytes);
        final HostSignature<String> recoveredObject = XDF.recover(HostSignatureConverter.getInstance(StringConverter.INSTANCE), byteArrayInputStream);
        
        Assert.assertEquals(message, recoveredObject.getElement());
    }
}
