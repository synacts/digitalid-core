package net.digitalid.core.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.string.Strings;

import net.digitalid.core.conversion.streams.input.BufferedInflaterInputStream;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
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
    @TODO(task = "I have no idea why this test works and the one at the bottom does not (even when using the BufferedInflaterInputStream).", date = "2017-02-10", author = Author.KASPAR_ETTER)
    public void shouldCompressOutputStreamMixed() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Random random = new Random();
        
        byte[] preCompression = new byte[128];
        random.nextBytes(preCompression);
        outputStream.write(preCompression);
        
        byte[] inCompression = new byte[128];
        random.nextBytes(inCompression);
//        for (int i = 0; i < 128; i++) {
//            inCompression[i] = (byte) (i % 8);
//        }
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream);
        deflaterOutputStream.write(inCompression);
        deflaterOutputStream.finish();
        
        byte[] postCompression = new byte[128];
        random.nextBytes(postCompression);
        outputStream.write(postCompression);
        deflaterOutputStream.close();
        
        final byte[] bytes = outputStream.toByteArray();
        
        System.out.println("bytes total: " + bytes.length + "; compressed bytes: " + (bytes.length - 256));
        
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
    
    @Pure
    private void printCompression(@Nonnull byte[] bytes) throws IOException {
        final @Nonnull Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        deflaterOutputStream.write(bytes);
        deflaterOutputStream.finish();
        System.out.println("Input:  " + Strings.hexWithSpaces(bytes));
        System.out.println("Output: " + Strings.hexWithSpaces(byteArrayOutputStream.toByteArray()));
        System.out.println();
    }
    
    @Pure
//    @Test
    public void testCompressions() throws Exception {
        for (int i = 0; i < 6; i++) {
            printCompression(new byte[1]);
        }
        
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        printCompression(bytes);
    }
    
    @Pure
    @TODO(task = "Why do we still have a single unread byte (with value zero) sometimes for small input lengths with the 'nowrap' inflater and deflater alternative?", date = "2017-02-10", author = Author.KASPAR_ETTER)
    private void printNumberOfUnreadBytes(@Nonnull byte[] bytes) throws IOException {
        final @Nonnull Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION); // Alternatively: new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        final @Nonnull Inflater inflater = new Inflater(); // Alternatively: new Inflater(true);
        
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        new DataOutputStream(deflaterOutputStream).write(bytes);
        deflaterOutputStream.finish();
        final @Nonnull byte[] compressedBytes = byteArrayOutputStream.toByteArray();
        
        final @Nonnull byte[] read = new byte[bytes.length];
        
        final @Nonnull LoggingByteArrayInputStream byteArrayInputStream = new LoggingByteArrayInputStream(compressedBytes, false);
        final @Nonnull InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream, inflater, 1);
        new DataInputStream(inflaterInputStream).readFully(read);
        
        final int position = byteArrayInputStream.getPosition();
        final int unread = compressedBytes.length - byteArrayInputStream.getPosition();
        System.out.println("Unread bytes: " + Strings.hexWithSpaces(compressedBytes, position, unread) + "; Input length: " + bytes.length + "; Inflater read: " + inflater.getBytesRead() + "; Position: " + position + (unread != 4 ? " <- ##############" : ""));
        Assert.assertArrayEquals(bytes, read);
    }
    
    @Pure
//    @Test
    public void testUnreadBytes() throws Exception {
        for (int i = 0; i < 50; i++) {
            final @Nonnull byte[] bytes = new byte[i];//new SecureRandom().nextInt(10000)];
            new SecureRandom().nextBytes(bytes);
            printNumberOfUnreadBytes(bytes);
        }
    }
    
    @Pure
//    @Test
    public void testDataAfterCompression() throws Exception {
        final @Nonnull Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        final @Nonnull Inflater inflater = new Inflater();
        
//        final @Nonnull ByteArrayOutputStream b = new ByteArrayOutputStream();
//        new DataOutputStream(b).writeUTF("W");
//        System.out.println("W in bytes: " + Strings.hexWithSpaces(b.toByteArray()));
//        
//        final @Nonnull ByteArrayOutputStream a = new ByteArrayOutputStream();
//        new DataOutputStream(a).writeUTF("o");
//        System.out.println("o in bytes: " + Strings.hexWithSpaces(a.toByteArray()));
//        
//        final @Nonnull ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        new DataOutputStream(baos).writeUTF("World");
//        System.out.println("World in bytes: " + Strings.hexWithSpaces(baos.toByteArray()));
        
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final @Nonnull DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        new DataOutputStream(deflaterOutputStream).writeUTF("Hello");
        deflaterOutputStream.finish();
        new DataOutputStream(byteArrayOutputStream).writeUTF("World");
        
        final @Nonnull byte[] bytes = byteArrayOutputStream.toByteArray();
        System.out.println(Strings.hexWithSpaces(bytes));
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new LoggingByteArrayInputStream(bytes, true);
        final @Nonnull InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream, inflater, 15); // This test only works if the buffer size is 15.
        System.out.println("Reading 'Hello'");
        Assert.assertEquals("Hello", new DataInputStream(inflaterInputStream).readUTF());
        System.out.println("Reading 'World'");
        Assert.assertEquals("World", new DataInputStream(byteArrayInputStream).readUTF());
    }
    
}
