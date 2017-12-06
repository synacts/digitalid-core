/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.signature.digest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnull;

import net.digitalid.core.parameters.Parameters;

import org.junit.Assert;
import org.junit.Test;

public class DigestTest {
    
    @Test
    public void shouldProduceSameDigest() throws Exception {
        final @Nonnull Random random = new Random();
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
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
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
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
