package net.digitalid.core.keychain;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.PrivateKey;

/**
 * This class models an item in the private key chain.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PrivateKeyChainItem extends KeyChainItem<PrivateKey> {}
