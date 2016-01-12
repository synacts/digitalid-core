package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * This class models the permissions of contacts as a set of attribute types.
 */
public final class FreezableContactPermissions extends FreezableAttributeTypeSet implements ReadOnlyContactPermissions {
    
    /**
     * Stores the semantic type {@code permission.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("permission.contact@core.digitalid.net").load(FreezableAttributeTypeSet.TYPE);
    
    
    /**
     * Stores an empty set of contact permissions.
     */
    public static final @Nonnull ReadOnlyContactPermissions NONE = new FreezableContactPermissions().freeze();
    
    
    /**
     * Creates an empty set of contact permissions.
     */
    public FreezableContactPermissions() {}
    
    /**
     * Creates new contact permissions with the given attribute type.
     * 
     * @param type the attribute type used for contact permission.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure isSingle() : "The new contact permissions are single.";
     */
    public FreezableContactPermissions(@Nonnull SemanticType type) {
        super(type);
    }
    
    /**
     * Creates new contact permissions from the given contact permissions.
     * 
     * @param permissions the contact permissions to add to the new contact permissions.
     */
    public FreezableContactPermissions(@Nonnull ReadOnlyContactPermissions permissions) {
        super(permissions);
    }
    
    /**
     * Creates new contact permissions from the given block.
     * 
     * @param block the block containing the contact permissions.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public FreezableContactPermissions(@Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Override
    public @Nonnull @Frozen ReadOnlyContactPermissions freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContactPermissions clone() {
        return new FreezableContactPermissions(this);
    }
    
}
