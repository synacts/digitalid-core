package ch.virtualid.handler.query.external;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.ReadonlyAttributeSet;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.AttributesReply;
import ch.virtualid.handler.reply.query.CoreServiceQueryReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries the given attributes from the given subject.
 * 
 * @see AttributesReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.2
 */
public final class AttributesQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.attribute@virtualid.ch").load(ListWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    private @Nonnull ReadonlyAttributeSet attributes;
    
    /**
     * Creates an external query to retrieve the given attributes from the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     */
    public AttributesQuery(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull ReadonlyAttributeSet attributes) {
        super(role, subject);
        
        this.attributes = attributes;
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates an external query that decodes the given block.
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
    private AttributesQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        // TODO: Decode the given block.
        throw new InvalidEncodingException("TODO");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new ListWrapper(TYPE).toBlock(); // TODO
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Retrieves the attributes {}."; // TODO
    }
    
    
    /**
     * Stores the required permissions for this method.
     */
    private final @Nonnull ReadonlyAgentPermissions requiredPermissions;
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return requiredPermissions;
    }
    
    
    @Override
    public @Nonnull AttributesReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert hasSignature() : "This handler has a signature.";
        
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
    
    @Override
    public Class<? extends CoreServiceQueryReply> getReplyClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, IdentityNotFoundException, InvalidDeclarationException {
            return new AttributesQuery(entity, signature, recipient, block);
        }
        
    }
    
}
