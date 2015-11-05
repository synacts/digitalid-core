package net.digitalid.service.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.sql.XDFBasedSQLConverter;
import net.digitalid.service.core.identity.resolution.Category;
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
 * This class models a {@link KeyChain key chain} of {@link PublicKey public keys}.
 */
@Immutable
public final class PublicKeyChain extends KeyChain<PublicKey, PublicKeyChain> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code item.public.key.chain.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.map("item.public.key.chain.host@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, PublicKey.TYPE);
    
    /**
     * Stores the semantic type {@code public.key.chain.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("public.key.chain.host@core.digitalid.net").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, ListWrapper.TYPE, ITEM);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    private PublicKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PublicKey>> items) {
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
    public static @Nonnull PublicKeyChain get(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PublicKey>> items) {
        return new PublicKeyChain(items);
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
    public static @Nonnull PublicKeyChain get(@Nonnull Time time, @Nonnull PublicKey key) {
        assert time.isLessThanOrEqualTo(Time.getCurrent()) : "The time lies in the past.";
        
        final @Nonnull FreezableLinkedList<ReadOnlyPair<Time, PublicKey>> items = FreezableLinkedList.get();
        items.add(FreezablePair.get(time, key).freeze());
        return new PublicKeyChain(items.freeze());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends KeyChain.EncodingFactory<PublicKey, PublicKeyChain> {
        
        /**
         * Creates a new encoding factory based on the encoding factory of the key.
         */
        protected EncodingFactory() {
            super(TYPE, ITEM, PublicKey.ENCODING_FACTORY);
        }
        
        @Pure
        @Override
        protected @Nonnull PublicKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PublicKey>> items) {
            return new PublicKeyChain(items);
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getXDFConverter() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull AbstractSQLConverter<PublicKeyChain, Object> STORING_FACTORY = XDFBasedSQLConverter.get(ENCODING_FACTORY);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<PublicKeyChain, Object> getSQLConverter() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Converters<PublicKeyChain, Object> FACTORIES = Converters.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
