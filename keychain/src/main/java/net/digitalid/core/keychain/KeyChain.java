package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.order.StrictlyDescending;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.AsymmetricKey;

/**
 * A key chain contains several items to support the rotation of host keys.
 * 
 * @see PublicKeyChain
 * @see PrivateKeyChain
 */
@Immutable
public abstract class KeyChain<K extends AsymmetricKey> {
    
    /* -------------------------------------------------- Items -------------------------------------------------- */
    
    /**
     * Returns the items of this key chain in chronological order with the newest one first.
     */
    @Pure
    @NonRepresentative // TODO: Remove this, as soon as lists can be properly converted.
    public abstract @Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull Pair<@Nonnull Time, @Nonnull K>> getItems();
    
    /* -------------------------------------------------- Times -------------------------------------------------- */
    
    /**
     * Returns the newest time of this key chain.
     */
    @Pure
    public @Nonnull Time getNewestTime() {
        return getItems().getFirst().get0();
    }
    
    /**
     * Returns the oldest time of this key chain.
     */
    @Pure
    public @Nonnull Time getOldestTime() {
        return getItems().getLast().get0();
    }
    
    /* -------------------------------------------------- Retrieval -------------------------------------------------- */
    
    /**
     * Returns the key in use at the given time.
     * 
     * @require time.isGreaterThanOrEqualTo(getOldestTime()) : "There is no key for the given time in this key chain.";
     */
    @Pure
    public @Nonnull K getKey(@Nonnull Time time) {
        Require.that(time.isGreaterThanOrEqualTo(getOldestTime())).orThrow("There is no key for the time $ in the key chain $.", time, this);
        
        for (@Nonnull Pair<@Nonnull Time, @Nonnull K> item : getItems()) {
            if (time.isGreaterThanOrEqualTo(item.get0())) { return item.get1(); }
        }
        throw UnexpectedValueException.with("time", time);
    }
    
    /* -------------------------------------------------- Modification -------------------------------------------------- */
    
    /**
     * Adds a new key to this key chain by returning a new instance.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @return a new key chain with the given key added and expired ones removed.
     * 
     * @require time.isGreaterThan(getNewestTime()) : "The time is greater than the newest time of this key chain.";
     * @require time.isGreaterThan(Time.TROPICAL_YEAR.ahead()) : "The time lies at least one year in the future.";
     */
    @Pure
    public @Nonnull KeyChain<K> add(@Nonnull Time time, @Nonnull K key) {
        Require.that(time.isGreaterThan(getNewestTime())).orThrow("The time is greater than the newest time of this key chain.");
        Require.that(time.isGreaterThan(Time.TROPICAL_YEAR.ahead())).orThrow("The time lies at least one year in the future.");
        
        final @Nonnull FreezableList<@Nonnull Pair<@Nonnull Time, @Nonnull K>> items = FreezableLinkedList.withNoElements();
        items.add(0, Pair.of(time, key));
        
        final @Nonnull Time cutoff = Time.TWO_YEARS.ago();
        for (@Nonnull Pair<@Nonnull Time, @Nonnull K> item : getItems()) {
            items.add(item);
            if (item.get0().isLessThan(cutoff)) { break; }
        }
        
        return createKeyChain(items.freeze());
    }
    
    @Pure
    protected abstract @Nonnull KeyChain<K> createKeyChain(@Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull Pair<@Nonnull Time, @Nonnull K>> items);
    
}
