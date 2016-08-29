package net.digitalid.core.cryptography.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompressionTest {
    
    @Test
    public void shouldCompressOutputStream() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] inCompression = new byte[128];
        for (int i = 0; i < 128; i++) {
            inCompression[i] = 1;
        }
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream);
        deflaterOutputStream.write(inCompression);
        deflaterOutputStream.close();

        final byte[] bytes = outputStream.toByteArray();

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        byte[] incomingInCompression = new byte[128];

        final @Nonnull InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
        inflaterInputStream.read(incomingInCompression);
        inflaterInputStream.close();

        Assert.assertTrue(Arrays.equals(inCompression, incomingInCompression));
    }
    
    @Test
    public void shouldCompressOutputStreamMixed() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Random random = new Random();

        byte[] preCompression = new byte[128];
        random.nextBytes(preCompression);
        outputStream.write(preCompression);

        byte[] inCompression = new byte[128];
        for (int i = 0; i < 128; i++) {
            inCompression[i] = 1;
        }
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream);
        deflaterOutputStream.write(inCompression);
        deflaterOutputStream.finish();

        byte[] postCompression = new byte[128];
        random.nextBytes(postCompression);
        outputStream.write(postCompression);
        deflaterOutputStream.close();

        final byte[] bytes = outputStream.toByteArray();

        @Nonnull InputStream inputStream = new ByteArrayInputStream(bytes);

        byte[] incomingPreCompression = new byte[128];
        byte[] incomingInCompression = new byte[128];
        byte[] incomingPostCompression = new byte[128];

        Assert.assertEquals(128, inputStream.read(incomingPreCompression));
        Assert.assertTrue(Arrays.equals(preCompression, incomingPreCompression));

        final @Nonnull BufferedInflaterInputStream inflaterInputStream = new BufferedInflaterInputStream(inputStream);
        Assert.assertEquals(128, inflaterInputStream.read(incomingInCompression));
        
        Assert.assertTrue(Arrays.equals(inCompression, incomingInCompression));

        inputStream = inflaterInputStream.finish();
        Assert.assertEquals(128, inputStream.read(incomingPostCompression));

        Assert.assertTrue("The written post-compression byte array is different than the read post-compression byte array: " + Arrays.toString(postCompression) + " != " + Arrays.toString(incomingPostCompression), Arrays.equals(postCompression, incomingPostCompression));
    }
    
}
