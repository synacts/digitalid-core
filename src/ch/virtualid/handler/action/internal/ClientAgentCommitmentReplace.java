package ch.virtualid.handler.action.internal;

import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Commitment;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.both.Agents;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the {@Commitment commitment} of a {@link ClientAgent client agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientAgentCommitmentReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.commitment.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_COMMITMENT = SemanticType.create("old.commitment.client.agent@virtualid.ch").load(Commitment.TYPE);
    
    /**
     * Stores the semantic type {@code new.commitment.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_COMMITMENT = SemanticType.create("new.commitment.client.agent@virtualid.ch").load(Commitment.TYPE);
    
    /**
     * Stores the semantic type {@code replace.commitment.client.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("replace.commitment.client.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_COMMITMENT, NEW_COMMITMENT);
    
    
    /**
     * Stores the client agent of this action.
     */
    private final @Nonnull ClientAgent clientAgent;
    
    /**
     * Stores the old commitment of the client agent.
     */
    private final @Nonnull Commitment oldCommitment;
    
    /**
     * Stores the new commitment of the client agent.
     */
    private final @Nonnull Commitment newCommitment;
    
    /**
     * Creates an internal action to replace the commitment of the given client agent.
     * 
     * @param clientAgent the client agent whose commitment is to be replaced.
     * @param oldCommitment the old commitment of the given client agent.
     * @param newCommitment the new commitment of the given client agent.
     * 
     * @require clientAgent.isOnClient() : "The client agent is on a client.";
     */
    public ClientAgentCommitmentReplace(@Nonnull ClientAgent clientAgent, @Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) {
        super(clientAgent.getRole());
        
        this.clientAgent = clientAgent;
        this.oldCommitment = oldCommitment;
        this.newCommitment = newCommitment;
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
    private ClientAgentCommitmentReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.clientAgent = Agent.get(entity.toNonHostEntity(), elements.getNotNull(0)).toClientAgent();
        this.oldCommitment = new Commitment(elements.getNotNull(1));
        this.newCommitment = new Commitment(elements.getNotNull(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(clientAgent.toBlock(), oldCommitment.toBlock().setType(OLD_COMMITMENT), newCommitment.toBlock().setType(NEW_COMMITMENT)).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replaces the commitment " + oldCommitment + " with " + newCommitment + " of the client agent with the number " + clientAgent + ".";
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && (!isOnClient() || !getRole().getAgent().equals(clientAgent));
    }
    
    @Pure
    @Override
    public @Nullable PublicKey getPublicKey() {
        return null; // The commitment can be replaced with a commitment to an inactive public key of the recipient.
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
        clientAgent.replaceCommitment(oldCommitment, newCommitment);
    }
    
    @Pure
    @Override
    public @Nonnull ClientAgentCommitmentReplace getReverse() {
        return new ClientAgentCommitmentReplace(clientAgent, newCommitment, oldCommitment);
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return Agents.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new ClientAgentCommitmentReplace(entity, signature, recipient, block);
        }
        
    }
    
}
