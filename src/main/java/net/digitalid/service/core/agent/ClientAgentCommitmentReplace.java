package net.digitalid.service.core.agent;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.client.Commitment;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreServiceInternalAction;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Replaces the {@link Commitment commitment} of a {@link ClientAgent client agent}.
 */
@Immutable
public final class ClientAgentCommitmentReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.commitment.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_COMMITMENT = SemanticType.map("old.commitment.client.agent@core.digitalid.net").load(Commitment.TYPE);
    
    /**
     * Stores the semantic type {@code new.commitment.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_COMMITMENT = SemanticType.map("new.commitment.client.agent@core.digitalid.net").load(Commitment.TYPE);
    
    /**
     * Stores the semantic type {@code replace.commitment.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.commitment.client.agent@core.digitalid.net").load(TupleWrapper.TYPE, Agent.TYPE, OLD_COMMITMENT, NEW_COMMITMENT);
    
    
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
    @NonCommitting
    private ClientAgentCommitmentReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getNonNullableElements(3);
        this.clientAgent = Agent.get(entity.toNonHostEntity(), elements.getNonNullable(0)).toClientAgent();
        this.oldCommitment = new Commitment(elements.getNonNullable(1));
        this.newCommitment = new Commitment(elements.getNonNullable(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, clientAgent.toBlock(), oldCommitment.toBlock().setType(OLD_COMMITMENT), newCommitment.toBlock().setType(NEW_COMMITMENT)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
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
    public @Nonnull Agent getRequiredAgentToExecuteMethod() {
        return clientAgent;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgentToSeeAudit() {
        return clientAgent;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws AbortException {
        clientAgent.replaceCommitment(oldCommitment, newCommitment);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ClientAgentCommitmentReplace && ((ClientAgentCommitmentReplace) action).clientAgent.equals(clientAgent);
    }
    
    @Pure
    @Override
    public @Nonnull ClientAgentCommitmentReplace getReverse() {
        return new ClientAgentCommitmentReplace(clientAgent, newCommitment, oldCommitment);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof ClientAgentCommitmentReplace) {
            final @Nonnull ClientAgentCommitmentReplace other = (ClientAgentCommitmentReplace) object;
            return this.clientAgent.equals(other.clientAgent) && this.oldCommitment.equals(other.oldCommitment) && this.newCommitment.equals(other.newCommitment);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + clientAgent.hashCode();
        hash = 89 * hash + oldCommitment.hashCode();
        hash = 89 * hash + newCommitment.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return AgentModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException  {
            return new ClientAgentCommitmentReplace(entity, signature, recipient, block);
        }
        
    }
    
}
