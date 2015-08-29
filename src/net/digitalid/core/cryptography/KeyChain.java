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
import net.digitalid.core.storable.BlockBasedSimpleNonConceptFactory;
import net.digitalid.core.storable.SimpleNonConceptFactory;
import net.digitalid.core.storable.Storable;
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
abstract class KeyChain<K extends Storable<K>, C extends KeyChain<K, C>> implements Storable<C> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Items –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the items of this key chain in chronological order with the newest one first.
     * 
     * @invariant items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private final @Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, K>> items;
    
    /**
     * Returns the items of this key chain in chronological order with the newest one first.
     * 
     * @return the items of this key chain in chronological order with the newest one first.
     * 
     * @ensure items.isStrictlyDescending() : "The list is strictly descending.";
     */
    @Pure
    public final @Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, K>> getItems() {
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
    public final @Nonnull K getKey(@Nonnull Time time) throws InvalidEncodingException {
        for (final @Nonnull ReadOnlyPair<Time, K> item : items) {
            if (time.isGreaterThanOrEqualTo(item.getNonNullableElement0())) return item.getNonNullableElement1();
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
        return items.getNonNullable(0).getNonNullableElement0();
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
    public final @Nonnull KeyChain<K, C> add(@Nonnull Time time, @Nonnull K key) {
        assert time.isGreaterThan(getNewestTime()) : "The time is greater than the newest time of this key chain.";
        assert time.isGreaterThan(new Time().add(Time.TROPICAL_YEAR)) : "The time lies at least one year in the future.";
        
        final @Nonnull FreezableList<ReadOnlyPair<Time, K>> copy = items.clone();
        final @Nonnull ReadOnlyPair<Time, K> pair = FreezablePair.get(time, key).freeze();
        copy.add(0, pair);
        
        final @Nonnull Time cutoff = Time.TWO_YEARS.ago();
        final @Nonnull FreezableIterator<ReadOnlyPair<Time, K>> iterator = copy.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getNonNullableElement0().isLessThan(cutoff)) {
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                break;
            }
        }
        return getFactory().createKeyChain(copy.freeze());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    protected KeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, K>> items) {
        this.items = items;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return items.toString();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static abstract class Factory<K extends Storable<K>, C extends KeyChain<K, C>> extends BlockBasedSimpleNonConceptFactory<C> {
        
        /**
         * Stores the type of the key chain items.
         */
        private final @Nonnull SemanticType itemType;
        
        /**
         * Stores the factory that retrieves a key from a block.
         */
        private final @Nonnull SimpleNonConceptFactory<K> factory;
        
        /**
         * Creates a new factory with the given parameters.
         * 
         * @param chainType the type of the key chain.
         * @param itemType the type of the key chain items.
         * @param factory the factory that retrieves a key from a block.
         */
        protected Factory(@Nonnull SemanticType chainType, @Nonnull SemanticType itemType, @Nonnull SimpleNonConceptFactory<K> factory) {
            super(chainType);
            
            this.itemType = itemType;
            this.factory = factory;
        }
        
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
        protected abstract @Nonnull C createKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, K>> items);
    
        @Pure
        @Override
        public final @Nonnull Block encodeNonNullable(@Nonnull C chain) {
            final @Nonnull ReadOnlyList<ReadOnlyPair<Time, K>> items = chain.getItems();
            final @Nonnull FreezableArrayList<Block> elements = FreezableArrayList.getWithCapacity(items.size());
            for (final @Nonnull ReadOnlyPair<Time, K> item : items) {
                final @Nonnull FreezableArray<Block> pair = FreezableArray.get(2);
                pair.set(0, Block.fromNonNullable(item.getNonNullableElement0()));
                pair.set(1, Block.fromNonNullable(item.getNonNullableElement1()));
                elements.add(TupleWrapper.encode(itemType, pair.freeze()));
            }
            return ListWrapper.encode(getType(), elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull C decodeNonNullable(@Nonnull Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
            
            final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
            if (elements.isEmpty()) throw new InvalidEncodingException("The list of elements may not be empty.");
            final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, K>> items = FreezableLinkedList.get();
            
            for (final @Nonnull Block element : elements) {
                final @Nonnull ReadOnlyArray<Block> pair = TupleWrapper.decode(element).getNonNullableElements(2);
                final @Nonnull Time time = new Time(pair.getNonNullable(0));
                final @Nonnull K key = factory.decodeNonNullable(pair.getNonNullable(1));
                items.add(FreezablePair.get(time, key).freeze());
            }
            
            if (!items.isStrictlyDescending()) throw new InvalidEncodingException("The time has to be strictly decreasing.");
            return createKeyChain(items.freeze());
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull Factory<K, C> getFactory();
    
}
