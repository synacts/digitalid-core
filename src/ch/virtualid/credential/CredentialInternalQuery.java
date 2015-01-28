package ch.virtualid.credential;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Group;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonNativeRole;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.Packet;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.CoreServiceInternalQuery;
import ch.virtualid.service.Service;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Requests a new identity- or role-based credential with the given permissions and relation.
 * 
 * It is important that the issuing host keeps track of all issued credentials for up to a year.
 * All hidden elements need to be verifiably encrypted, so this class needs to override the send method.
 * 
 * @see CredentialReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
final class CredentialInternalQuery extends CoreServiceInternalQuery {
    
    /**
     * Stores the semantic type {@code query.internal.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("query.internal.credential@virtualid.ch").load(TupleWrapper.TYPE, RandomizedAgentPermissions.TYPE, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the permissions for which a credential is requested.
     */
    private final @Nonnull RandomizedAgentPermissions permissions;
    
    /**
     * Stores the relation of the role-based credential that is requested.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private final @Nullable SemanticType relation;
    
    /**
     * Stores either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    private final @Nullable BigInteger value;
    
    /**
     * Creates an internal query for a new identity-based credential with the given permissions.
     * 
     * @param role the role to which this handler belongs.
     * @param permissions the permissions for which a credential is requested.
     */
    CredentialInternalQuery(@Nonnull Role role, @Nonnull RandomizedAgentPermissions permissions) {
        super(role);
        
        this.permissions = permissions;
        this.relation = null;
        this.value = null;
    }
    
    /**
     * Creates an internal query for a new role-based credential with the given permissions.
     * 
     * @param role the role to which this handler belongs.
     * @param permissions the permissions for which a credential is requested.
     * @param value the value used for shortening an existing credential.
     */
    CredentialInternalQuery(@Nonnull NonNativeRole role, @Nonnull RandomizedAgentPermissions permissions, @Nonnull BigInteger value) {
        super(role.getRecipient());
        
        this.permissions = permissions;
        this.relation = role.getRelation();
        this.value = value;
    }
    
    /**
     * Creates an internal query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private CredentialInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity.toNonHostEntity(), signature, recipient);
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.permissions = new RandomizedAgentPermissions(tuple.getElementNotNull(0));
        if (tuple.isElementNull(1)) this.relation = null;
        else this.relation = IdentityClass.create(tuple.getElementNotNull(1)).toSemanticType().checkIsRoleType();
        // TODO: What about the value?
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, permissions, (relation != null ? relation.toBlockable(SemanticType.IDENTIFIER) : null)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Requests an identity- or role-based credential.";
    }
    
    
    @Pure
    @Override
    public boolean isLodged() {
        return true;
    }
    
    @Pure
    @Override
    public @Nullable BigInteger getValue() {
        return value;
    }
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return permissions.getPermissionsNotNull();
    }
    
    
    @Override
    public @Nonnull CredentialReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull Service service = module.getService();
        final @Nonnull NonHostAccount account = getNonHostAccount();
        if (module.getService().equals(CoreService.SERVICE)) {
            final @Nonnull Agent agent = getSignatureNotNull().getAgentCheckedAndRestricted(account, null);
            return new CredentialReply(account, module.getState(account, agent.getPermissions(), agent.getRestrictions(), agent), service);
        } else {
            final @Nonnull Credential credential = getSignatureNotNull().toCredentialsSignatureWrapper().getCredentials().getNotNull(0);
            final @Nullable ReadonlyAgentPermissions permissions = credential.getPermissions();
            final @Nullable Restrictions restrictions = credential.getRestrictions();
            if (permissions == null || restrictions == null) throw new PacketException(PacketError.AUTHORIZATION, "For state queries, neither the permissions nor the restrictions may be null.");
            return new CredentialReply(account, module.getState(account, permissions, restrictions, null), service);
        }
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof CredentialInternalQuery && ((CredentialInternalQuery) reply).state.getType().equals(module.getStateFormat());
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof CredentialInternalQuery && this.module.equals(((CredentialInternalQuery) object).module);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + module.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new CredentialInternalQuery(entity, signature, recipient, block);
        }
        
    }
    
    
    /**
     * Obtains a credential for the given client on behalf of the given requester at the given issuer for the given authorization.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param requester the VID of a person on behalf of which the credential is to be obtained.
     * @param issuer the VID of a non-host at which the credential is to be obtained.
     * @param randomizedAuthorization the desired authorization in randomized form.
     * 
     * @return a credential for the given client on behalf of the given requester at the given issuer.
     * 
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
     */
    public static ClientCredential obtainCredential(Client client, long requester, long issuer, RandomizedAuthorization randomizedAuthorization) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
        assert randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
        
