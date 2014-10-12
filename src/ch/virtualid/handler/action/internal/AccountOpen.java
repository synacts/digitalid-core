package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.packet.Audit;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
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
    
    /**
     * Stores the semantic type {@code open.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("open.account@virtualid.ch").load(todo);
    
    
    public AccountOpen() {
        
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
    
    /**
     * Opens a new account with the given identifier and category.
     * 
     * @param client the client to be authorized at the newly created VID.
     * @param identifier the identifier of the new account.
     * @param name the name of the client.
     * @param category the category of the new account.
     * @require client != null : "The client is not null.";
     * @require identifier != null : "The identifier is not null.";
     * @require Identifier.isValid(identifier) : "The identifier is valid.";
     * @require !Identifier.isHost(identifier) : "The identifier may not denote a host.";
     * @require name != null : "The name is not null.";
     * @require Category.isValid(category) : "The category is valid.";
     */
    public static void openAccount(Client client, String identifier, String name, byte category) throws Exception {
        assert client != null : "The client is not null.";
        assert identifier != null : "The identifier is not null.";
        assert Identifier.isValid(identifier) : "The identifier is valid.";
        assert !Identifier.isHost(identifier) : "The identifier may not denote a host.";
        assert name != null : "The name is not null.";
        assert Category.isValid(category) : "The category is valid.";
        
        Block[] elements = new Block[] {new Int8Wrapper(category).toBlock(), new StringWrapper(name).toBlock()};
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.open.account@virtualid.ch", new TupleWrapper(elements).toBlock());
        Packet response = new Packet(content, NonHostIdentifier.getHost(identifier), new SymmetricKey(), identifier, 0, client.getSecret()).send();
        client.setTimeOfLastRequest(Mapper.getVid(identifier), response.getSignatures().getTime());
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
