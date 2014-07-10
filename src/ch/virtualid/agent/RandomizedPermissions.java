package ch.virtualid.agent;

import ch.virtualid.annotation.Pure;
import ch.virtualid.client.Commitment;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.HashWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the randomized permissions of clients.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class RandomizedPermissions implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code time@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("time@virtualid.ch").load(Int64Wrapper.TYPE);
    
    
    /**
     * Stores the hash of the randomized permissions.
     */
    private final @Nonnull BigInteger hash;
    
    /**
     * Stores a random value that is used as an additional input to the hash function.
     */
    private final @Nullable BigInteger salt;
    
    /**
     * Stores the actual permissions.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     */
    private final @Nullable ReadonlyPermissions permissions;
    
    /**
     * Creates new randomized permissions with the given permissions.
     * 
     * @param permissions the permissions to randomize.
     * 
     * @require permissions.isFrozen() : "The permissions have to be frozen.";
     */
    public RandomizedPermissions(@Nonnull ReadonlyPermissions permissions) {
        assert permissions.isFrozen() : "The permissions have to be frozen.";
        
        this.permissions = permissions;
        this.salt = new BigInteger(Parameters.HASH, new SecureRandom());
        this.hash = toBlock().getHash();
    }
    
    /**
     * Creates new randomized permissions with the given hash.
     * 
     * @param hash the hash of the randomized permissions.
     */
    public RandomizedPermissions(@Nonnull BigInteger hash) {
        this.hash = hash;
        this.salt = null;
        this.permissions = null;
    }
    
    /**
     * Creates new randomized permissions from the given block.
     * 
     * @param block the block containing the randomized permissions.
     */
    public RandomizedPermissions(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(block);
        
        this.hash = block.getHash();
        @Nonnull ReadonlyArray<Block> tuple = new TupleWrapper(block).getElementsNotNull(2);
        this.salt = new HashWrapper(tuple.getNotNull(0)).getValue();
        this.permissions = new Permissions(tuple.getNotNull(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        if (permissions == null) return Block.EMPTY;
        
        @Nonnull FreezableArray<Block> array = new FreezableArray<Block>(2);
        array.set(0, new HashWrapper(salt).toBlock());
        array.set(1, permissions.toBlock());
        return new TupleWrapper(array.freeze()).toBlock();
    }
    
    
    /**
     * Returns the hash of the randomized permissions.
     * 
     * @return the hash of the randomized permissions.
     */
    @Pure
    public @Nonnull BigInteger getHash() {
        return hash;
    }
    
    /**
     * Returns the actual permissions.
     * 
     * @return the actual permissions.
     * 
     * @ensure permissions.isFrozen() : "The permissions are frozen.";
     */
    @Pure
    public @Nullable ReadonlyPermissions getPermissions() {
        return permissions;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == null || !(object instanceof Commitment)) return false;
        @Nonnull RandomizedPermissions other = (RandomizedPermissions) object;
        return hash.equals(other.hash) && Objects.equals(salt, other.salt) && Objects.equals(permissions, other.permissions);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.hash);
        hash = 17 * hash + Objects.hashCode(this.salt);
        hash = 17 * hash + Objects.hashCode(this.permissions);
        return hash;
    }
    
}
