package ch.virtualid.agent;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.HashWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the randomized {@link AgentPermissions permissions} of {@link OutgoingRole outgoing roles}.
 * 
 * @invariant (salt == null) == (permissions == null) : "The salt and the permissions are either both null or both non-null.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class RandomizedAgentPermissions implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code salt.randomized.permission.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SALT = SemanticType.create("salt.randomized.permission.agent@virtualid.ch").load(HashWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code permissions.randomized.permission.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType PERMISSIONS = SemanticType.create("permissions.randomized.permission.agent@virtualid.ch").load(AgentPermissions.TYPE);
    
    /**
     * Stores the semantic type {@code randomized.permission.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("randomized.permission.agent@virtualid.ch").load(TupleWrapper.TYPE, SALT, PERMISSIONS);
    
    /**
     * Stores the semantic type {@code hash.randomized.permission.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType HASH = SemanticType.create("hash.randomized.permission.agent@virtualid.ch").load(HashWrapper.TYPE);
    
    
    /**
     * Stores the hash of these randomized permissions.
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
    private final @Nullable ReadonlyAgentPermissions permissions;
    
    /**
     * Creates new randomized permissions with the given permissions.
     * 
     * @param permissions the permissions to randomize.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    public RandomizedAgentPermissions(@Nonnull ReadonlyAgentPermissions permissions) {
        assert permissions.isFrozen() : "The permissions are frozen.";
        
        this.permissions = permissions;
        this.salt = new BigInteger(Parameters.HASH, new SecureRandom());
        this.hash = toBlock().getHash();
    }
    
    /**
     * Creates new randomized permissions with the given hash.
     * 
     * @param hash the hash of the randomized permissions.
     */
    public RandomizedAgentPermissions(@Nonnull BigInteger hash) {
        this.hash = hash;
        this.salt = null;
        this.permissions = null;
    }
    
    /**
     * Creates new randomized permissions from the given block.
     * <p>
     * <em>Important:</em> Please note that {@link #toBlock()}
     * returns an invalid block if the permissions are hidden.
     * 
     * @param block the block containing the randomized permissions.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public RandomizedAgentPermissions(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.hash = block.getHash();
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.salt = new HashWrapper(elements.getNotNull(0)).getValue();
        this.permissions = new AgentPermissions(elements.getNotNull(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, salt == null ? null : new HashWrapper(SALT, salt).toBlock());
        elements.set(1, Block.toBlock(PERMISSIONS, permissions));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
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
    public @Nullable ReadonlyAgentPermissions getPermissions() {
        return permissions;
    }
    
    /**
     * Returns the actual permissions.
     * 
     * @return the actual permissions.
     * 
     * @require areShown() : "The permissions are exposed.";
     * 
     * @ensure permissions.isFrozen() : "The permissions are frozen.";
     */
    @Pure
    public @Nonnull ReadonlyAgentPermissions getPermissionsNotNull() {
        assert permissions != null : "The permissions are exposed.";
        
        return permissions;
    }
    
    /**
     * Returns whether the permissions are shown.
     * 
     * @return whether the permissions are shown.
     */
    @Pure
    public boolean areShown() {
        return permissions != null;
    }
    
    /**
     * Returns whether the permissions are hidden.
     * 
     * @return whether the permissions are hidden.
     */
    @Pure
    public boolean areHidden() {
        return permissions == null;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof RandomizedAgentPermissions)) return false;
        final @Nonnull RandomizedAgentPermissions other = (RandomizedAgentPermissions) object;
        return this.hash.equals(other.hash) && Objects.equals(this.salt, other.salt) && Objects.equals(this.permissions, other.permissions);
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
