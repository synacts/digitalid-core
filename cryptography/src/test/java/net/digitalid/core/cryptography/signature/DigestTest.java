package net.digitalid.core.cryptography.signature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DigestTest {
    
    @Test
    public void shouldProduceSameDigest() throws Exception {
        final @Nonnull Random random = new Random();
        final @Nonnull MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final @Nonnull DigestOutputStream digestOutputStream = new DigestOutputStream(byteArrayOutputStream, messageDigest);
        
        final @Nonnull byte[] randomBytes = new byte[300];
        random.nextBytes(randomBytes);
        
        digestOutputStream.write(randomBytes);
        
        Assert.assertTrue("Expected equal bytes in output stream, but got " + Arrays.toString(randomBytes) + " != " + Arrays.toString(byteArrayOutputStream.toByteArray()), Arrays.equals(randomBytes, byteArrayOutputStream.toByteArray()));
        byte[] digestOutgoing = digestOutputStream.getMessageDigest().digest();
    
        final @Nonnull byte[] incomingBytes = new byte[300];
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(randomBytes);
        final @Nonnull DigestInputStream digestInputStream = new DigestInputStream(byteArrayInputStream, messageDigest);
        digestInputStream.read(incomingBytes);
        
        byte[] digestIncoming = digestInputStream.getMessageDigest().digest();
        
        Assert.assertTrue("Expected equal hash for incoming bytes and outgoing bytes, but got: " + Arrays.toString(digestOutgoing) + " != " + Arrays.toString(digestIncoming), Arrays.equals(digestOutgoing, digestIncoming));
    }
    
    @Test
    public void shouldAllowPreAndPostFillingOfOutputStream() throws Exception {
        final @Nonnull Random random = new Random();
        final @Nonnull MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
        final @Nonnull byte[] preDigest = new byte[29];
        random.nextBytes(preDigest);
        byteArrayOutputStream.write(preDigest);
        
        final @Nonnull DigestOutputStream digestOutputStream = new DigestOutputStream(byteArrayOutputStream, messageDigest);
        
        final @Nonnull byte[] randomBytes = new byte[300];
        random.nextBytes(randomBytes);
        
        digestOutputStream.write(randomBytes);
    
        final @Nonnull byte[] postDigest = new byte[29];
        random.nextBytes(postDigest);
        byteArrayOutputStream.write(postDigest);
        byteArrayOutputStream.flush();
    
        byte[] digestOutgoing = digestOutputStream.getMessageDigest().digest();
    
        final @Nonnull byte[] incomingPreDigest = new byte[29];
        final @Nonnull byte[] incomingBytes = new byte[300];
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        byteArrayInputStream.read(incomingPreDigest);
        Assert.assertTrue("Expected equal array for incoming pre-digest bytes and outgoing bytes, but got: " + Arrays.toString(preDigest) + " != " + Arrays.toString(incomingPreDigest), Arrays.equals(preDigest, incomingPreDigest));
        
        final @Nonnull DigestInputStream digestInputStream = new DigestInputStream(byteArrayInputStream, messageDigest);
        digestInputStream.read(incomingBytes);
        
        byte[] digestIncoming = digestInputStream.getMessageDigest().digest();
        
        Assert.assertTrue("Expected equal hash for incoming bytes and outgoing bytes, but got: " + Arrays.toString(digestOutgoing) + " != " + Arrays.toString(digestIncoming), Arrays.equals(digestOutgoing, digestIncoming));
    
        final @Nonnull byte[] incomingPostDigest = new byte[29];
        byteArrayInputStream.read(incomingPostDigest);
        Assert.assertTrue("Expected equal array for incoming post-digest bytes and outgoing bytes, but got: " + Arrays.toString(postDigest) + " != " + Arrays.toString(incomingPostDigest), Arrays.equals(postDigest, incomingPostDigest));
    }
    
}
