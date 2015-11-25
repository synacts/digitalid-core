package net.digitalid.service.core.cache;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.attribute.Attribute;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.FreezableAttributeTypeSet;
import net.digitalid.service.core.concepts.contact.ReadOnlyAttributeTypeSet;
import net.digitalid.service.core.concepts.contact.ReadOnlyContactPermissions;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.expression.PassiveExpression;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.handler.core.CoreServiceExternalQuery;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * Queries the given attributes from the given subject.
 * 
 * @see AttributesReply
 */
@Immutable
public final class AttributesQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("query.attribute@core.digitalid.net").load(TupleWrapper.XDF_TYPE, FreezableAttributeTypeSet.TYPE, Attribute.PUBLISHED);
    
    
    /**
     * Stores the attribute types that are queried.
     * 
     * @invariant attributeTypes.isFrozen() : "The attribute types are frozen.";
     * @invariant !attributeTypes.isEmpty() : "The attribute types are not empty.";
     */
    private final @Nonnull ReadOnlyAttributeTypeSet attributeTypes;
    
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
     * @require !attributeTypes.isEmpty() : "The attribute types are not empty.";
     */
    public AttributesQuery(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull ReadOnlyAttributeTypeSet attributeTypes, boolean published) {
        super(role, subject);
        
        assert attributeTypes.isFrozen() : "The attribute types are frozen.";
        assert !attributeTypes.isEmpty() : "The attribute types are not empty.";
        
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
    private AttributesQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.attributeTypes = new FreezableAttributeTypeSet(elements.getNonNullable(0)).freeze();
        if (attributeTypes.isEmpty()) { throw InvalidParameterValueException.get("attribute types", attributeTypes); }
        this.published = BooleanWrapper.decode(elements.getNonNullable(1));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, attributeTypes, BooleanWrapper.encode(Attribute.PUBLISHED, published));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Queries the " + (published ? "published" : "unpublished") + " attributes " + attributeTypes + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return attributeTypes.toAgentPermissions().freeze();
    }
    
    
    @Override
    @NonCommitting
    public @Nonnull AttributesReply executeOnHost() throws RequestException, SQLException {
        final @Nonnull FreezableList<AttributeValue> attributeValues = FreezableArrayList.getWithCapacity(attributeTypes.size());
        
        final @Nonnull Account account = getAccount();
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        final boolean isInternalPerson = account.getIdentity() instanceof InternalPerson;
        if (signature instanceof CredentialsSignatureWrapper && isInternalPerson) {
            final @Nonnull CredentialsSignatureWrapper credentialsSignature = (CredentialsSignatureWrapper) signature;
            
            final @Nullable ReadOnlyContactPermissions contactPermissions;
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
                    if (contactPermissions != null && contactPermissions.contains(attributeType)) { attributeValues.add(attributeValue); }
                    else {
                        final @Nullable PassiveExpression attributeVisibility = attribute.getVisibility();
                        if (attributeVisibility != null && attributeVisibility.matches(credentialsSignature)) { attributeValues.add(attributeValue); }
                        else { attributeValues.add(null); }
                    }
                } else { attributeValues.add(null); }
            }
        } else {
            for (final @Nonnull SemanticType attributeType : attributeTypes) {
                final @Nonnull Attribute attribute = Attribute.get(account, attributeType);
                final @Nullable AttributeValue attributeValue = published ? attribute.getValue() : attribute.getUnpublishedValue();
                if (attributeValue != null) {
                    if (isInternalPerson) {
                        final @Nullable PassiveExpression attributeVisibility = attribute.getVisibility();
                        if (attributeVisibility != null && attributeVisibility.isPublic()) { attributeValues.add(attributeValue); }
                        else { attributeValues.add(null); }
                    } else { attributeValues.add(attributeValue); }
                } else { attributeValues.add(null); }
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new AttributesQuery(entity, signature, recipient, block);
        }
        
    }
    
}
