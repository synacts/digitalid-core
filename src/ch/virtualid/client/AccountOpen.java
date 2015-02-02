package ch.virtualid.client;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.contact.Context;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.Response;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Opens a new account with the given category and client.
 * (This class inherits directly from the action class because no entity can be given.)
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for the action to open an account.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class AccountOpen extends Action {
    
    /**
     * Stores the semantic type {@code open.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("open.account@virtualid.ch").load(TupleWrapper.TYPE, Category.TYPE, Agent.NUMBER, Client.NAME, Client.ICON);
    
    
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
     * Stores the icon of the client agent.
     * 
     * @invariant Client.isValid(icon) : "The icon is valid.";
     */
    private final @Nonnull Image icon;
    
    /**
     * Creates an action to open a new account.
     * 
     * @param subject the identifier of the new account.
     * @param category the category of the new account.
     * @param client the client creating the new account.
     * 
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    @DoesNotCommit
    AccountOpen(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull Client client) throws SQLException, IOException, PacketException, ExternalException {
        super(null, subject, subject.getHostIdentifier());
        
        assert category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
        
        this.category = category;
        this.agentNumber = new SecureRandom().nextLong();
        this.commitment = client.getCommitment(subject);
        this.secret = client.getSecret();
        this.name = client.getName();
        this.icon = client.getIcon();
    }
    
    /**
     * Creates an action that decodes the given block.
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
    private AccountOpen(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new PacketException(PacketError.IDENTIFIER, "The host of the subject has to match the recipient for the action to open an account.");
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(4);
        
        this.category = Category.get(elements.getNotNull(0));
        if (!category.isInternalNonHostIdentity()) throw new InvalidEncodingException("The category has to denote an internal non-host identity.");
        
        this.agentNumber = new Int64Wrapper(elements.getNotNull(1)).getValue();
        
        if (!(signature instanceof ClientSignatureWrapper)) throw new InvalidEncodingException("The action to open an account has to be signed by a client.");
        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
        this.secret = null;
        
        this.name = new StringWrapper(elements.getNotNull(2)).getString();
        if (!Client.isValid(name)) throw new InvalidEncodingException("The name is invalid.");
        
        this.icon = new Image(elements.getNotNull(3));
        if (!Client.isValid(icon)) throw new InvalidEncodingException("The icon is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, category.toBlock(), new Int64Wrapper(Agent.NUMBER, agentNumber).toBlock(), new StringWrapper(Client.NAME, name).toBlock(), icon.toBlock().setType(Client.ICON)).toBlock();
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
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return AgentPermissions.GENERAL_WRITE;
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
    @DoesNotCommit
    public void initialize(@Nonnull NonHostEntity entity) throws SQLException {
        final @Nonnull Context context = Context.getRoot(entity);
        context.createForActions();
        
        final @Nonnull ClientAgent clientAgent = ClientAgent.get(entity, agentNumber, false);
        final @Nonnull Restrictions restrictions = new Restrictions(true, true, true, context);
        clientAgent.createForActions(AgentPermissions.GENERAL_WRITE, restrictions, commitment, name, icon);
    }
    
    @Override
    @DoesNotCommit
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
    @DoesNotCommit
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
    @DoesNotCommit
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        if (secret == null) throw new PacketException(PacketError.INTERNAL, "The secret may not be null for sending.");
        return new ClientRequest(new FreezableArrayList<Method>(this).freeze(), getSubject(), null, commitment.addSecret(secret)).send();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof AccountOpen) {
            final @Nonnull AccountOpen other = (AccountOpen) object;
            return this.category == other.category && this.agentNumber == other.agentNumber && this.commitment.equals(other.commitment) && Objects.equals(this.secret, other.secret) && this.name.equals(other.name) && this.icon.equals(other.icon);
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
        hash = 89 * hash + icon.hashCode();
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
    public @Nonnull BothModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @DoesNotCommit
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccountOpen(entity, signature, recipient, block);
        }
        
    }
    
}
