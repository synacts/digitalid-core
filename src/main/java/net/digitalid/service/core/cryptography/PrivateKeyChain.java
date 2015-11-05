package net.digitalid.service.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.sql.XDFBasedSQLConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
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
     * @require time.isLessThanOrEqualTo(Time.getCurrent()) : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PrivateKeyChain get(@Nonnull Time time, @Nonnull PrivateKey key) {
        assert time.isLessThanOrEqualTo(Time.getCurrent()) : "The time lies in the past.";
        
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, PrivateKey>> items = FreezableLinkedList.get();
        items.add(FreezablePair.get(time, key).freeze());
        return new PrivateKeyChain(items.freeze());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends KeyChain.EncodingFactory<PrivateKey, PrivateKeyChain> {
        
        /**
         * Creates a new encoding factory based on the encoding factory of the key.
         */
        protected EncodingFactory() {
            super(TYPE, ITEM, PrivateKey.ENCODING_FACTORY);
        }
        
        @Pure
        @Override
        protected @Nonnull PrivateKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
            return new PrivateKeyChain(items);
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull AbstractSQLConverter<PrivateKeyChain, Object> STORING_FACTORY = XDFBasedSQLConverter.get(ENCODING_FACTORY);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<PrivateKeyChain, Object> getSQLConverter() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Converters<PrivateKeyChain, Object> FACTORIES = Converters.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
