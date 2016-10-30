package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;

import net.digitalid.core.asymmetrickey.PublicKey;

import net.digitalid.utility.cryptography.key.chain.PublicKeyChainSubclass;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.order.StrictlyDescending;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a {@link KeyChain key chain} of {@link PublicKey public keys}.
 */
@Immutable
@GenerateSubclass
public abstract class PublicKeyChain extends KeyChain<PublicKey> {
    
    /**
     * Returns a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @require time.isInPast() : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PublicKeyChain with(@Nonnull Time time, @Nonnull PublicKey key) {
        Require.that(time.isInPast()).orThrow("The time lies in the past.");
        
        return new PublicKeyChainSubclass(FreezableLinkedList.<Pair<Time, PublicKey>>withElement(Pair.of(time, key)).freeze());
    }
    
    @Pure
    @Override
    protected @Nonnull PublicKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull Pair<@Nonnull Time, @Nonnull PublicKey>> items) {
        return new PublicKeyChainSubclass(items);
    }
    
}
