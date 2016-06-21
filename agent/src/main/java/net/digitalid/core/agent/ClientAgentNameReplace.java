package net.digitalid.core.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.client.Client;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.core.CoreServiceInternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.SemanticType;

import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

/**
 * Replaces the name of a {@link ClientAgent client agent}.
 */
@Immutable
final class ClientAgentNameReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.name.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_NAME = SemanticType.map("old.name.client.agent@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code new.name.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_NAME = SemanticType.map("new.name.client.agent@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code replace.name.client.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.name.client.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, OLD_NAME, NEW_NAME);
    
    
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
        
        Require.that(Client.isValidName(oldName)).orThrow("The old name is valid.");
        Require.that(Client.isValidName(newName)).orThrow("The new name is valid.");
        
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
    @NonCommitting
    private ClientAgentNameReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        this.clientAgent = Agent.get(entity.castTo(NonHostEntity.class), elements.getNonNullable(0)).castTo(ClientAgent.class);
        this.oldName = StringWrapper.decodeNonNullable(elements.getNonNullable(1));
        if (!Client.isValidName(oldName)) { throw InvalidParameterValueException.get("old name", oldName); }
        this.newName = StringWrapper.decodeNonNullable(elements.getNonNullable(2));
        if (!Client.isValidName(newName)) { throw InvalidParameterValueException.get("new name", newName); }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, clientAgent, StringWrapper.encodeNonNullable(OLD_NAME, oldName), StringWrapper.encodeNonNullable(NEW_NAME, newName));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the name '" + oldName + "' with '" + newName + "' of the client agent with the number " + clientAgent + ".";
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
    protected void executeOnBoth() throws DatabaseException {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
            return new ClientAgentNameReplace(entity, signature, recipient, block);
        }
        
    }
    
}
