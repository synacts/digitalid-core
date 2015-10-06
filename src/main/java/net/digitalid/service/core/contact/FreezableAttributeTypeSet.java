package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.annotations.AttributeType;
import net.digitalid.annotations.reference.Capturable;
import net.digitalid.collections.annotations.freezable.Frozen;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.collections.annotations.freezable.NonFrozen;
import net.digitalid.collections.annotations.freezable.NonFrozenRecipient;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.annotations.Single;
import net.digitalid.core.collections.Brackets;
import net.digitalid.core.collections.ElementConverter;
import net.digitalid.collections.freezable.FreezableArrayList;
import net.digitalid.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.collections.freezable.FreezableList;
import net.digitalid.core.collections.IterableConverter;
import net.digitalid.collections.readonly.ReadOnlyList;
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
 * @version 1.0.0
 */
public class FreezableAttributeTypeSet extends FreezableLinkedHashSet<SemanticType> implements ReadOnlyAttributeTypeSet {
    
    /**
     * Stores the semantic type {@code list.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("list.attribute.type@core.digitalid.net").load(ListWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER);
    
    
    /**
     * Creates an empty set of attribute types.
     */
    public FreezableAttributeTypeSet() {}
    
    /**
     * Creates a new attribute type set with the given attribute type.
     * 
     * @param type the attribute type to add to the new set.
     */
    public @Single FreezableAttributeTypeSet(@Nonnull @AttributeType SemanticType type) {
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
    public @Nonnull @Frozen ReadOnlyAttributeTypeSet freeze() {
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
     */
    @NonFrozenRecipient
    public final void addAll(@Nonnull ReadOnlyAttributeTypeSet attributeSet) {
        for (final @Nonnull SemanticType type : attributeSet) {
            add(type);
        }
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAttributeTypeSet clone() {
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
    
    
    /**
     * Stores the converter that converts a type to the desired string.
     */
    private static final @Nonnull ElementConverter<SemanticType> converter = new ElementConverter<SemanticType>() { @Pure @Override public String toString(@Nullable SemanticType type) { return type == null ? "null" : type.getAddress().getString(); } };
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(this, converter, Brackets.CURLY);
    }
    
}
