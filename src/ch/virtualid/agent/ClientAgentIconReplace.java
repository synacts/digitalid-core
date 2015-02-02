package ch.virtualid.agent;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the icon of a {@link ClientAgent client agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class ClientAgentIconReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.icon.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_ICON = SemanticType.create("old.icon.client.agent@virtualid.ch").load(Image.TYPE);
    
    /**
     * Stores the semantic type {@code new.icon.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_ICON = SemanticType.create("new.icon.client.agent@virtualid.ch").load(Image.TYPE);
    
    /**
     * Stores the semantic type {@code replace.icon.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("replace.icon.client.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_ICON, NEW_ICON);
    
    
    /**
     * Stores the client agent of this action.
     */
    private final @Nonnull ClientAgent clientAgent;
    
    /**
     * Stores the old icon of the client agent.
     * 
     * @invariant Client.isValid(oldIcon) : "The old icon is valid.";
     */
    private final @Nonnull Image oldIcon;
    
    /**
     * Stores the new icon of the client agent.
     * 
     * @invariant Client.isValid(newIcon) : "The new icon is valid.";
     */
    private final @Nonnull Image newIcon;
    
    /**
     * Creates an internal action to replace the icon of the given client agent.
     * 
     * @param clientAgent the client agent whose icon is to be replaced.
     * @param oldIcon the old icon of the given client agent.
     * @param newIcon the new icon of the given client agent.
     * 
     * @require clientAgent.isOnClient() : "The client agent is on a client.";
     * @require Client.isValid(oldIcon) : "The old icon is valid.";
     * @require Client.isValid(newIcon) : "The new icon is valid.";
     */
    ClientAgentIconReplace(@Nonnull ClientAgent clientAgent, @Nonnull Image oldIcon, @Nonnull Image newIcon) {
        super(clientAgent.getRole());
        
        assert Client.isValid(oldIcon) : "The old icon is valid.";
        assert Client.isValid(newIcon) : "The new icon is valid.";
        
        this.clientAgent = clientAgent;
        this.oldIcon = oldIcon;
        this.newIcon = newIcon;
    }
    
    /**
     * Creates an internal action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @DoesNotCommit
    private ClientAgentIconReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.clientAgent = Agent.get(entity.toNonHostEntity(), elements.getNotNull(0)).toClientAgent();
        this.oldIcon = new Image(elements.getNotNull(1));
        if (!Client.isValid(oldIcon)) throw new InvalidEncodingException("The old icon is invalid.");
        this.newIcon = new Image(elements.getNotNull(2));
        if (!Client.isValid(newIcon)) throw new InvalidEncodingException("The new icon is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, clientAgent.toBlock(), oldIcon.toBlock().setType(OLD_ICON), newIcon.toBlock().setType(NEW_ICON)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the icon of the client agent with the number " + clientAgent + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgent() {
        return clientAgent;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getAuditAgent() {
        return clientAgent;
    }
    
    
    @Override
    @DoesNotCommit
    protected void executeOnBoth() throws SQLException {
        clientAgent.replaceIcon(oldIcon, newIcon);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ClientAgentIconReplace && ((ClientAgentIconReplace) action).clientAgent.equals(clientAgent);
    }
    
    @Pure
    @Override
    public @Nonnull ClientAgentIconReplace getReverse() {
        return new ClientAgentIconReplace(clientAgent, newIcon, oldIcon);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof ClientAgentIconReplace) {
            final @Nonnull ClientAgentIconReplace other = (ClientAgentIconReplace) object;
            return this.clientAgent.equals(other.clientAgent) && this.oldIcon.equals(other.oldIcon) && this.newIcon.equals(other.newIcon);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + clientAgent.hashCode();
        hash = 89 * hash + oldIcon.hashCode();
        hash = 89 * hash + newIcon.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return AgentModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @DoesNotCommit
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new ClientAgentIconReplace(entity, signature, recipient, block);
        }
        
    }
    
}
