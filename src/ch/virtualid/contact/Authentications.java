package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models the authentications of contacts as a set of attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Authentications extends AttributeTypeSet implements ReadonlyAuthentications, Blockable {
    
    /**
     * Stores the semantic type {@code authentication.contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("authentication.contact@virtualid.ch").load(AttributeTypeSet.TYPE);
    
    
    /**
     * Stores the semantic type {@code identity.based.authentication.contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTITY_BASED_TYPE = SemanticType.create("identity.based.authentication.contact@virtualid.ch").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, BooleanWrapper.TYPE);
    
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
        super(type);
    }
    
    /**
     * Creates new authentications from the given authentications.
     * 
     * @param authentications the authentications to add to the new authentications.
     */
    public Authentications(@Nonnull ReadonlyAuthentications authentications) {
        super(authentications);
    }
    
    /**
     * Creates new authentications from the given block.
     * 
     * @param block the block containing the authentications.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Authentications(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Override
    public @Nonnull ReadonlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull Authentications clone() {
        return new Authentications(this);
    }
    
}
