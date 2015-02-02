package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
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
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the name of a {@link ClientAgent client agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class ClientAgentNameReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.name.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_NAME = SemanticType.create("old.name.client.agent@virtualid.ch").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code new.name.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_NAME = SemanticType.create("new.name.client.agent@virtualid.ch").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code replace.name.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("replace.name.client.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_NAME, NEW_NAME);
    
    
    /**
     * Stores the client agent of this action.
     */
    private final @Nonnull ClientAgent clientAgent;
    
    /**
     * Stores the old name of the client agent.
     * 
     * @invariant Client.isValid(oldName) : "The old name is valid.";
     */
    private final @Nonnull String oldName;
    
    /**
     * Stores the new name of the client agent.
     * 
     * @invariant Client.isValid(newName) : "The new name is valid.";
     */
    private final @Nonnull String newName;
    
    /**
     * Creates an internal action to replace the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be replaced.
     * @param oldName the old name of the given client agent.
     * @param newName the new name of the given client agent.
     * 
     * @require clientAgent.isOnClient() : "The client agent is on a client.";
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    ClientAgentNameReplace(@Nonnull ClientAgent clientAgent, @Nonnull String oldName, @Nonnull String newName) {
        super(clientAgent.getRole());
        
        assert Client.isValid(oldName) : "The old name is valid.";
        assert Client.isValid(newName) : "The new name is valid.";
        
        this.clientAgent = clientAgent;
        this.oldName = oldName;
        this.newName = newName;
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
    private ClientAgentNameReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.clientAgent = Agent.get(entity.toNonHostEntity(), elements.getNotNull(0)).toClientAgent();
        this.oldName = new StringWrapper(elements.getNotNull(1)).getString();
        if (!Client.isValid(oldName)) throw new InvalidEncodingException("The old name is invalid.");
        this.newName = new StringWrapper(elements.getNotNull(2)).getString();
        if (!Client.isValid(newName)) throw new InvalidEncodingException("The new name is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, clientAgent, new StringWrapper(OLD_NAME, oldName), new StringWrapper(NEW_NAME, newName)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the name '" + oldName + "' with '" + newName + "' of the client agent with the number " + clientAgent + ".";
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
    protected void executeOnBoth() throws SQLException {
        clientAgent.replaceName(oldName, newName);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ClientAgentNameReplace && ((ClientAgentNameReplace) action).clientAgent.equals(clientAgent);
    }
    
    @Pure
    @Override
    public @Nonnull ClientAgentNameReplace getReverse() {
        return new ClientAgentNameReplace(clientAgent, newName, oldName);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof ClientAgentNameReplace) {
            final @Nonnull ClientAgentNameReplace other = (ClientAgentNameReplace) object;
            return this.clientAgent.equals(other.clientAgent) && this.oldName.equals(other.oldName) && this.newName.equals(other.newName);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + clientAgent.hashCode();
        hash = 89 * hash + oldName.hashCode();
        hash = 89 * hash + newName.hashCode();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new ClientAgentNameReplace(entity, signature, recipient, block);
        }
        
    }
    
}
