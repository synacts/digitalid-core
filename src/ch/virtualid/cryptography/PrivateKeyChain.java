package ch.virtualid.cryptography;

import ch.virtualid.annotation.Pure;
import ch.virtualid.concepts.Time;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;
import org.javatuples.Pair;

/**
 * This class models a {@link KeyChain key chain} of {@link PrivateKey private keys}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PrivateKeyChain extends KeyChain<PrivateKey> implements Immutable {
    
    /**
     * Stores the semantic type {@code item.private.key.chain.host@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ITEM = SemanticType.create("item.private.key.chain.host@virtualid.ch").load(TupleWrapper.TYPE, Time.TYPE, PrivateKey.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.chain.host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("private.key.chain.host@virtualid.ch").load(ListWrapper.TYPE, ITEM);
    
    
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
     * @require !items.isEmpty() : "The list is not empty.";
     * @require items.isFrozen() : "The list is frozen.";
     * @require items.doesNotContainNull() : "The list does not contain null.";
     * @require items.isStrictlyDescending() : "The list is strictly descending.";
     */
    public PrivateKeyChain(@Nonnull ReadonlyList<Pair<Time, PrivateKey>> items) {
        super(items);
    }
    
    /**
     * Creates a new key chain with the entries encoded in the given block.
     * 
     * @param block the block containing the key chain entries.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
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
    protected @Nonnull KeyChain<PrivateKey> createKeyChain(@Nonnull ReadonlyList<Pair<Time, PrivateKey>> items) {
        return new PrivateKeyChain(items);
    }
    
}
