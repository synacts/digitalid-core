package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a freezable set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
abstract class AttributeSet extends FreezableLinkedHashSet<SemanticType> implements ReadonlyAttributeSet, Blockable {
    
    /**
     * Creates an empty set of attribute types.
     */
    protected AttributeSet() {}
    
    /**
     * Creates a new attribute set with the given attribute type.
     * 
     * @param type the attribute type to add to the new set.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure areSingle() : "The new attribute set contains a single element.";
     */
    protected AttributeSet(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        add(type);
    }
    
    /**
     * Creates a new attribute set from the given attribute set.
     * 
     * @param attributeSet the attribute set to add to the new attribute set.
     */
    protected AttributeSet(@Nonnull ReadonlyAttributeSet attributeSet) {
        addAll(attributeSet);
    }
    
    /**
     * Creates a new attribute set from the given block.
     * 
     * @param block the block containing the attribute set.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    protected AttributeSet(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull SemanticType type = new NonHostIdentifier(element).getIdentity().toSemanticType();
            type.checkIsAttributeType();
            add(type);
        }
    }
    
    /**
     * Returns the type of the list elements.
     * 
     * @return the type of the list elements.
     * 
     * @ensure return.isBasedOn(SemanticType.IDENTIFIER) : "The returned type is based on the semantic type.";
     */
    @Pure
    public abstract @Nonnull SemanticType getAttributeType();
    
    /**
     * @ensure return.isBasedOn(ListWrapper.TYPE) : "The returned type is based on the list type.";
     */
    @Pure
    @Override
    public abstract @Nonnull SemanticType getType();
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(size());
        for (final @Nonnull SemanticType type : this) {
            elements.add(type.getAddress().toBlock().setType(getAttributeType()));
        }
        return new ListWrapper(getType(), elements.freeze()).toBlock();
    }
    
    
    @Override
    public @Nonnull ReadonlyAttributeSet freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public final boolean areSingle() {
        return size() == 1;
    }
    
    
    /**
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Override
    public final boolean add(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        return super.add(type);
    }
    
    /**
     * Adds the given attribute set to this attribute set.
     * 
     * @param attributeSet the attribute set to add to this attribute set.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public final void addAll(@Nonnull ReadonlyAttributeSet attributeSet) {
        for (final @Nonnull SemanticType type : attributeSet) {
            add(type);
        }
    }
    
    
    @Pure
    @Override
    public abstract @Capturable @Nonnull AttributeSet clone();
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull SemanticType type : this) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString());
        }
        string.append("]");
        return string.toString();
    }
    
}
