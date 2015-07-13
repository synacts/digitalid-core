package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.annotations.AttributeType;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableLinkedHashSet;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;

/**
 * This class models a freezable set of attribute types.
 * 
 * @see Authentications
 * @see ContactPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableAttributeTypeSet extends FreezableLinkedHashSet<SemanticType> implements ReadOnlyAttributeTypeSet {
    
    /**
     * Stores the semantic type {@code list.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("list.attribute.type@core.digitalid.net").load(ListWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER);
    
    
    /**
     * Creates an empty set of attribute types.
     */
    public FreezableAttributeTypeSet() {}
    
    /**
     * Creates a new attribute type set with the given attribute type.
     * 
     * @param type the attribute type to add to the new set.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure isSingle() : "The new attribute type set contains a single element.";
     */
    public FreezableAttributeTypeSet(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        add(type);
    }
    
    /**
     * Creates a new attribute type set from the given attribute type set.
     * 
     * @param attributeSet the attribute type set to add to the new attribute type set.
     */
    public FreezableAttributeTypeSet(@Nonnull ReadOnlyAttributeTypeSet attributeSet) {
        addAll(attributeSet);
    }
    
    /**
     * Creates a new attribute type set from the given block.
     * 
     * @param block the block containing the attribute type set.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public FreezableAttributeTypeSet(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull SemanticType type = IdentityClass.create(element).toSemanticType().checkIsAttributeType();
            add(type);
        }
    }
    
    /**
     * @ensure return.isBasedOn(TYPE) : "The returned type is based on the indicated type.";
     */
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(size());
        for (final @Nonnull SemanticType type : this) {
            elements.add(type.toBlock(SemanticType.ATTRIBUTE_IDENTIFIER));
        }
        return new ListWrapper(getType(), elements.freeze()).toBlock();
    }
    
    
    @Override
    public @Nonnull ReadOnlyAttributeTypeSet freeze() {
        super.freeze();
        return this;
    }
    
    
    @Override
    public final boolean add(@Nonnull @AttributeType SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        return super.add(type);
    }
    
    /**
     * Adds the given attribute type set to this attribute type set.
     * 
     * @param attributeSet the attribute type set to add to this attribute type set.
     * 
     * @require !isFrozen() : "This object is not frozen.";
     */
    public final void addAll(@Nonnull ReadOnlyAttributeTypeSet attributeSet) {
        for (final @Nonnull SemanticType type : attributeSet) {
            add(type);
        }
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableAttributeTypeSet clone() {
        return new FreezableAttributeTypeSet(this);
    }
    
    
    @Pure
    @Override
    public final @Capturable @Nonnull FreezableAgentPermissions toAgentPermissions() {
        final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
        for (final @Nonnull SemanticType type : this) {
            permissions.put(type, false);
        }
        return permissions;
    }
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("{");
        for (final @Nonnull SemanticType type : this) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString());
        }
        string.append("}");
        return string.toString();
    }
    
}
