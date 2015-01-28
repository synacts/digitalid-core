package ch.virtualid.credential;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
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
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.host.Host;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalPerson;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.Packet;
import ch.virtualid.service.CoreServiceInternalQuery;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CredentialsSignatureWrapper;
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
     * 
     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
     */
    CredentialInternalQuery(@Nonnull Role role, @Nonnull RandomizedAgentPermissions permissions) {
        super(role);
        
        assert role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
        
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
     * 
     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
     */
    CredentialInternalQuery(@Nonnull NonNativeRole role, @Nonnull RandomizedAgentPermissions permissions, @Nonnull BigInteger value) {
        super(role.getRecipient());
        
        assert role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
        
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
        super(entity, signature, recipient);
        
        if (!(entity.getIdentity() instanceof InternalPerson)) throw new PacketException(PacketError.IDENTIFIER, "An identity- or role-based credential can only be requested for internal persons.");
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.permissions = new RandomizedAgentPermissions(tuple.getElementNotNull(0));
        if (tuple.isElementNull(1)) this.relation = null;
        else this.relation = IdentityClass.create(tuple.getElementNotNull(1)).toSemanticType().checkIsRoleType();
        if (signature instanceof ClientSignatureWrapper) this.value = ((ClientSignatureWrapper) signature).getCommitment().getValue().getValue();
        else if (signature instanceof CredentialsSignatureWrapper) this.value = ((CredentialsSignatureWrapper) signature).getValue();
        else throw new PacketException(PacketError.SIGNATURE, "A credential request must be signed by a client or with credentials.");
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
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        if (relation == null) return Restrictions.MIN;
        else return Restrictions.ROLE;
    }
    
    
    @Override
    protected @Nonnull CredentialReply executeOnHost(@Nonnull Agent agent) throws SQLException {
        final @Nonnull Restrictions restrictions = agent.getRestrictions();
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        final @Nonnull NonHostAccount account = getNonHostAccount();
        final @Nonnull Host host = account.getHost();
        
        final @Nonnull Time issuance = signature instanceof CredentialsSignatureWrapper ? ((CredentialsSignatureWrapper) signature).getCredentials().getNotNull(0).getIssuance() : signature.getTimeNotNull().roundDown(Time.HALF_HOUR);
        
        try {
            final @Nonnull PublicKey publicKey = host.getPublicKeyChain().getKey(issuance);
            final @Nonnull PrivateKey privateKey = host.getPrivateKeyChain().getKey(issuance);
            final @Nonnull Group group = privateKey.getCompositeGroup();
            
            assert value != null : "See the constructor.";
            final @Nonnull Element f = group.getElement(value);
            final @Nonnull Exponent i = new Exponent(new BigInteger(Parameters.HASH, new SecureRandom()));
            final @Nonnull Exponent v = new Exponent(restrictions.toBlock().getHash());
            final @Nonnull Exponent o = new Exponent(Credential.getExposed(account.getIdentity(), issuance, permissions, relation, null).getHash());
            final @Nonnull Exponent e = new Exponent(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            
            final @Nonnull Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(o).inverse()).pow(e.inverse(group)).inverse();
            
            // TODO: It is important that the issuing host keeps track of all issued credentials for up to a year. (Store the signature, restrictions (or just v?), (c), e and i, with i being the primary key?)
            
            return new CredentialReply(account, restrictions, c, e, i);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("No key was found for the time of the signature.");
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
    
}
