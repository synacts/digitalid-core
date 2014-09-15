package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import javax.annotation.Nonnull;
import org.javatuples.Pair;

/**
 * Description.
 * 
 * Inherits directly from the action class because no entity can be given.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AccountOpen extends Action {
    
    public AccountOpen() {
        
    }
    
    /**
     * The handler for requests of type {@code request.open.account@virtualid.ch}.
     */
    private static class OpenAccount extends Handler {
        
        private OpenAccount() { super(SemanticType.ACCOUNT_OPEN_REQUEST, SemanticType.ACCOUNT_OPEN_RESPONSE); }
        
        @Override
        protected @Nonnull Pair<Block, Audit> handle(@Nonnull Connection connection, @Nonnull SignatureWrapper signature, @Nonnull Block element) throws PacketException, InvalidEncodingException {
            String identifier = signature.getIdentifier();
            
            Block[] elements = new TupleWrapper(element).getElementsNotNull(2);
            byte category = new Int8Wrapper(elements[0]).getValue();
            if (!Category.isValid(category)) throw new InvalidEncodingException("The stated category of the new account is valid.");
            String name = new StringWrapper(elements[1]).getString();
            if (name.length() > 255) throw new InvalidEncodingException("The name of the new account may be at most 255 bytes.");
            
            host.openAccount(connection, identifier, category, signature.getClient(), name);
            
            assert identifier.getHostIdentifier().equals(this.identifier) : "The host part of the identifier has to match this host.";
            assert name.length() <= 255 : "The name may have at most 255 characters.";

            @Nonnull NonHostIdentity identity = Mapper.mapIdentity(identifier, category).toNonHostIdentity();
            Restrictions restrictions = new Restrictions(0, true, 0l);
            AgentPermissions authorization = new AgentPermissions(SemanticType.CLIENT_GENERAL_AUTHORIZATION, true);
            accreditClient(connection, identity, commitment, name, authorization);
            authorizeClient(connection, identity, commitment, restrictions, authorization);
            return identity;
            
            return Block.EMPTY;
        }
        
    }
    
}
