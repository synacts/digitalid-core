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
