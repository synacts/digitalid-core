package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.external.AttributesQuery;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the queried attributes of the given subject that are accessible by the requester.
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
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.attribute@virtualid.ch").load(Certificate.LIST);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Returns whether all the attributes which are not null are certificates.
     * 
     * @param attributes the list of attributes which is to be checked.
     * 
     * @return whether all the attributes which are not null are certificates.
     */
    public static boolean areCertificates(@Nonnull ReadonlyList<SignatureWrapper> attributes) {
        for (final @Nullable SignatureWrapper attribute : attributes) {
            if (attribute != null && !attribute.isCertificate()) return false;
        }
        return true;
    }
    
    
    /**
     * Stores the attributes of this reply.
     * 
     * @invariant attributes.isFrozen() : "The attributes are frozen.";
     * @invariant attributes.isNotEmpty() : "The attributes are not empty.";
     * @invariant areCertificates(attributes) : "All the attributes which are not null are certificates.";
     */
    private final @Nonnull ReadonlyList<SignatureWrapper> attributes;
    
    /**
     * Creates an attributes reply for the queried attributes of given subject.
     * 
     * @param subject the subject of this handler.
     * @param attributes the attributes of this reply.
     * 
     * @require attributes.isFrozen() : "The attributes are frozen.";
     * @require attributes.isNotEmpty() : "The attributes are not empty.";
     * @require areCertificates(attributes) : "All the attributes which are not null are certificates.";
     */
    public AttributesReply(@Nonnull InternalNonHostIdentifier subject, @Nonnull ReadonlyList<SignatureWrapper> attributes) throws SQLException, PacketException {
        super(subject);
        
        assert attributes.isFrozen() : "The attributes are frozen.";
        assert attributes.isNotEmpty() : "The attributes are not empty.";
        assert areCertificates(attributes) : "All the attributes which are not null are certificates.";
        
        this.attributes = attributes;
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
    private AttributesReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElements();
        final @Nonnull FreezableList<SignatureWrapper> attributes = new FreezableArrayList<SignatureWrapper>(elements.size());
        for (final @Nullable Block element : elements) {
            if (element != null) {
                final @Nonnull SignatureWrapper attribute = SignatureWrapper.decodeWithoutVerifying(element, false, null);
                try {
                    attribute.verifyAsCertificate();
                    attributes.add(attribute);
                } catch (@Nonnull InvalidSignatureException exception) {
                    attributes.add(new SignatureWrapper(Certificate.TYPE, attribute.getElementNotNull(), null));
                }
            } else {
                attributes.add(null);
            }
        }
        this.attributes = attributes.freeze();
        if (attributes.isEmpty()) throw new InvalidEncodingException("The attributes may not be empty.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull ReadonlyList<SignatureWrapper> attributes = this.attributes;
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(attributes.size());
        for (final @Nullable SignatureWrapper attribute : attributes) {
            elements.add(Block.toBlock(Certificate.TYPE, attribute));
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replies the queried attributes.";
    }
    
    
    /**
     * Returns the attributes of this reply.
     * 
     * @return the attributes of this reply.
     * 
     * @ensure return.isFrozen() : "The attributes are frozen.";
     * @ensure attributes.isNotEmpty() : "The attributes are not empty.";
     * @ensure areCertificates(return) : "All the attributes which are not null are certificates.";
     */
    public @Nonnull ReadonlyList<SignatureWrapper> getAttributes() {
        return attributes;
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Reply create(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AttributesReply(entity, signature, number, block);
        }
        
    }
    
}
