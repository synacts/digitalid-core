package ch.virtualid.handler.query.external;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.AttributeSet;
import ch.virtualid.contact.ReadonlyAttributeSet;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.AttributesReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries the given attributes from the given subject.
 * 
 * @see AttributesReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.6
 */
public final class AttributesQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.attribute@virtualid.ch").load(AttributeSet.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the attributes that are queried.
     * 
     * @invariant attributes.isFrozen() : "The attributes are frozen.";
     * @invariant attributes.isNotEmpty() : "The attributes are not empty.";
     */
    private final @Nonnull ReadonlyAttributeSet attributes;
    
    /**
     * Creates an attributes query to query the given attributes of the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param attributes the queried attributes.
     * 
     * @require attributes.isFrozen() : "The attributes are frozen.";
     * @require attributes.isNotEmpty() : "The attributes are not empty.";
     */
    public AttributesQuery(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull ReadonlyAttributeSet attributes) {
        super(role, subject);
        
        assert attributes.isFrozen() : "The attributes are frozen.";
        assert attributes.isNotEmpty() : "The attributes are not empty.";
        
        this.attributes = attributes;
        this.requiredPermissions = attributes.toAgentPermissions().freeze();
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
    private AttributesQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.attributes = new AttributeSet(block).freeze();
        if (attributes.isEmpty()) throw new InvalidEncodingException("The attributes may not be empty.");
        this.requiredPermissions = attributes.toAgentPermissions().freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return attributes.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Queries the attributes " + attributes + ".";
    }
    
    
    /**
     * Returns the attributes that are queried.
     * 
     * @return the attributes that are queried.
     * 
     * @ensure return.isFrozen() : "The attributes are frozen.";
     * @ensure return.isNotEmpty() : "The attributes are not empty.";
     */
    public @Nonnull ReadonlyAttributeSet getAttributes() {
        return attributes;
    }
    
    
    /**
     * Stores the required permissions for this method.
     * 
     * @invariant requiredPermissions.isFrozen() : "The required permissions are frozen.";
     */
    private final @Nonnull ReadonlyAgentPermissions requiredPermissions;
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return requiredPermissions;
    }
    
    
    @Pure
    @Override
    public @Nonnull Class<AttributesReply> getReplyClass() {
        return AttributesReply.class;
    }
    
    @Override
    public @Nonnull AttributesReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert hasSignature() : "This handler has a signature.";
        
        // TODO:
        /*
        AgentPermissions authorization = null;
        if (signature.getClient() != null) authorization = host.getAuthorization(connection, vid, signature.getClient());

        List<Block> types = new ListWrapper(element).getElements();
        List<Block> declarations = new ArrayList<Block>(types.size());

        for (Block type : types) {
            String identifier = new StringWrapper(type).getString();
            if (!Identifier.isValid(identifier)) throw new InvalidEncodingException("The identifier of a requested type is invalid.");
            long semanticType = Mapper.getVid(identifier);
            if (!Category.isSemanticType(semanticType)) throw new InvalidEncodingException("The identifiers of the requested types have to denote semantic types.");
            Pair<Block, String> pair = host.getAttribute(connection, vid, semanticType);
            if (pair == null) {
                declarations.add(Block.EMPTY);
            } else {
                Block attribute = pair.getValue0();
                String visibility = pair.getValue1();

                if (visibility == null) {
                    declarations.add(new TupleWrapper(new Block[]{attribute, Block.EMPTY}).toBlock());
                } else {
                    if (authorization != null) {
                        authorization.checkRead(semanticType);
                        declarations.add(new TupleWrapper(new Block[]{attribute, new StringWrapper(visibility).toBlock()}).toBlock());
                    } else {
                        Credential.checkRead(signature.getCredentials(), semanticType);
                        if (Expression.parse(visibility, connection, host, vid).matches(signature.getCredentials())) {
                            declarations.add(new TupleWrapper(new Block[]{attribute, Block.EMPTY}).toBlock());
                        } else {
                            declarations.add(Block.EMPTY);
                        }
                    }
                }
            }
        }

        return new ListWrapper(declarations).toBlock();
        */
        
        throw new SQLException();
//        return new AttributesReply();
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Method.Factory {
        
        static { Method.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AttributesQuery(entity, signature, recipient, block);
        }
        
    }
    
}
