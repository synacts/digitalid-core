package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class PrivateKeyChain extends KeyChain<PrivateKey, PrivateKeyChain> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code item.private.key.chain.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.map("item.private.key.chain.host@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, PrivateKey.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.chain.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("private.key.chain.host@core.digitalid.net").load(ListWrapper.TYPE, ITEM);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private PrivateKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        super(items);
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
    public static @Nonnull PrivateKeyChain get(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        return new PrivateKeyChain(items);
    }
    
    /**
     * Creates a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @return a new key chain with the given time and key.
     * 
     * @require time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PrivateKeyChain get(@Nonnull Time time, @Nonnull PrivateKey key) {
        assert time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
        
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, PrivateKey>> items = FreezableLinkedList.get();
        items.add(FreezablePair.get(time, key).freeze());
        return new PrivateKeyChain(items.freeze());
    }
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends KeyChain.Factory<PrivateKey, PrivateKeyChain> {
        
        /**
         * Creates a new factory.
         */
        protected Factory() {
            super(TYPE, ITEM, PrivateKey.FACTORY);
        }
        
        @Pure
        @Override
        protected @Nonnull PrivateKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
            return new PrivateKeyChain(items);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
