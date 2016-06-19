package net.digitalid.core.contact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.converter.Brackets;
import net.digitalid.utility.collections.converter.ElementConverter;
import net.digitalid.utility.collections.converter.IterableConverter;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.freezable.NonFrozenRecipient;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.Single;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.AttributeType;

/**
 * This class models a freezable set of attribute types.
 * 
 * @see Authentications
 * @see ContactPermissions
 */
public class FreezableAttributeTypeSet extends FreezableLinkedHashSet<SemanticType> implements ReadOnlyAttributeTypeSet {
    
    /**
     * Stores the semantic type {@code list.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("list.attribute.type@core.digitalid.net").load(ListWrapper.XDF_TYPE, SemanticType.ATTRIBUTE_IDENTIFIER);
    
    
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
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
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
    public FreezableAttributeTypeSet(@Nonnull Block block) throws ExternalException {
        Require.that(block.getType().isBasedOn(getType())).orThrow("The block is based on the indicated type.");
        
        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull SemanticType type = IdentityImplementation.create(element).castTo(SemanticType.class).checkIsAttributeType();
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
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(size());
        for (final @Nonnull SemanticType type : this) {
            elements.add(type.toBlock(SemanticType.ATTRIBUTE_IDENTIFIER));
        }
        return ListWrapper.encode(getType(), elements.freeze());
    }
    
    
    @Override
    public @Nonnull @Frozen ReadOnlyAttributeTypeSet freeze() {
        super.freeze();
        return this;
    }
    
    
    @Override
    public final boolean add(@Nonnull @AttributeType SemanticType type) {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
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
