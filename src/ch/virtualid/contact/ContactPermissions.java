package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models the permissions of contacts as a set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ContactPermissions extends AttributeSet implements ReadonlyContactPermissions, Blockable {
    
    /**
     * Stores the semantic type {@code permission.contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("permission.contact@virtualid.ch").load(ListWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER);
    
    
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
     * @ensure areSingle() : "The new contact permissions are single.";
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
    public ContactPermissions(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
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
