package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.interfaces.CustomComparable;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.AsymmetricKey;

/**
 * This class models an item in the key chain.
 */
@Immutable
public abstract class KeyChainItem<KEY extends AsymmetricKey> implements CustomComparable<KeyChainItem<KEY>> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the time from when on the key is valid.
     */
    @Pure
    public abstract @Nonnull Time getTime();
    
    /**
     * Returns the key.
     */
    @Pure
    public abstract @Nonnull KEY getKey();
    
    /* -------------------------------------------------- Comparable -------------------------------------------------- */
    
    @Pure
    @Override
    public int compareTo(@Nonnull KeyChainItem<KEY> keyChainItem) {
        return getTime().compareTo(keyChainItem.getTime());
    }
    
}
