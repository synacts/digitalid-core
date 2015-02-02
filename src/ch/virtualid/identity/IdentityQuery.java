package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.service.CoreServiceExternalQuery;
import ch.xdf.Block;
import ch.xdf.EmptyWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries the identity of the given subject.
 * 
 * @see IdentityReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class IdentityQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.identity@virtualid.ch").load(EmptyWrapper.TYPE);
    
    
    /**
     * Creates an identity query to retrieve the identity of the given subject.
     * 
     * @param subject the subject of this handler.
     */
    IdentityQuery(@Nonnull InternalNonHostIdentifier subject) {
        super(null, subject);
    }
    
    /**
     * Creates an identity query that decodes the given block.
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
    private IdentityQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, recipient);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new EmptyWrapper(TYPE).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Queries the identity.";
    }
    
    
    @Override
    public @Nonnull IdentityReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull InternalIdentifier subject = getSubject(); // The following exception should never be thrown as the condition is already checked in the packet class.
        if (!(subject instanceof InternalNonHostIdentifier)) throw new PacketException(PacketError.IDENTIFIER, "The identity may only be queried of non-host identities.");
        return new IdentityReply((InternalNonHostIdentifier) subject);
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof IdentityReply;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof IdentityQuery;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return protectedHashCode();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new IdentityQuery(entity, signature, recipient, block);
        }
        
    }
    
}
