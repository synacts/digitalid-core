package net.digitalid.core.client;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.annotations.HasSubject;

import net.digitalid.core.conversion.wrappers.signature.ClientSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;

import net.digitalid.core.context.Context;

import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.dataservice.StateModule;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.NonHostEntity;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.ActionReply;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;

import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;

import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.identity.annotations.BasedOn;

import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

import net.digitalid.core.packet.ClientRequest;
import net.digitalid.core.packet.Response;

import net.digitalid.core.state.Service;

/**
 * Opens a new account with the given category and client.
 * (This class inherits directly from the action class because no entity can be given.)
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for the action to open an account.";
 */
@Immutable
public final class AccountOpen extends Action {
    
    /**
     * Stores the semantic type {@code open.account@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("open.account@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Category.TYPE, Agent.NUMBER, Client.NAME);
    
    
    /**
     * Stores the category of the new account.
     * 
     * @invariant category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    private final @Nonnull Category category;
    
    /**
     * Stores the number of the client agent.
     */
    private final long agentNumber;
    
    /**
     * Stores the commitment of the client agent.
     */
    private final @Nonnull Commitment commitment;
    
    /**
     * Stores the secret of the client agent.
     */
    private final @Nullable Exponent secret;
    
    /**
     * Stores the name of the client agent.
     * 
     * @invariant Client.isValid(name) : "The name is valid.";
     */
    private final @Nonnull String name;
    
    /**
     * Creates an action to open a new account.
     * 
     * @param subject the identifier of the new account.
     * @param category the category of the new account.
     * @param client the client creating the new account.
     * 
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    @NonCommitting
    AccountOpen(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull Client client) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(null, subject, subject.getHostIdentifier());
        
        Require.that(category.isInternalNonHostIdentity()).orThrow("The category denotes an internal non-host identity.");
        
        this.category = category;
        this.agentNumber = new Random().nextLong();
        this.commitment = client.getCommitment(subject);
        this.secret = client.getSecret();
        this.name = client.getName();
    }
    
    /**
     * Creates an action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     */
    @NonCommitting
    private AccountOpen(@Nonnull Entity entity, @Nonnull @HasSubject SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull @BasedOn("open.account@core.digitalid.net") Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The host of the subject has to match the recipient for the action to open an account."); }
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        
        this.category = Category.get(elements.getNonNullable(0));
        if (!category.isInternalNonHostIdentity()) { throw InternalException.get("The category has to denote an internal non-host identity but was " + category.name() + "."); }
        
        this.agentNumber = Integer64Wrapper.decode(elements.getNonNullable(1));
        
        if (!(signature instanceof ClientSignatureWrapper)) { throw InternalException.get("The action to open an account has to be signed by a client."); }
        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
        this.secret = null;
        
        this.name = StringWrapper.decodeNonNullable(elements.getNonNullable(2));
        if (!Client.isValidName(name)) { throw InvalidParameterValueException.get("name", name); }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, category.toBlock(), Integer64Wrapper.encode(Agent.NUMBER, agentNumber), StringWrapper.encodeNonNullable(Client.NAME, name));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Opens a new account with the category '" + category.name() + "'.";
    }
    
    
    /**
     * Returns the number of the client agent.
     * 
     * @return the number of the client agent.
     */
    public long getAgentNumber() {
        return agentNumber;
    }
    
    /**
     * Returns the commitment of the client agent.
     * 
     * @return the commitment of the client agent.
     */
    public @Nonnull Commitment getCommitment() {
        return commitment;
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return Restrictions.MAX;
    }
    
    
    /**
     * Creates the root context and the client agent for the given entity.
     * 
     * @param entity the entity which is to be initialized.
     */
    @NonCommitting
    public void initialize(@Nonnull NonHostEntity entity) throws DatabaseException {
        final @Nonnull Context context = Context.getRoot(entity);
        context.createForActions();
        context.replaceName("New Context", "Root Context");
        
        final @Nonnull ClientAgent clientAgent = ClientAgent.get(entity, agentNumber, false);
        final @Nonnull Restrictions restrictions = new Restrictions(true, true, true, context);
        clientAgent.createForActions(FreezableAgentPermissions.GENERAL_WRITE, restrictions, commitment, name);
    }
    
    @Override
    @NonCommitting
    public @Nullable ActionReply executeOnHost() throws RequestException, SQLException {
        final @Nonnull InternalIdentifier subject = getSubject();
        if (subject.isMapped()) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The account with the identifier " + subject + " already exists."); }
        
        // TODO: Include the resctriction mechanisms like the tokens.
        
        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) Mapper.mapIdentity(subject, category, null);
        final @Nonnull NonHostAccount account = NonHostAccount.get(getAccount().getHost(), identity);
        initialize(account);
        account.opened();
        return null;
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    @Override
    @NonCommitting
    public void executeOnClient() throws DatabaseException {
        throw ShouldNeverHappenError.get("The action to open an account should never be executed on a client.");
    }
    
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }

    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return false;
    }
    
    @Override
    @NonCommitting
    public @Nonnull Response send() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        if (secret == null) { throw InternalException.get("The secret may not be null for sending."); }
        return new ClientRequest(new FreezableArrayList<Method>(this).freeze(), getSubject(), null, commitment.addSecret(secret)).send();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof AccountOpen) {
            final @Nonnull AccountOpen other = (AccountOpen) object;
            return this.category == other.category && this.agentNumber == other.agentNumber && this.commitment.equals(other.commitment) && Objects.equals(this.secret, other.secret) && this.name.equals(other.name);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + category.hashCode();
        hash = 89 * hash + (int) (agentNumber ^ (agentNumber >>> 32));
        hash = 89 * hash + commitment.hashCode();
        hash = 89 * hash + Objects.hashCode(secret);
        hash = 89 * hash + name.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new AccountOpen(entity, signature, recipient, block);
        }
        
    }
    
}
