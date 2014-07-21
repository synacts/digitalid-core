package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models the authentications of agents as a set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Authentications extends FreezableLinkedHashSet<SemanticType> implements ReadonlyAuthentications, Blockable {
    
    /**
     * Stores the semantic type {@code type.authentication.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ATTRIBUTE_TYPE = SemanticType.create("type.authentication.agent@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code authentication.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("authentication.agent@virtualid.ch").load(ListWrapper.TYPE, ATTRIBUTE_TYPE);
    
    
    /**
     * Stores the semantic type {@code identity.based.authentication.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTITY_BASED_TYPE = SemanticType.create("identity.based.authentication.agent@virtualid.ch").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, BooleanWrapper.TYPE);
    
    /**
     * Stores an empty set of authentications.
     */
    public static final @Nonnull ReadonlyAuthentications NONE = new Authentications().freeze();
    
    /**
     * Stores an identity-based authentication.
     */
    public static final @Nonnull ReadonlyAuthentications IDENTITY_BASED = new Authentications(IDENTITY_BASED_TYPE).freeze();
    
    /**
     * Creates an empty set of authentications.
     */
    public Authentications() {}
    
    /**
     * Creates new authentications with the given attribute type.
     * 
     * @param type the attribute type used for authentication.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure areSingle() : "The new authentications are single.";
     */
    public Authentications(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        add(type);
    }
    
    /**
     * Creates new authentications with the given authentications.
     * 
     * @param authentications the authentications to add to the new authentications.
     */
    public Authentications(@Nonnull ReadonlyAuthentications authentications) {
        addAll(authentications);
    }
    
    /**
     * Creates new authentications from the given block.
     * 
     * @param block the block containing the authentications.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public Authentications(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull SemanticType type = new NonHostIdentifier(element).getIdentity().toSemanticType();
            type.checkIsAttributeType();
            add(type);
        }
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(size());
        for (final @Nonnull SemanticType type : this) {
            elements.add(type.getAddress().toBlock().setType(ATTRIBUTE_TYPE));
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    @Override
    public @Nonnull ReadonlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean areSingle() {
        return size() == 1;
    }
    
    
    /**
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Override
    public boolean add(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        return super.add(type);
    }
    
    /**
     * Adds the given authentications to these authentications.
     * 
     * @param authentications the authentications to add to these authentications.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void addAll(@Nonnull ReadonlyAuthentications authentications) {
        for (final @Nonnull SemanticType type : authentications) {
            add(type);
        }
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone() {
        return new Authentications(this);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull SemanticType type : this) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString());
        }
        string.append("]");
        return string.toString();
    }
    
}
