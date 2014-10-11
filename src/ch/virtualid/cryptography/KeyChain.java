package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableIterator;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;
import org.javatuples.Pair;

/**
 * A key chain contains several items to support the rotation of host keys.
 * 
 * @see PublicKeyChain
 * @see PrivateKeyChain
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
abstract class KeyChain<Key extends Blockable> implements Immutable, Blockable {
    
    /**
     * Stores the items of this key chain in chronological order with the newest one first.
     * 
     * @invariant !items.isEmpty() : "The list is not empty.";
     * @invariant items.isFrozen() : "The list is frozen.";
     * @invariant items.doesNotContainNull() : "The list does not contain null.";
     * @invariant items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private final @Nonnull ReadonlyList<Pair<Time, Key>> items;
    
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
        
        final @Nonnull FreezableLinkedList<Pair<Time, Key>> items = new FreezableLinkedList<Pair<Time, Key>>();
        items.add(new Pair<Time, Key>(time, key));
        this.items = items.freeze();
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require !items.isEmpty() : "The list is not empty.";
     * @require items.isFrozen() : "The list is frozen.";
     * @require items.doesNotContainNull() : "The list does not contain null.";
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    protected KeyChain(@Nonnull ReadonlyList<Pair<Time, Key>> items) {
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
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        if (elements.isEmpty()) throw new InvalidEncodingException("The list of elements may not be empty.");
        final @Nonnull FreezableLinkedList<Pair<Time, Key>> items = new FreezableLinkedList<Pair<Time, Key>>();
        
        for (final @Nonnull Block element : elements) {
            final @Nonnull ReadonlyArray<Block> pair = new TupleWrapper(element).getElementsNotNull(2);
            final @Nonnull Time time = new Time(pair.getNotNull(0));
            final @Nonnull Key key = createKey(pair.getNotNull(1));
            items.add(new Pair<Time, Key>(time, key));
        }
        
        if (!items.isStrictlyDescending()) throw new InvalidEncodingException("The time has to be strictly decreasing.");
        this.items = items.freeze();
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArrayList<Block> elements = new FreezableArrayList<Block>(items.size());
        for (final @Nonnull Pair<Time, Key> item : items) {
            final @Nonnull FreezableArray<Block> pair = new FreezableArray<Block>(2);
            pair.set(0, item.getValue0().toBlock());
            pair.set(1, item.getValue1().toBlock());
            elements.add(new TupleWrapper(getItemType(), pair.freeze()).toBlock());
        }
        return new ListWrapper(getType(), elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the items of this key chain in chronological order with the newest one first.
     * 
     * @return the items of this key chain in chronological order with the newest one first.
     * 
     * @ensure !items.isEmpty() : "The list is not empty.";
     * @ensure items.isFrozen() : "The list is frozen.";
     * @ensure items.doesNotContainNull() : "The list does not contain null.";
     * @ensure items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    public final @Nonnull ReadonlyList<Pair<Time, Key>> getItems() {
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
        for (final @Nonnull Pair<Time, Key> item : items) {
            if (time.isGreaterThanOrEqualTo(item.getValue0())) return item.getValue1();
        }
        throw new InvalidEncodingException("There is no key for the given time.");
    }
    
    /**
     * Returns the newest time of this key chain.
     * 
     * @return the newest time of this key chain.
     */
    @Pure
    public final @Nonnull Time getNewestTime() {
        return items.getNotNull(0).getValue0();
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
        
        final @Nonnull FreezableList<Pair<Time, Key>> copy = items.clone();
        final @Nonnull Pair<Time, Key> pair = new Pair<Time, Key>(time, key);
        copy.add(0, pair);
        
        final @Nonnull Time cutoff = Time.TWO_YEARS.ago();
        final @Nonnull FreezableIterator<Pair<Time, Key>> iterator = copy.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue0().isLessThan(cutoff)) {
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                break;
            }
        }
        return createKeyChain(copy.freeze());
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
     * @require !items.isEmpty() : "The list is not empty.";
     * @require items.isFrozen() : "The list is frozen.";
     * @require items.doesNotContainNull() : "The list does not contain null.";
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    protected abstract @Nonnull KeyChain<Key> createKeyChain(@Nonnull ReadonlyList<Pair<Time, Key>> items);
    
}
