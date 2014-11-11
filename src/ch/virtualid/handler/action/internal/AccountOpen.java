package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.Service;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.Response;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Opens a new account with the given category.
 * (This class inherits directly from the action class because no entity can be given.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public final class AccountOpen extends Action {
    
    /**
     * Stores the semantic type {@code open.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("open.account@virtualid.ch").load(TupleWrapper.TYPE, Category.TYPE, Client.NAME, Client.ICON);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the category of the new account.
     * 
     * @invariant category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    private final @Nonnull Category category;
    
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
     * @invariant name.length() <= Client.NAME_LENGTH : "The name has at most the indicated length.";
     */
    private final @Nonnull String name;
    
    /**
     * Stores the icon of the client agent.
     * 
     * @invariant icon.isSquare(Client.ICON_SIZE) : "The icon has the specified size.";
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
    public AccountOpen(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull Client client) throws SQLException, IOException, PacketException, ExternalException {
        super(null, subject, subject.getHostIdentifier());
        
        assert category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
        
        this.category = category;
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
    private AccountOpen(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        
        this.category = Category.get(elements.getNotNull(0));
        if (!category.isInternalNonHostIdentity()) throw new InvalidEncodingException("The category has to denote an internal non-host identity.");
        
        if (!(signature instanceof ClientSignatureWrapper)) throw new InvalidEncodingException("The action to open an account has to be signed by a client.");
        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
        this.secret = null;
        
        this.name = new StringWrapper(elements.getNotNull(1)).getString();
        if (name.length() > Client.NAME_LENGTH) throw new InvalidEncodingException("The name must have at most the indicated length.");
        
        this.icon = new Image(elements.getNotNull(2));
        if (icon.isSquare(Client.ICON_SIZE)) throw new InvalidEncodingException("The icon must have the specified size.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(category.toBlock(), new StringWrapper(Client.NAME, name).toBlock(), icon.toBlock().setType(Client.ICON)).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Opens a new account with the category '" + category.name() + "'.";
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
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }

    @Override
    public ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    
    @Override
    public @Nullable Class<? extends ActionReply> getReplyClass() {
        return null;
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert hasSignature() : "This handler has a signature.";
        
        final @Nonnull InternalIdentifier subject = getSubject();
        if (subject.isMapped()) throw new PacketException(PacketError.IDENTIFIER, "The account with the identifier " + subject + " already exists.");
        
        // TODO: Include the resctriction mechanisms like the tokens.
        
        final @Nonnull InternalIdentity identity = (InternalIdentity) Mapper.mapIdentity(subject, category, null);
        final @Nonnull Account account = new Account(((Account) getEntityNotNull()).getHost(), identity);
        
        // TODO: Authorize the client agent here with maximal permissions!
        final @Nonnull ReadonlyAgentPermissions permissions = new AgentPermissions(AgentPermissions.GENERAL, true).freeze();
        // Agents.accreditClient(identity, commitment, name, authorization);
        
        // TODO: Does the root context need to be created first?
        final @Nonnull Restrictions restrictions = new Restrictions(true, true, true, Context.get(account, Context.ROOT));
        // Agents.authorizeClient(identity, commitment, restrictions, authorization);
        
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        throw new ShouldNeverHappenError("The action to open an account should never be executed on a client.");
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return false;
    }
    
    @Override
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        if (secret == null) throw new PacketException(PacketError.INTERNAL, "The secret may not be null for sending.");
        return new ClientRequest(new FreezableArrayList<Method>(this).freeze(), getSubject(), null, commitment.addSecret(secret)).send();
    }
    
}
