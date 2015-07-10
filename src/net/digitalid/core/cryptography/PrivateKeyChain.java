package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class PrivateKeyChain extends KeyChain<PrivateKey> {
    
    /**
     * Stores the semantic type {@code item.private.key.chain.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.create("item.private.key.chain.host@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, PrivateKey.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.chain.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("private.key.chain.host@core.digitalid.net").load(ListWrapper.TYPE, ITEM);
    
    
    /**
     * Creates a new key chain with the given time and private key.
     * 
     * @param time the time from when on the given private key is valid.
     * @param privateKey the private key that is valid from the given time on.
     * 
     * @require time.isLessThanOrEqualTo(new Time()) : "The time lies in the past.";
     */
    public PrivateKeyChain(@Nonnull Time time, @Nonnull PrivateKey privateKey) {
        super(time, privateKey);
    }
    
    /**
     * Creates a new key chain with the given items.
     * 
     * @param items the items of the new key chain.
     * 
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    public PrivateKeyChain(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        super(items);
    }
    
    /**
     * Creates a new key chain with the entries encoded in the given block.
     * 
     * @param block the block containing the key chain entries.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PrivateKeyChain(@Nonnull Block block) throws InvalidEncodingException {
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
    protected @Nonnull PrivateKey createKey(@Nonnull Block block) throws InvalidEncodingException {
        return new PrivateKey(block);
    }
    
    @Pure
    @Override
    protected @Nonnull KeyChain<PrivateKey> createKeyChain(@Nonnull ReadOnlyList<ReadOnlyPair<Time, PrivateKey>> items) {
        return new PrivateKeyChain(items);
    }
    
}
