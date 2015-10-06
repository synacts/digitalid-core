package net.digitalid.core.client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.HasSubject;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.collections.freezable.FreezableArrayList;
import net.digitalid.collections.readonly.ReadOnlyArray;
import net.digitalid.core.contact.Context;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.system.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.ActionReply;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.data.StateModule;
import net.digitalid.core.packet.ClientRequest;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.data.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ClientSignatureWrapper;
import net.digitalid.core.wrappers.Int64Wrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.StringWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Opens a new account with the given category and client.
 * (This class inherits directly from the action class because no entity can be given.)
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for the action to open an account.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.9
 */
@Immutable
public final class AccountOpen extends Action {
    
    /**
     * Stores the semantic type {@code open.account@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("open.account@core.digitalid.net").load(TupleWrapper.TYPE, Category.TYPE, Agent.NUMBER, Client.NAME);
    
    
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
    AccountOpen(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull Client client) throws SQLException, IOException, PacketException, ExternalException {
        super(null, subject, subject.getHostIdentifier());
        
        assert category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
        
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
    private AccountOpen(@Nonnull Entity entity, @Nonnull @HasSubject SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull @BasedOn("open.account@core.digitalid.net") Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new PacketException(PacketError.IDENTIFIER, "The host of the subject has to match the recipient for the action to open an account.");
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getNonNullableElements(3);
        
        this.category = Category.get(elements.getNonNullable(0));
        if (!category.isInternalNonHostIdentity()) throw new InvalidEncodingException("The category has to denote an internal non-host identity.");
        
        this.agentNumber = new Int64Wrapper(elements.getNonNullable(1)).getValue();
        
        if (!(signature instanceof ClientSignatureWrapper)) throw new InvalidEncodingException("The action to open an account has to be signed by a client.");
        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
        this.secret = null;
        
        this.name = new StringWrapper(elements.getNonNullable(2)).getString();
        if (!Client.isValidName(name)) throw new InvalidEncodingException("The name '" + name + "' is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, category.toBlock(), new Int64Wrapper(Agent.NUMBER, agentNumber).toBlock(), new StringWrapper(Client.NAME, name).toBlock()).toBlock();
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
    public @Nonnull ReadOnlyAgentPermissions getAuditPermissions() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return Restrictions.MAX;
    }
    
    
    /**
     * Creates the root context and the client agent for the given entity.
     * 
     * @param entity the entity which is to be initialized.
     */
    @NonCommitting
    public void initialize(@Nonnull NonHostEntity entity) throws SQLException {
        final @Nonnull Context context = Context.getRoot(entity);
        context.createForActions();
        context.replaceName("New Context", "Root Context");
        
        final @Nonnull ClientAgent clientAgent = ClientAgent.get(entity, agentNumber, false);
        final @Nonnull Restrictions restrictions = new Restrictions(true, true, true, context);
        clientAgent.createForActions(FreezableAgentPermissions.GENERAL_WRITE, restrictions, commitment, name);
    }
    
    @Override
    @NonCommitting
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull InternalIdentifier subject = getSubject();
        if (subject.isMapped()) throw new PacketException(PacketError.IDENTIFIER, "The account with the identifier " + subject + " already exists.");
        
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
    public void executeOnClient() throws SQLException {
        throw new ShouldNeverHappenError("The action to open an account should never be executed on a client.");
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
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        if (secret == null) throw new PacketException(PacketError.INTERNAL, "The secret may not be null for sending.");
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccountOpen(entity, signature, recipient, block);
        }
        
    }
    
}