        PublicKey publicKey = new PublicKey(Request.getAttributeNotNullUnwrapped(Mapper.getHost(issuer), Vid.HOST_PUBLIC_KEY));
        Group group = publicKey.getCompositeGroup();
        
        if (issuer == requester) {
            SelfcontainedWrapper content = new SelfcontainedWrapper("request.credential.client@virtualid.ch", randomizedAuthorization.toBlock());
            Packet request = new Packet(content, Mapper.getIdentifier(Mapper.getHost(issuer)), new SymmetricKey(), Mapper.getIdentifier(issuer), 0, client.getSecret());
            Packet response = request.send();
            Block[] elements = new TupleWrapper(respogetContentstent().getElement()).getElementsNotNull(3);
            
            String identifier = respogetSignaturesture().getIdentifier();
            long time = reqgetSignaturesature().getSignatureTimeRoundedDown();
            Restrictions restrictions = Request.getRestrictions(client, issuer);
            Element c = group.getElement(elements[0]);
            Exponent e = group.getExponent(elements[1]);
            Exponent b = group.getExponent(BigInteger.ZERO);
            Exponent u = group.getExponent(client.getSecret());
            Exponent i = group.getExponent(elements[2]);
            Exponent v = group.getExponent(restrictions.toBlock().getHash());
            
            return new Credential(publicKey, identifier, time, randomizedAuthorization, null, restrictions, c, e, b, u, i, v);
        } else {
            throw new UnsupportedOperationException("Credentials for attribute-based access control are not yet supported!");
        }
    }
    
    /**
     * The handler for requests of type {@code request.credential.client@virtualid.ch}.
     */
    private static class ObtainCredential extends Handler {

        private ObtainCredential() throws Exception { super("request.credential.client@virtualid.ch", "response.credential.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            if (!Category.isPerson(vid)) throw new PacketException(PacketException.IDENTIFIER);
            
            BigInteger commitment = signature.getClient();
            Restrictions restrictions = host.getRestrictions(connection, vid, commitment);
            AgentPermissions authorization = host.getAuthorization(connection, vid, commitment);
            if (restrictions == null) throw new PacketException(PacketException.AUTHORIZATION);
            RandomizedAuthorization randomizedAuthorization = new RandomizedAuthorization(element);
            authorization.checkCover(randomizedAuthorization.getAuthorization());
            
            PublicKey publicKey = host.getPublicKey();
            PrivateKey privateKey = host.getPrivateKey();
            Group group = privateKey.getCompositeGroup();
            
            Element f = group.getElement(commitment);
            Exponent i = group.getExponent(new BigInteger(Parameters.HASH, new SecureRandom()));
            Exponent v = group.getExponent(restrictions.toBlock().getHash());
            Exponent o = group.getExponent(Credential.getExposed(Mapper.getIdentifier(vid), signature.getSignatureTimeRoundedDown(), randomizedAuthorization, null).getHash());
            Exponent e = group.getExponent(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            
            Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(o).inverse()).pow(e.inverse()).inverse();
            
            return new TupleWrapper(new Block[]{c.toBlock(), e.toBlock(), i.toBlock()}).toBlock();
        }
        
    }
    
}
