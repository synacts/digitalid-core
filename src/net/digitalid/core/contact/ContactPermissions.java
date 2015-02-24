package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.wrappers.Block;

/**
 * This class models the permissions of contacts as a set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class ContactPermissions extends AttributeTypeSet implements ReadonlyContactPermissions, Blockable {
    
    /**
     * Stores the semantic type {@code permission.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("permission.contact@core.digitalid.net").load(AttributeTypeSet.TYPE);
    
    
    /**
     * Stores an empty set of contact permissions.
     */
    public static final @Nonnull ReadonlyContactPermissions NONE = new ContactPermissions().freeze();
    
    
    /**
     * Creates an empty set of contact permissions.
     */
    public ContactPermissions() {}
    
    /**
     * Creates new contact permissions with the given attribute type.
     * 
     * @param type the attribute type used for contact permission.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure isSingle() : "The new contact permissions are single.";
     */
    public ContactPermissions(@Nonnull SemanticType type) {
        super(type);
    }
    
    /**
     * Creates new contact permissions from the given contact permissions.
     * 
     * @param permissions the contact permissions to add to the new contact permissions.
     */
    public ContactPermissions(@Nonnull ReadonlyContactPermissions permissions) {
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
    public ContactPermissions(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Override
    public @Nonnull ReadonlyContactPermissions freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull ContactPermissions clone() {
        return new ContactPermissions(this);
    }
    
}
