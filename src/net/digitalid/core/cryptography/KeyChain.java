package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableIterator;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Blockable;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * A key chain contains several items to support the rotation of host keys.
 * 
 * @see PublicKeyChain
 * @see PrivateKeyChain
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
abstract class KeyChain<Key extends Blockable> implements Blockable {
    
    /**
     * Stores the items of this key chain in chronological order with the newest one first.
     * 
     * @invariant items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private final @Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, Key>> items;
    
    /**
     * Creates a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @require time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
     */
    protected KeyChain(@Nonnull Time time, @Nonnull Key key) {
        assert time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
        
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, Key>> items = new FreezableLinkedList<>();
        items.add(new FreezablePair<>(time, key).freeze());
        this.items = items.freeze();
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    protected KeyChain(@Nonnull@Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, Key>> items) {
        this.items = items;
    }
    
    /**
     * Creates a new key chain with the entries encoded in the given block.
     * 
     * @param block the block containing the key chain entries.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    protected KeyChain(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        if (elements.isEmpty()) throw new InvalidEncodingException("The list of elements may not be empty.");
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, Key>> items = new FreezableLinkedList<>();
        
        for (final @Nonnull Block element : elements) {
            final @Nonnull ReadOnlyArray<Block> pair = new TupleWrapper(element).getElementsNotNull(2);
            final @Nonnull Time time = new Time(pair.getNonNullable(0));
            final @Nonnull Key key = createKey(pair.getNonNullable(1));
            items.add(new FreezablePair<>(time, key).freeze());
        }
        
        if (!items.isStrictlyDescending()) throw new InvalidEncodingException("The time has to be strictly decreasing.");
        this.items = items.freeze();
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArrayList<Block> elements = new FreezableArrayList<>(items.size());
        for (final @Nonnull ReadOnlyPair<Time, Key> item : items) {
            final @Nonnull FreezableArray<Block> pair = new FreezableArray<>(2);
            pair.set(0, item.getElement0().toBlock());
            pair.set(1, item.getElement1().toBlock());
            elements.add(new TupleWrapper(getItemType(), pair.freeze()).toBlock());
        }
        return new ListWrapper(getType(), elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the items of this key chain in chronological order with the newest one first.
     * 
     * @return the items of this key chain in chronological order with the newest one first.
     * 
     * @ensure items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    public final @Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, Key>> getItems() {
        return items;
    }
    
    /**
     * Returns the key in use at the given time.
     * 
     * @param time the time for which the key returned.
     * 
     * @return the key in use at the given time.
     * 
     * @throws InvalidEncodingException if there is no key for the given time.
     */
    @Pure
    public final @Nonnull Key getKey(@Nonnull Time time) throws InvalidEncodingException {
        for (final @Nonnull ReadOnlyPair<Time, Key> item : items) {
            if (time.isGreaterThanOrEqualTo(item.getElement0())) return item.getElement1();
        }
        throw new InvalidEncodingException("There is no key for the given time (" + time + ") in this key chain " + this + ".");
    }
    
    /**
     * Returns the newest time of this key chain.
     * 
     * @return the newest time of this key chain.
     */
    @Pure
    public final @Nonnull Time getNewestTime() {
        return items.getNotNull(0).getElement0();
    }
    
    /**
     * Adds a new key to this key chain by returning a new instance.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @return a new key chain with the given key added and expired ones removed.
     */
    @Pure
    public final @Nonnull KeyChain<Key> add(@Nonnull Time time, @Nonnull Key key) {
        assert time.isGreaterThan(getNewestTime()) : "The time is greater than the newest time of this key chain.";
        assert time.isGreaterThan(new Time().add(Time.TROPICAL_YEAR)) : "The time lies at least one year in the future.";
        
        final @Nonnull FreezableList<ReadOnlyPair<Time, Key>> copy = items.clone();
        final @Nonnull ReadOnlyPair<Time, Key> pair = new FreezablePair<>(time, key).freeze();
        copy.add(0, pair);
        
        final @Nonnull Time cutoff = Time.TWO_YEARS.ago();
        final @Nonnull FreezableIterator<ReadOnlyPair<Time, Key>> iterator = copy.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getElement0().isLessThan(cutoff)) {
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                break;
            }
        }
        return createKeyChain(copy.freeze());
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return items.toString();
    }
    
    
    /**
     * Returns the type of the key chain items.
     * 
     * @return the type of the key chain items.
     */
    @Pure
    protected abstract @Nonnull SemanticType getItemType();
    
    /**
     * Creates a new key from the given block.
     * 
     * @param block the block containing the key.
     * 
     * @return a new key created from the given block.
     */
    @Pure
    protected abstract @Nonnull Key createKey(@Nonnull Block block) throws InvalidEncodingException;
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @return a new key chain with the given items.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    protected abstract @Nonnull KeyChain<Key> createKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, Key>> items);
    
}
