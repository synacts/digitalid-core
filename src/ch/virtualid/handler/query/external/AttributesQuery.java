package ch.virtualid.handler.query.external;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.AttributesReply;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
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
    
    
    /**
     * Creates an external query to retrieve the given attributes from the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     */
    public AttributesQuery(@Nullable Role role, @Nonnull Identity subject) {
        super(role, subject.getAddress());
        
        // TODO: Include the list of attributes.
    }
    
    /**
     * Creates an external query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private AttributesQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        // TODO: Decode the given block.
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
        assert getSignature() != null : "The signature of this handler is not null.";
        
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
        
        return new AttributesReply();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
            return new AttributesQuery(entity, signature, recipient, block);
        }
        
    }
    
}
