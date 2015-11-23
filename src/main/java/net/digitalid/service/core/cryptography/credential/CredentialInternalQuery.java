package net.digitalid.service.core.cryptography.credential;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ClientSignatureWrapper;
import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.RandomizedAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.Group;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.cryptography.PrivateKey;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonNativeRole;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.handler.core.CoreServiceInternalQuery;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * Requests a new identity- or role-based credential with the given permissions and relation.
 * 
 * @see CredentialReply
 */
@Immutable
final class CredentialInternalQuery extends CoreServiceInternalQuery {
    
    /**
     * Stores the semantic type {@code query.internal.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("query.internal.credential@core.digitalid.net").load(TupleWrapper.XDF_TYPE, RandomizedAgentPermissions.TYPE, SemanticType.IDENTIFIER);
    
    
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
    @NonCommitting
    private CredentialInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        if (!(entity.getIdentity() instanceof InternalPerson)) { throw new PacketException(PacketErrorCode.IDENTIFIER, "An identity- or role-based credential can only be requested for internal persons."); }
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
        this.permissions = new RandomizedAgentPermissions(tuple.getNonNullableElement(0));
        if (tuple.isElementNull(1)) { this.relation = null; }
        else { this.relation = IdentityImplementation.create(tuple.getNonNullableElement(1)).castTo(SemanticType.class).checkIsRoleType(); }
        if (signature instanceof ClientSignatureWrapper) { this.value = ((ClientSignatureWrapper) signature).getCommitment().getValue().getValue(); }
        else if (signature instanceof CredentialsSignatureWrapper) { this.value = ((CredentialsSignatureWrapper) signature).getValue(); }
        else { throw new PacketException(PacketErrorCode.SIGNATURE, "A credential request must be signed by a client or with credentials."); }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, permissions, (relation != null ? relation.toBlockable(SemanticType.IDENTIFIER) : null));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
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
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return permissions.getPermissionsNotNull();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        if (relation == null) { return Restrictions.MIN; }
        else { return Restrictions.ROLE; }
    }
    
    
    @Override
    @NonCommitting
    protected @Nonnull CredentialReply executeOnHost(@Nonnull Agent agent) throws DatabaseException {
        final @Nonnull Restrictions restrictions = agent.getRestrictions();
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        final @Nonnull NonHostAccount account = getNonHostAccount();
        final @Nonnull Host host = account.getHost();
        
        final @Nonnull Time issuance = signature instanceof CredentialsSignatureWrapper ? ((CredentialsSignatureWrapper) signature).getCredentials().getNonNullable(0).getIssuance() : signature.getNonNullableTime().roundDown(Time.HALF_HOUR);
        
        try {
            final @Nonnull PublicKey publicKey = host.getPublicKeyChain().getKey(issuance);
            final @Nonnull PrivateKey privateKey = host.getPrivateKeyChain().getKey(issuance);
            final @Nonnull Group group = privateKey.getCompositeGroup();
            
            assert value != null : "See the constructor.";
            final @Nonnull Element f = group.getElement(value);
            final @Nonnull Exponent i = Exponent.get(new BigInteger(Parameters.HASH, new SecureRandom()));
            final @Nonnull Exponent v = Exponent.get(restrictions.toBlock().getHash());
            final @Nonnull Exponent o = Exponent.get(Credential.getExposed(account.getIdentity(), issuance, permissions, relation, null).getHash());
            final @Nonnull Exponent e = Exponent.get(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            
            final @Nonnull Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(o).inverse()).pow(e.inverse(group)).inverse();
            
            HostCredentialModule.store(account, e, i, v, signature);
            
            return new CredentialReply(account, publicKey, restrictions, issuance, c, e, i);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("No key was found for the time of the signature.");
        }
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof CredentialReply;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof CredentialInternalQuery) {
            final @Nonnull CredentialInternalQuery other = (CredentialInternalQuery) object;
            return this.permissions.equals(other.permissions) && Objects.equals(this.relation, other.relation) && Objects.equals(this.value, other.value);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + permissions.hashCode();
        hash = 89 * hash + Objects.hashCode(relation);
        hash = 89 * hash + Objects.hashCode(value);
        return hash;
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
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, PacketException, ExternalException, NetworkException {
            return new CredentialInternalQuery(entity, signature, recipient, block);
        }
        
    }
    
}
