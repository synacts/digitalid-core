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
package net.digitalid.core.host.key;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PublicKeyChain;

/**
 * This class creates and stores a key pair for a host.
 */
@Utility
public abstract class KeyPairGenerator {
    
    /**
     * Generates a key pair for the host with the given identifier.
     */
    @PureWithSideEffects
    public static void generateKeyPairFor(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        Log.information("Generating a key pair for the host $.", identifier);
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull PrivateKeyChain privateKeyChain = PrivateKeyChain.with(time, keyPair.getPrivateKey());
        final @Nonnull PublicKeyChain publicKeyChain = PublicKeyChain.with(time, keyPair.getPublicKey());
        PrivateKeyChainLoader.store(identifier, privateKeyChain);
        PublicKeyChainLoader.store(identifier, publicKeyChain);
    }
    
}
