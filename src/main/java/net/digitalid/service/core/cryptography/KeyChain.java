package net.digitalid.service.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableIterator;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.converter.SQL;

/**
 * A key chain contains several items to support the rotation of host keys.
 * 
 * @see PublicKeyChain
 * @see PrivateKeyChain
 */
@Immutable
abstract class KeyChain<K extends XDF<K, Object>, C extends KeyChain<K, C>> implements XDF<C, Object>, SQL<C, Object> {
    
    /* -------------------------------------------------- Items -------------------------------------------------- */
    
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
        assert time.isGreaterThan(Time.getCurrent().add(Time.TROPICAL_YEAR)) : "The time lies at least one year in the future.";
        
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
        return getXDFConverter().createKeyChain(copy.freeze());
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return items.toString();
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static abstract class XDFConverter<K extends XDF<K, Object>, C extends KeyChain<K, C>> extends AbstractNonRequestingXDFConverter<C, Object> {
        
        /**
         * Stores the type of the key chain items.
         */
        private final @Nonnull SemanticType itemType;
        
        /**
         * Stores the factory that retrieves a key from a block.
         */
        private final @Nonnull AbstractNonRequestingXDFConverter<K, Object> factory;
        
        /**
         * Creates a new XDF converter with the given parameters.
         * 
         * @param chainType the type of the key chain.
         * @param itemType the type of the key chain items.
         * @param factory the factory that retrieves a key from a block.
         */
        protected XDFConverter(@Nonnull SemanticType chainType, @Nonnull SemanticType itemType, @Nonnull AbstractNonRequestingXDFConverter<K, Object> factory) {
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
                pair.set(0, ConvertToXDF.nonNullable(item.getNonNullableElement0()));
                pair.set(1, ConvertToXDF.nonNullable(item.getNonNullableElement1()));
                elements.add(TupleWrapper.encode(itemType, pair.freeze()));
            }
            return ListWrapper.encode(getType(), elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull C decodeNonNullable(@Nonnull Object none, @Nonnull Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
            
            final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
            if (elements.isEmpty()) throw new InvalidEncodingException("The list of elements may not be empty.");
            final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, K>> items = FreezableLinkedList.get();
            
            for (final @Nonnull Block element : elements) {
                final @Nonnull ReadOnlyArray<Block> pair = TupleWrapper.decode(element).getNonNullableElements(2);
                final @Nonnull Time time = Time.XDF_CONVERTER.decodeNonNullable(none, pair.getNonNullable(0));
                final @Nonnull K key = factory.decodeNonNullable(none, pair.getNonNullable(1));
                items.add(FreezablePair.get(time, key).freeze());
            }
            
            if (!items.isStrictlyDescending()) throw new InvalidEncodingException("The time has to be strictly decreasing.");
            return createKeyChain(items.freeze());
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull XDFConverter<K, C> getXDFConverter();
    
}
