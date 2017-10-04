package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.order.StrictlyDescending;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.AsymmetricKey;
import net.digitalid.core.pack.Packable;

/**
 * A key chain contains several items to support the rotation of host keys.
 * 
 * @see PublicKeyChain
 * @see PrivateKeyChain
 */
@Immutable
public abstract class KeyChain<@Unspecifiable KEY extends AsymmetricKey, @Unspecifiable ITEM extends KeyChainItem<KEY>> implements Packable {
    
    /* -------------------------------------------------- Items -------------------------------------------------- */
    
    /**
     * Returns the items of this key chain in chronological order with the newest one first.
     */
    @Pure
    @TODO(task="Implement mechanism to freeze collections when they are recovered in the converter", date="2017-09-25", author = Author.STEPHANIE_STROKA)
    public abstract @Nonnull /*@Frozen*/ @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull ITEM> getItems();
    
    /* -------------------------------------------------- Times -------------------------------------------------- */
    
    /**
     * Returns the newest time of this key chain.
     */
    @Pure
    public @Nonnull Time getNewestTime() {
        return getItems().getFirst().getTime();
    }
    
    /**
     * Returns the oldest time of this key chain.
     */
    @Pure
    public @Nonnull Time getOldestTime() {
        return getItems().getLast().getTime();
    }
    
    /* -------------------------------------------------- Retrieval -------------------------------------------------- */
    
    /**
     * Returns the key in use at the given time.
     * 
     * @require time.isGreaterThanOrEqualTo(getOldestTime()) : "There is no key for the given time in this key chain.";
     */
    @Pure
    public @Nonnull KEY getKey(@Nonnull Time time) {
        Require.that(time.isGreaterThanOrEqualTo(getOldestTime())).orThrow("There is no key for the time $ in the key chain $.", time, this);
        
        for (@Nonnull ITEM item : getItems()) {
            if (time.isGreaterThanOrEqualTo(item.getTime())) { return item.getKey(); }
        }
        throw CaseExceptionBuilder.withVariable("time").withValue(time).build();
    }
    
    /* -------------------------------------------------- Modification -------------------------------------------------- */
    
    @Pure
    protected abstract @Nonnull ITEM buildItem(@Nonnull Time time, @Nonnull KEY key);
    
    @Pure
    protected abstract @Nonnull KeyChain<KEY, ITEM> createKeyChain(@Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull ITEM> items);
    
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
    public @Nonnull KeyChain<KEY, ITEM> add(@Nonnull Time time, @Nonnull KEY key) {
        Require.that(time.isGreaterThan(getNewestTime())).orThrow("The time is greater than the newest time of this key chain.");
        Require.that(time.isGreaterThan(Time.TROPICAL_YEAR.ahead())).orThrow("The time lies at least one year in the future.");
        
        final @Nonnull FreezableList<@Nonnull ITEM> items = FreezableLinkedList.withNoElements();
        items.add(0, buildItem(time, key));
        
        final @Nonnull Time cutoff = Time.TWO_YEARS.ago();
        for (@Nonnull ITEM item : getItems()) {
            items.add(item);
            if (item.getTime().isLessThan(cutoff)) { break; }
        }
        
        return createKeyChain(items.freeze());
    }
    
}
