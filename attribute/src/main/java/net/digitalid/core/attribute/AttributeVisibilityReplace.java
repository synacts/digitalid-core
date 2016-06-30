package net.digitalid.core.attribute;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.exceptions.InvalidConceptPropertyActionException;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.service.handler.CoreServiceInternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.IdentifierImplementation;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * Replaces the {@link PassiveExpression visibility} of an {@link Attribute attribute}.
 * 
 * @invariant !Objects.equals(oldVisibility, newVisibility) : "The old and new visibility are not equal.";
 */
@Immutable
final class AttributeVisibilityReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.visibility.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_VISIBILITY = SemanticType.map("old.visibility.attribute@core.digitalid.net").load(PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code new.visibility.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_VISIBILITY = SemanticType.map("new.visibility.attribute@core.digitalid.net").load(PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code replace.visibility.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.visibility.attribute@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, OLD_VISIBILITY, NEW_VISIBILITY);
    
    
    /**
     * Stores the attribute of this action.
     */
    private final @Nonnull Attribute attribute;
    
    /**
     * Stores the old visibility of the attribute.
     * 
     * @invariant oldVisibility == null || oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility is null or belongs to the entity of the attribute.";
     */
    private final @Nullable PassiveExpression oldVisibility;
    
    /**
     * Stores the new visibility of the attribute.
     * 
     * @invariant newVisibility == null || newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility is null or belongs to the entity of the attribute.";
     */
    private final @Nullable PassiveExpression newVisibility;
    
    /**
     * Creates an internal action to replace the visibility of the given attribute.
     * 
     * @param attribute the attribute whose visibility is to be replaced.
     * @param oldVisibility the old visibility of the given attribute.
     * @param newVisibility the new visibility of the given attribute.
     * 
     * @require attribute.isOnClient() : "The attribute is on a client.";
     * @require !Objects.equals(oldVisibility, newVisibility) : "The old and new visibility are not equal.";
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the attribute belongs to an internal person.";
     * @require oldVisibility == null || oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility is null or belongs to the entity of the attribute.";
     * @require newVisibility == null || newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility is null or belongs to the entity of the attribute.";
     */
    AttributeVisibilityReplace(@Nonnull Attribute attribute, @Nullable PassiveExpression oldVisibility, @Nullable PassiveExpression newVisibility) {
        super(attribute.getRole());
        
        Require.that(!Objects.equals(oldVisibility, newVisibility)).orThrow("The old and new visibility are not equal.");
        Require.that(attribute.getEntity().getIdentity() instanceof InternalPerson).orThrow("The entity of the attribute belongs to an internal person.");
        Require.that(oldVisibility == null || oldVisibility.getEntity().equals(attribute.getEntity())).orThrow("The old visibility is null or belongs to the entity of the attribute.");
        Require.that(newVisibility == null || newVisibility.getEntity().equals(attribute.getEntity())).orThrow("The new visibility is null or belongs to the entity of the attribute.");
        
        this.attribute = attribute;
        this.oldVisibility = oldVisibility;
        this.newVisibility = newVisibility;
    }
    
    /**
     * Creates an internal action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private AttributeVisibilityReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.castTo(NonHostEntity.class);
        nonHostEntity.getIdentity().castTo(InternalPerson.class);
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
        this.attribute = Attribute.get(entity, IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(0)).getIdentity().castTo(SemanticType.class).checkIsAttributeFor(entity));
        this.oldVisibility = tuple.isElementNotNull(1) ? new PassiveExpression(nonHostEntity, tuple.getNonNullableElement(1)) : null;
        this.newVisibility = tuple.isElementNotNull(2) ? new PassiveExpression(nonHostEntity, tuple.getNonNullableElement(2)) : null;
        if (Objects.equals(oldVisibility, newVisibility)) { throw InvalidConceptPropertyActionException.get(this); }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, attribute.getType().toBlock(SemanticType.ATTRIBUTE_IDENTIFIER), Block.toBlock(OLD_VISIBILITY, oldVisibility), Block.toBlock(NEW_VISIBILITY, newVisibility));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the visibility '" + oldVisibility + "' with '" + newVisibility + "' of the attribute with the type " + attribute.getType().getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return new FreezableAgentPermissions(attribute.getType(), true).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return new FreezableAgentPermissions(attribute.getType(), true).freeze();
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws DatabaseException {
        attribute.replaceVisibility(oldVisibility, newVisibility);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AttributeVisibilityReplace && ((AttributeVisibilityReplace) action).attribute.equals(attribute);
    }
    
    @Pure
    @Override
    public @Nonnull AttributeVisibilityReplace getReverse() {
        return new AttributeVisibilityReplace(attribute, newVisibility, oldVisibility);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof AttributeVisibilityReplace) {
            final @Nonnull AttributeVisibilityReplace other = (AttributeVisibilityReplace) object;
            return this.attribute.equals(other.attribute) && Objects.equals(this.oldVisibility, other.oldVisibility) && Objects.equals(this.newVisibility, other.newVisibility);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + attribute.hashCode();
        hash = 89 * hash + Objects.hashCode(oldVisibility);
        hash = 89 * hash + Objects.hashCode(newVisibility);
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return AttributeModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    @Stateless
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
            return new AttributeVisibilityReplace(entity, signature, recipient, block);
        }
        
    }
    
}
