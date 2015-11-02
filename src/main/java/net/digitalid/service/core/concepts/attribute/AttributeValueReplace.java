package net.digitalid.service.core.concepts.attribute;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.certificate.CertificateIssue;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Replaces the {@link AttributeValue value} of an {@link Attribute attribute}.
 * 
 * @invariant !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
 */
@Immutable
final class AttributeValueReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.value.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_VALUE = SemanticType.map("old.value.attribute@core.digitalid.net").load(AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code new.value.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_VALUE = SemanticType.map("new.value.attribute@core.digitalid.net").load(AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code replace.value.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.value.attribute@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, OLD_VALUE, NEW_VALUE);
    
    
    /**
     * Stores the attribute of this action.
     */
    private final @Nonnull Attribute attribute;
    
    /**
     * Stores whether the published value is replaced.
     */
    private final boolean published;
    
    /**
     * Stores the old value of the attribute.
     * 
     * @invariant oldValue == null || oldValue.isVerified() && oldValue.matches(attribute) : "The old value is null or verified and matches the attribute.";
     */
    private final @Nullable AttributeValue oldValue;
    
    /**
     * Stores the new value of the attribute.
     * 
     * @invariant newValue == null || newValue.isVerified() && newValue.matches(attribute) : "The new value is null or verified and matches the attribute.";
     */
    private final @Nullable AttributeValue newValue;
    
    /**
     * Creates an internal action to replace the value of the given attribute.
     * 
     * @param attribute the attribute whose value is to be replaced.
     * @param published whether the published value is replaced.
     * @param oldValue the old value of the given attribute.
     * @param newValue the new value of the given attribute.
     * 
     * @require attribute.isOnClient() : "The attribute is on a client.";
     * @require !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
     * @require oldValue == null || oldValue.isVerified() && oldValue.matches(attribute) : "The old value is null or verified and matches the attribute.";
     * @require newValue == null || newValue.isVerified() && newValue.matches(attribute) : "The new value is null or verified and matches the attribute.";
     */
    AttributeValueReplace(@Nonnull Attribute attribute, boolean published, @Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) {
        super(attribute.getRole());
        
        assert !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
        assert oldValue == null || oldValue.isVerified() && oldValue.matches(attribute) : "The old value is null or verified and matches the attribute.";
        assert newValue == null || newValue.isVerified() && newValue.matches(attribute) : "The new value is null or verified and matches the attribute.";
        
        this.attribute = attribute;
        this.published = published;
        this.oldValue = oldValue;
        this.newValue = newValue;
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
    private AttributeValueReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity.toNonHostEntity(), signature, recipient);
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.attribute = Attribute.get(entity, IdentifierImplementation.create(tuple.getNonNullableElement(0)).getIdentity().toSemanticType().checkIsAttributeFor(entity));
        this.published = new BooleanWrapper(tuple.getNonNullableElement(1)).getValue();
        this.oldValue = tuple.isElementNotNull(2) ? AttributeValue.get(tuple.getNonNullableElement(2), true).checkMatches(attribute) : null;
        this.newValue = tuple.isElementNotNull(3) ? AttributeValue.get(tuple.getNonNullableElement(3), true).checkMatches(attribute) : null;
        if (Objects.equals(oldValue, newValue)) throw new InvalidEncodingException("The old and new value may not be equal.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, attribute.getType().toBlock(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(Attribute.PUBLISHED, published).toBlock(), Block.toBlock(OLD_VALUE, oldValue), Block.toBlock(NEW_VALUE, newValue)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the " + (published ? "published" : "unpublished") + " value '" + oldValue + "' with '" + newValue + "' of the attribute with the type " + attribute.getType().getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return new FreezableAgentPermissions(attribute.getType(), true).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return new FreezableAgentPermissions(attribute.getType(), false).freeze();
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws AbortException {
        if (published) attribute.replaceValue(oldValue, newValue);
        else attribute.replaceUnpublishedValue(oldValue, newValue);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AttributeValueReplace && ((AttributeValueReplace) action).attribute.equals(attribute) && ((AttributeValueReplace) action).published == published || action instanceof CertificateIssue && ((CertificateIssue) action).getCertificate().getContent().getType().equals(attribute.getType());
    }
    
    @Pure
    @Override
    public @Nonnull AttributeValueReplace getReverse() {
        return new AttributeValueReplace(attribute, published, newValue, oldValue);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof AttributeValueReplace) {
            final @Nonnull AttributeValueReplace other = (AttributeValueReplace) object;
            return this.attribute.equals(other.attribute) && this.published == other.published && Objects.equals(this.oldValue, other.oldValue) && Objects.equals(this.newValue, other.newValue);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + attribute.hashCode();
        hash = 89 * hash + (published ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(oldValue);
        hash = 89 * hash + Objects.hashCode(newValue);
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
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException  {
            return new AttributeValueReplace(entity, signature, recipient, block);
        }
        
    }
    
}
