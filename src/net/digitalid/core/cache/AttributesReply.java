package net.digitalid.core.cache;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.OnlyForHosts;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.attribute.CertifiedAttributeValue;
import net.digitalid.core.attribute.UncertifiedAttributeValue;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.external.InvalidSignatureException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.service.CoreServiceQueryReply;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.ListWrapper;

/**
 * Replies the queried attribute values of the given subject that are accessible by the requester.
 * 
 * @see AttributesQuery
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
            if (attribute != null && !attribute.isVerified()) return false;
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
    @OnlyForHosts
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
    private AttributesReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        final @Nonnull InternalIdentity subject = getSubject().getIdentity();
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElements();
        final @Nonnull FreezableList<AttributeValue> attributeValues = new FreezableArrayList<>(elements.size());
        final @Nonnull Time time = Time.getCurrent();
        for (final @Nullable Block element : elements) {
            if (element != null) {
                final @Nonnull AttributeValue attributeValue = AttributeValue.get(element, false);
                try {
                    attributeValue.verify();
                    if (attributeValue.isCertified()) {
                        final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.toCertifiedAttributeValue();
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
        if (attributeValues.isEmpty()) throw new InvalidEncodingException("The attribute values may not be empty.");
        this.attributeValues = attributeValues.freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(attributeValues.size());
        for (final @Nullable AttributeValue attributeValue : attributeValues) {
            elements.add(Block.toBlock(AttributeValue.TYPE, attributeValue));
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AttributesReply(entity, signature, number, block);
        }
        
    }
    
}
