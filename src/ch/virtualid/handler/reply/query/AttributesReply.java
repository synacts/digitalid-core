package ch.virtualid.handler.reply.query;

import ch.virtualid.entity.Account;
import ch.virtualid.handler.query.external.AttributesQuery;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import javax.annotation.Nonnull;

/**
 * Replies the queried attributes of the given subject that are accessible by the requester.
 * 
 * @see AttributesQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AttributesReply extends CoreServiceQueryReply {
    
    /**
     * Creates a new reply with the given connection, entity, signature and block.
     * 
     * @param account the account to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected AttributesReply(@Nonnull Account account, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(account);
    }

    @Override
    public SemanticType getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Block toBlock() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
