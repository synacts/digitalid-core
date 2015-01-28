package ch.virtualid.cache;

import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.attribute.UncertifiedAttributeValue;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.ListWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the queried attribute values of the given subject that are accessible by the requester.
 * 
 * @see AttributesQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class AttributesReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.attribute@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("reply.attribute@virtualid.ch").load(AttributeValue.LIST);
    
    
    /**
     * Returns whether all the attribute values which are not null are verified.
     * 
     * @param attributeValues the list of attribute values which is to be checked.
     * 
     * @return whether all the attribute values which are not null are verified.
     */
    static boolean areVerified(@Nonnull ReadonlyList<AttributeValue> attributeValues) {
        for (final @Nullable AttributeValue attribute : attributeValues) {
            if (attribute != null && !attribute.isVerified()) return false;
        }
        return true;
    }
    
    
    /**
     * Stores the attribute values of this reply.
     * 
     * @invariant attributeValues.isFrozen() : "The attribute values are frozen.";
     * @invariant attributeValues.isNotEmpty() : "The attribute values are not empty.";
     * @invariant areVerified(attributeValues) : "All the attribute values which are not null are verified.";
     */
    private final @Nonnull ReadonlyList<AttributeValue> attributeValues;
    
    /**
     * Creates an attributes reply for the queried attribute values of given subject.
     * 
     * @param subject the subject of this handler.
     * @param attributeValues the attribute values of this reply.
     * 
     * @require attributeValues.isFrozen() : "The attribute values are frozen.";
     * @require attributeValues.isNotEmpty() : "The attribute values are not empty.";
     * @require areVerified(attributeValues) : "All the attribute values which are not null are verified.";
     */
    AttributesReply(@Nonnull InternalIdentifier subject, @Nonnull ReadonlyList<AttributeValue> attributeValues) throws SQLException, PacketException {
        super(subject);
        
        assert attributeValues.isFrozen() : "The attribute values are frozen.";
        assert attributeValues.isNotEmpty() : "The attribute values are not empty.";
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
    private AttributesReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        final @Nonnull InternalIdentity subject = getSubject().getIdentity();
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElements();
        final @Nonnull FreezableList<AttributeValue> attributeValues = new FreezableArrayList<AttributeValue>(elements.size());
        final @Nonnull Time time = new Time();
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
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(attributeValues.size());
        for (final @Nullable AttributeValue attributeValue : attributeValues) {
            elements.add(Block.toBlock(AttributeValue.TYPE, attributeValue));
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replies the queried attribute values.";
    }
    
    
    /**
     * Returns the attribute values of this reply.
     * 
     * @return the attribute values of this reply.
     * 
     * @ensure return.isFrozen() : "The attribute values are frozen.";
     * @ensure return.isNotEmpty() : "The attribute values are not empty.";
     * @ensure areVerified(return) : "All the attribute values which are not null are verified.";
     */
    @Nonnull ReadonlyList<AttributeValue> getAttributeValues() {
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AttributesReply(entity, signature, number, block);
        }
        
    }
    
}
