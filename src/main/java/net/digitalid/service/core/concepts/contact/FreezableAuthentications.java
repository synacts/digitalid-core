package net.digitalid.service.core.concepts.contact;

import net.digitalid.service.core.block.Block;

import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models the authentications of contacts as a set of attribute types.
 */
public final class FreezableAuthentications extends FreezableAttributeTypeSet implements ReadOnlyAuthentications {
    
    /**
     * Stores the semantic type {@code authentication.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("authentication.contact@core.digitalid.net").load(FreezableAttributeTypeSet.TYPE);
    
    
    /**
     * Stores the semantic type {@code identity.based.authentication.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTITY_BASED_TYPE = SemanticType.map("identity.based.authentication.contact@core.digitalid.net").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, BooleanWrapper.TYPE);
    
    /**
     * Stores an empty set of authentications.
     */
    public static final @Nonnull ReadOnlyAuthentications NONE = new FreezableAuthentications().freeze();
    
    /**
     * Stores an identity-based authentication.
     */
    public static final @Nonnull ReadOnlyAuthentications IDENTITY_BASED = new FreezableAuthentications(IDENTITY_BASED_TYPE).freeze();
    
    
    /**
     * Creates an empty set of authentications.
     */
    public FreezableAuthentications() {}
    
    /**
     * Creates new authentications with the given attribute type.
     * 
     * @param type the attribute type used for authentication.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure isSingle() : "The new authentications are single.";
     */
    public FreezableAuthentications(@Nonnull SemanticType type) {
        super(type);
    }
    
    /**
     * Creates new authentications from the given authentications.
     * 
     * @param authentications the authentications to add to the new authentications.
     */
    public FreezableAuthentications(@Nonnull ReadOnlyAuthentications authentications) {
        super(authentications);
    }
    
    /**
     * Creates new authentications from the given block.
     * 
     * @param block the block containing the authentications.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public FreezableAuthentications(@Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    @Override
    public @Nonnull @Frozen ReadOnlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAuthentications clone() {
        return new FreezableAuthentications(this);
    }
    
}
