package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.Attribute;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.both.Attributes;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the {@AttributeValue value} of an {@link Attribute attribute}.
 * 
 * @invariant !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class AttributeValueReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.value.attribute@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_VALUE = SemanticType.create("old.value.attribute@virtualid.ch").load(AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code new.value.attribute@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_VALUE = SemanticType.create("new.value.attribute@virtualid.ch").load(AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code replace.value.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("replace.value.attribute@virtualid.ch").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, OLD_VALUE, NEW_VALUE);
    
    
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
    public AttributeValueReplace(@Nonnull Attribute attribute, boolean published, @Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) {
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
    private AttributeValueReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity.toNonHostEntity(), signature, recipient);
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.attribute = Attribute.get(entity, IdentifierClass.create(tuple.getElementNotNull(0)).getIdentity().toSemanticType().checkIsAttributeFor(entity));
        this.published = new BooleanWrapper(tuple.getElementNotNull(1)).getValue();
        this.oldValue = tuple.isElementNotNull(2) ? AttributeValue.get(tuple.getElementNotNull(2), true).checkMatches(attribute) : null;
        this.newValue = tuple.isElementNotNull(3) ? AttributeValue.get(tuple.getElementNotNull(3), true).checkMatches(attribute) : null;
        if (Objects.equals(oldValue, newValue)) throw new InvalidEncodingException("The old and new value may not be equal.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(attribute.getType().toBlock(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(Attribute.PUBLISHED, published).toBlock(), Block.toBlock(OLD_VALUE, oldValue), Block.toBlock(NEW_VALUE, newValue)).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replaces the " + (published ? "published" : "unpublished") + " value '" + oldValue + "' with '" + newValue + "' of the attribute with the type " + attribute.getType().getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return new AgentPermissions(attribute.getType(), true).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return new AgentPermissions(attribute.getType(), false).freeze();
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        if (published) attribute.replaceValue(oldValue, newValue);
        else attribute.replaceUnpublishedValue(oldValue, newValue);
    }
    
    @Pure
    @Override
    public @Nonnull AttributeValueReplace getReverse() {
        return new AttributeValueReplace(attribute, published, newValue, oldValue);
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return Attributes.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new AttributeValueReplace(entity, signature, recipient, block);
        }
        
    }
    
}
