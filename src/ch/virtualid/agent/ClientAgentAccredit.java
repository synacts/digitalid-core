package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.packet.Response;
import ch.virtualid.password.Password;
import ch.virtualid.service.CoreServiceInternalAction;
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
 * Accredits a {@link ClientAgent client agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientAgentAccredit extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code accredit.client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("accredit.client.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, AgentPermissions.TYPE, Client.NAME, Client.ICON, Password.TYPE);
    
    
    /**
     * Stores the client agent of this action.
     * 
     * @invariant clientAgent.isRemoved() : "The client agent is removed.";
     */
    private final @Nonnull ClientAgent clientAgent;
    
    /**
     * Stores the permissions of the client agent.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     */
    private final @Nonnull ReadonlyAgentPermissions permissions;
    
    /**
     * Stores the commitment of the client agent.
     */
    private final @Nonnull Commitment commitment;
    
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
     * Stores the password of the subject.
     * 
     * @invariant Password.isValid(password) : "The password is valid.";
     */
    private final @Nonnull String password;
    
    /**
     * Creates an internal action to accredit the client agent given by the role.
     * 
     * @param role the role whose client agent is to be accredited.
     * @param password the password needed to accredit the client agent.
     * 
     * @require Password.isValid(password) : "The password is valid.";
     */
    public ClientAgentAccredit(@Nonnull NativeRole role, @Nonnull String password) throws SQLException, IOException, PacketException, ExternalException {
        super(role);
        
        assert Password.isValid(password) : "The password is valid.";
        
        final @Nonnull Client client = role.getClient();
        this.clientAgent = ClientAgent.get(role, role.getAgent().getNumber(), true);
        this.permissions = client.getPreferredPermissions();
        this.commitment = client.getCommitment(role.getIdentity().getAddress());
        this.name = client.getName();
        this.icon = client.getIcon();
        this.password = password;
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
    private ClientAgentAccredit(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(5);
        
        this.clientAgent = Agent.get(entity.toNonHostEntity(), elements.getNotNull(0)).toClientAgent();
        if (clientAgent.isNotRemoved()) throw new InvalidEncodingException("The client agent has to be removed.");
        
        this.permissions = new AgentPermissions(elements.getNotNull(1)).freeze();
        
        if (!(signature instanceof ClientSignatureWrapper)) throw new InvalidEncodingException("The action to accredit a client agent has to be signed by a client.");
        this.commitment = ((ClientSignatureWrapper) signature).getCommitment();
        
        this.name = new StringWrapper(elements.getNotNull(2)).getString();
        if (!Client.isValid(name)) throw new InvalidEncodingException("The name is invalid.");
        
        this.icon = new Image(elements.getNotNull(3));
        if (!Client.isValid(icon)) throw new InvalidEncodingException("The icon is invalid.");
        
        this.password = new StringWrapper(elements.getNotNull(4)).getString();
        if (!Password.isValid(password)) throw new InvalidEncodingException("The password is invalid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, clientAgent.toBlock(), permissions.toBlock(), new StringWrapper(Client.NAME, name).toBlock(), icon.toBlock().setType(Client.ICON), new StringWrapper(Password.TYPE, password).toBlock()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Accredits the client agent with the number " + clientAgent + ".";
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return false;
    }
    
    @Override
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull Response response = Method.send(new FreezableArrayList<Method>(this).freeze(), null);
        response.checkReply(0);
        return response;
    }
    
    
    @Pure
    @Override
    public @Nonnull Agent getAuditAgent() {
        return clientAgent;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        clientAgent.createForActions(permissions, new Restrictions(true, false, false, Context.getRoot(getNonHostEntity())), commitment, name, icon);
    }
    
    @Override
    public void executeOnHostInternalAction() throws PacketException, SQLException {
        if (!Password.get(getNonHostAccount()).getValue().equals(password)) throw new PacketException(PacketError.AUTHORIZATION, "The password is not correct.");
        executeOnBoth();
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    public @Nullable InternalAction getReverse() {
        return null;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof ClientAgentAccredit) {
            final @Nonnull ClientAgentAccredit other = (ClientAgentAccredit) object;
            return this.clientAgent.equals(other.clientAgent) && this.permissions.equals(other.permissions) && this.commitment.equals(other.commitment) && this.name.equals(other.name) && this.icon.equals(other.icon) && this.password.equals(other.password);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + clientAgent.hashCode();
        hash = 89 * hash + permissions.hashCode();
        hash = 89 * hash + commitment.hashCode();
        hash = 89 * hash + name.hashCode();
        hash = 89 * hash + icon.hashCode();
        hash = 89 * hash + password.hashCode();
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
            return new ClientAgentAccredit(entity, signature, recipient, block);
        }
        
    }
    
}
