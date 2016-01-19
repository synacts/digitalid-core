package net.digitalid.core.agent;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;

import net.digitalid.service.core.block.Blockable;

import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.conversion.wrappers.value.binary.Binary256Wrapper;

import net.digitalid.service.core.cryptography.Parameters;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.SemanticType;

/**
 * This class models the randomized {@link FreezableAgentPermissions permissions} of {@link OutgoingRole outgoing roles}.
 * 
 * @invariant (salt == null) == (permissions == null) : "The salt and the permissions are either both null or both non-null.";
 */
@Immutable
public final class RandomizedAgentPermissions implements Blockable {
    
    /**
     * Stores the semantic type {@code salt.randomized.permission.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SALT = SemanticType.map("salt.randomized.permission.agent@core.digitalid.net").load(Binary256Wrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code permissions.randomized.permission.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType PERMISSIONS = SemanticType.map("permissions.randomized.permission.agent@core.digitalid.net").load(FreezableAgentPermissions.TYPE);
    
    /**
     * Stores the semantic type {@code randomized.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("randomized.permission.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SALT, PERMISSIONS);
    
    /**
     * Stores the semantic type {@code hash.randomized.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType HASH = SemanticType.map("hash.randomized.permission.agent@core.digitalid.net").load(Binary256Wrapper.XDF_TYPE);
    
    
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
    private final @Nullable ReadOnlyAgentPermissions permissions;
    
    /**
     * Creates new randomized permissions with the given permissions.
     * 
     * @param permissions the permissions to randomize.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    public RandomizedAgentPermissions(@Nonnull ReadOnlyAgentPermissions permissions) {
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
    public RandomizedAgentPermissions(@Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.hash = block.getHash();
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.salt = Binary256Wrapper.decodeNonNullable(elements.getNonNullable(0));
        this.permissions = new FreezableAgentPermissions(elements.getNonNullable(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
        elements.set(0, Binary256Wrapper.encodeNullable(SALT, salt));
        elements.set(1, Block.toBlock(PERMISSIONS, permissions));
        return TupleWrapper.encode(TYPE, elements.freeze());
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
    public @Nullable ReadOnlyAgentPermissions getPermissions() {
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
    public @Nonnull ReadOnlyAgentPermissions getPermissionsNotNull() {
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
        if (object == this) { return true; }
        if (object == null || !(object instanceof RandomizedAgentPermissions)) { return false; }
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
