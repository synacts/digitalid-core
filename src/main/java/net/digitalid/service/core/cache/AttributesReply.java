package net.digitalid.service.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.attribute.CertifiedAttributeValue;
import net.digitalid.service.core.concepts.attribute.UncertifiedAttributeValue;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.exceptions.external.signature.InvalidSignatureException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.handler.core.CoreServiceQueryReply;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.annotations.Hosts;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * Replies the queried attribute values of the given subject that are accessible by the requester.
 * 
 * @see AttributesQuery
 */
@Immutable
public final class AttributesReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.attribute@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("reply.attribute@core.digitalid.net").load(AttributeValue.LIST);
    
    
    /**
     * Returns whether all the attribute values which are not null are verified.
     * 
     * @param attributeValues the list of attribute values which is to be checked.
     * 
     * @return whether all the attribute values which are not null are verified.
     */
    static boolean areVerified(@Nonnull ReadOnlyList<AttributeValue> attributeValues) {
        for (final @Nullable AttributeValue attribute : attributeValues) {
            if (attribute != null && !attribute.isVerified()) { return false; }
        }
        return true;
    }
    
    
    /**
     * Stores the attribute values of this reply.
     * 
     * @invariant attributeValues.isFrozen() : "The attribute values are frozen.";
     * @invariant !attributeValues.isEmpty() : "The attribute values are not empty.";
     * @invariant areVerified(attributeValues) : "All the attribute values which are not null are verified.";
     */
    private final @Nonnull ReadOnlyList<AttributeValue> attributeValues;
    
    /**
     * Creates an attributes reply for the queried attribute values of given subject.
     * 
     * @param account the account to which this query reply belongs.
     * @param attributeValues the attribute values of this reply.
     * 
     * @require attributeValues.isFrozen() : "The attribute values are frozen.";
     * @require !attributeValues.isEmpty() : "The attribute values are not empty.";
     * @require areVerified(attributeValues) : "All the attribute values which are not null are verified.";
     */
    @Hosts
    @NonCommitting
    AttributesReply(@Nonnull Account account, @Nonnull ReadOnlyList<AttributeValue> attributeValues) {
        super(account);
        
        assert attributeValues.isFrozen() : "The attribute values are frozen.";
        assert !attributeValues.isEmpty() : "The attribute values are not empty.";
        assert areVerified(attributeValues) : "All the attribute values which are not null are verified.";
        
        this.attributeValues = attributeValues;
    }
    
    /**
     * Creates an attribute reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * @param block the content which is to be decoded.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    @NonCommitting
    private AttributesReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, number);
        
        final @Nonnull InternalIdentity subject = getSubject().getIdentity();
        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNullableElements(block);
        final @Nonnull FreezableList<AttributeValue> attributeValues = FreezableArrayList.getWithCapacity(elements.size());
        final @Nonnull Time time = Time.getCurrent();
        for (final @Nullable Block element : elements) {
            if (element != null) {
                final @Nonnull AttributeValue attributeValue = AttributeValue.get(element, false);
                try {
                    attributeValue.verify();
                    if (attributeValue.isCertified()) {
                        final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
                        certifiedAttributeValue.checkSubject(subject);
                        certifiedAttributeValue.checkIsValid(time);
                    }
                    attributeValues.add(attributeValue);
                } catch (@Nonnull InvalidSignatureException exception) {
                    attributeValues.add(new UncertifiedAttributeValue(attributeValue.getContent()));
                }
            } else {
                attributeValues.add(null);
            }
        }
        if (attributeValues.isEmpty()) { throw InvalidParameterValueException.get("attribute values", attributeValues); }
        this.attributeValues = attributeValues.freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(attributeValues.size());
        for (final @Nullable AttributeValue attributeValue : attributeValues) {
            elements.add(Block.toBlock(AttributeValue.TYPE, attributeValue));
        }
        return ListWrapper.encode(TYPE, elements.freeze());
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the queried attribute values.";
    }
    
    
    /**
     * Returns the attribute values of this reply.
     * 
     * @return the attribute values of this reply.
     * 
     * @ensure return.isFrozen() : "The attribute values are frozen.";
     * @ensure !return.isEmpty() : "The attribute values are not empty.";
     * @ensure areVerified(return) : "All the attribute values which are not null are verified.";
     */
    @Nonnull ReadOnlyList<AttributeValue> getAttributeValues() {
        return attributeValues;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AttributesReply && this.attributeValues.equals(((AttributesReply) object).attributeValues);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + attributeValues.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new AttributesReply(entity, signature, number, block);
        }
        
    }
    
}
