package net.digitalid.core.keychain;

import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PublicKey;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PublicKeyChainItem extends KeyChainItem<PublicKey> {
    
}
