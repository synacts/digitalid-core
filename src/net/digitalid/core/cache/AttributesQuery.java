package net.digitalid.core.cache;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.ReadonlyAgentPermissions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyArray;
import net.digitalid.core.contact.AttributeTypeSet;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.contact.ReadonlyAttributeTypeSet;
import net.digitalid.core.contact.ReadonlyContactPermissions;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.service.CoreServiceExternalQuery;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.BooleanWrapper;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Queries the given attributes from the given subject.
 * 
 * @see AttributesReply
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class AttributesQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.attribute@core.digitalid.net").load(TupleWrapper.TYPE, AttributeTypeSet.TYPE, Attribute.PUBLISHED);
    
    
    /**
     * Stores the attribute types that are queried.
     * 
     * @invariant attributeTypes.isFrozen() : "The attribute types are frozen.";
     * @invariant attributeTypes.isNotEmpty() : "The attribute types are not empty.";
     */
    private final @Nonnull ReadonlyAttributeTypeSet attributeTypes;
    
    /**
     * Stores whether the published values are queried.
     */
    private final boolean published;
    
    /**
     * Creates an attributes query to query the given attributes of the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param attributeTypes the queried attribute types.
     * @param published whether the published values are queried.
     * 
     * @require attributeTypes.isFrozen() : "The attribute types are frozen.";
     * @require attributeTypes.isNotEmpty() : "The attribute types are not empty.";
     */
    public AttributesQuery(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull ReadonlyAttributeTypeSet attributeTypes, boolean published) {
        super(role, subject);
        
        assert attributeTypes.isFrozen() : "The attribute types are frozen.";
        assert attributeTypes.isNotEmpty() : "The attribute types are not empty.";
        
        this.attributeTypes = attributeTypes;
        this.published = published;
    }
    
    /**
     * Creates an attributes query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    @NonCommitting
    private AttributesQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.attributeTypes = new AttributeTypeSet(elements.getNotNull(0)).freeze();
        if (attributeTypes.isEmpty()) throw new InvalidEncodingException("The attribute types may not be empty.");
        this.published = new BooleanWrapper(elements.getNotNull(1)).getValue();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, attributeTypes, new BooleanWrapper(Attribute.PUBLISHED, published)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Queries the " + (published ? "published" : "unpublished") + " attributes " + attributeTypes + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return attributeTypes.toAgentPermissions().freeze();
    }
    
    
    @Override
    @NonCommitting
    public @Nonnull AttributesReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull FreezableList<AttributeValue> attributeValues = new FreezableArrayList<AttributeValue>(attributeTypes.size());
        
        final @Nonnull Account account = getAccount();
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        final boolean isInternalPerson = account.getIdentity() instanceof InternalPerson;
        if (signature instanceof CredentialsSignatureWrapper && isInternalPerson) {
            final @Nonnull CredentialsSignatureWrapper credentialsSignature = (CredentialsSignatureWrapper) signature;
            
            final @Nullable ReadonlyContactPermissions contactPermissions;
            if (credentialsSignature.isIdentityBased() && !credentialsSignature.isRoleBased()) {
                final @Nonnull InternalPerson issuer = credentialsSignature.getIssuer();
                final @Nonnull Contact contact = Contact.get(getNonHostEntity(), issuer);
                contactPermissions = contact.getPermissions();
            } else {
                contactPermissions = null;
            }
            
            for (final @Nonnull SemanticType attributeType : attributeTypes) {
                final @Nonnull Attribute attribute = Attribute.get(account, attributeType);
                final @Nullable AttributeValue attributeValue = published ? attribute.getValue() : attribute.getUnpublishedValue();
                if (attributeValue != null) {
                    if (contactPermissions != null && contactPermissions.contains(attributeType)) attributeValues.add(attributeValue);
                    else {
                        final @Nullable PassiveExpression attributeVisibility = attribute.getVisibility();
                        if (attributeVisibility != null && attributeVisibility.matches(credentialsSignature)) attributeValues.add(attributeValue);
                        else attributeValues.add(null);
                    }
                } else attributeValues.add(null);
            }
        } else {
            for (final @Nonnull SemanticType attributeType : attributeTypes) {
                final @Nonnull Attribute attribute = Attribute.get(account, attributeType);
                final @Nullable AttributeValue attributeValue = published ? attribute.getValue() : attribute.getUnpublishedValue();
                if (attributeValue != null) {
                    if (isInternalPerson) {
                        final @Nullable PassiveExpression attributeVisibility = attribute.getVisibility();
                        if (attributeVisibility != null && attributeVisibility.isPublic()) attributeValues.add(attributeValue);
                        else attributeValues.add(null);
                    } else attributeValues.add(attributeValue);
                } else attributeValues.add(null);
            }
        }
        
        return new AttributesReply(account, attributeValues.freeze());
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof AttributesReply;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof AttributesQuery) {
            final @Nonnull AttributesQuery other = (AttributesQuery) object;
            return this.attributeTypes.equals(other.attributeTypes) && this.published == other.published;
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + attributeTypes.hashCode();
        hash = 89 * hash + (published ? 1 : 0);
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AttributesQuery(entity, signature, recipient, block);
        }
        
    }
    
}
