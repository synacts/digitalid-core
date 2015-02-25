package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.tuples.ReadonlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models a {@link KeyChain key chain} of {@link PublicKey public keys}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class PublicKeyChain extends KeyChain<PublicKey> implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code item.public.key.chain.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.create("item.public.key.chain.host@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, PublicKey.TYPE);
    
    /**
     * Stores the semantic type {@code public.key.chain.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("public.key.chain.host@core.digitalid.net").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, ListWrapper.TYPE, ITEM);
    
    
    /**
     * Creates a new key chain with the given time and public key.
     * 
     * @param time the time from when on the given public key is valid.
     * @param publicKey the public key that is valid from the given time on.
     * 
     * @require time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
     */
    public PublicKeyChain(@Nonnull Time time, @Nonnull PublicKey publicKey) {
        super(time, publicKey);
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    public PublicKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadonlyList<ReadonlyPair<Time, PublicKey>> items) {
        super(items);
    }
    
    /**
     * Creates a new key chain with the entries encoded in the given block.
     * 
     * @param block the block containing the key chain entries.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PublicKeyChain(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Pure
    @Override
    protected @Nonnull SemanticType getItemType() {
        return ITEM;
    }
    
    @Pure
    @Override
    protected @Nonnull PublicKey createKey(@Nonnull Block block) throws InvalidEncodingException {
        return new PublicKey(block);
    }
    
    @Pure
    @Override
    protected @Nonnull KeyChain<PublicKey> createKeyChain(@Nonnull ReadonlyList<ReadonlyPair<Time, PublicKey>> items) {
        return new PublicKeyChain(items);
    }
    
}
