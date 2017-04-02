package net.digitalid.core.authorization;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.credential.utility.SaltedAgentPermissions;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.InternalQuery;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Requests a new identity- or role-based credential with the given permissions and relation.
 * 
 * @see CredentialReply
 */
@Immutable
// TODO: @GenerateSubclass
// TODO: @GenerateConverter
abstract class CredentialInternalQuery extends InternalQuery implements CoreMethod<NonHostEntity> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the permissions for which a credential is requested.
     */
    @Pure
    abstract @Nonnull SaltedAgentPermissions getPermissions(); // TODO: Rather HashedOrSaltedAgentPermissions?
    
    /**
     * Returns the relation of the role-based credential that is requested.
     */
    @Pure
    abstract @Nullable @RoleType SemanticType getRelation();
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    @Pure
    abstract @Nullable BigInteger getValue();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    // TODO: Replicate the checks and invariants of the following constructors.
    
//    /**
//     * Creates an internal query for a new identity-based credential with the given permissions.
//     * 
//     * @param role the role to which this handler belongs.
//     * @param permissions the permissions for which a credential is requested.
//     * 
//     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
//     */
//    CredentialInternalQuery(@Nonnull Role role, @Nonnull SaltedAgentPermissions permissions) {
//        super(role);
//        
//        Require.that(role.getIdentity() instanceof InternalPerson).orThrow("The role belongs to an internal person.");
//        
//        this.permissions = permissions;
//        this.relation = null;
//        this.value = null;
//    }
//    
//    /**
//     * Creates an internal query for a new role-based credential with the given permissions.
//     * 
//     * @param role the role to which this handler belongs.
//     * @param permissions the permissions for which a credential is requested.
//     * @param value the value used for shortening an existing credential.
//     * 
//     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
//     */
//    CredentialInternalQuery(@Nonnull NonNativeRole role, @Nonnull RandomizedAgentPermissions permissions, @Nonnull BigInteger value) {
//        super(role.getRecipient());
//        
//        Require.that(role.getIdentity() instanceof InternalPerson).orThrow("The role belongs to an internal person.");
//        
//        this.permissions = permissions;
//        this.relation = role.getRelation();
//        this.value = value;
//    }
//    
//    /**
//     * Creates an internal query that decodes the given block.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the signature of this handler.
//     * @param recipient the recipient of this method.
//     * @param block the content which is to be decoded.
//     * 
//     * @require signature.hasSubject() : "The signature has a subject.";
//     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     * @ensure isOnHost() : "Queries are only decoded on hosts.";
//     */
//    @NonCommitting
//    private CredentialInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, recipient);
//        
//        if (!(entity.getIdentity() instanceof InternalPerson)) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "An identity- or role-based credential can only be requested for internal persons."); }
//        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
//        this.permissions = new RandomizedAgentPermissions(tuple.getNonNullableElement(0));
//        if (tuple.isElementNull(1)) { this.relation = null; }
//        else { this.relation = IdentityImplementation.create(tuple.getNonNullableElement(1)).castTo(SemanticType.class).checkIsRoleType(); }
//        if (signature instanceof ClientSignatureWrapper) { this.value = ((ClientSignatureWrapper) signature).getCommitment().getValue().getValue(); }
//        else if (signature instanceof CredentialsSignatureWrapper) { this.value = ((CredentialsSignatureWrapper) signature).getValue(); }
//        else { throw RequestException.get(RequestErrorCode.SIGNATURE, "A credential request must be signed by a client or with credentials."); }
//    }
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isLodged() {
        return true;
    }
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return getPermissions().getPermissions();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        if (getRelation() == null) { return Restrictions.MIN; }
        else { return Restrictions.CAN_ASSUME_ROLES; } // TODO: Is this the right one?
    }
    
    // TODO:
    
//    @Override
//    @NonCommitting
//    protected @Nonnull CredentialReply executeOnHost(@Nonnull Agent agent) throws DatabaseException {
//        final @Nonnull Restrictions restrictions = agent.getRestrictions();
//        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
//        final @Nonnull NonHostAccount account = getNonHostAccount();
//        final @Nonnull Host host = account.getHost();
//        
//        final @Nonnull Time issuance = signature instanceof CredentialsSignatureWrapper ? ((CredentialsSignatureWrapper) signature).getCredentials().getNonNullable(0).getIssuance() : signature.getNonNullableTime().roundDown(Time.HALF_HOUR);
//        
//        try {
//            final @Nonnull PublicKey publicKey = host.getPublicKeyChain().getKey(issuance);
//            final @Nonnull PrivateKey privateKey = host.getPrivateKeyChain().getKey(issuance);
//            final @Nonnull Group group = privateKey.getCompositeGroup();
//            
//            Require.that(value != null).orThrow("See the constructor.");
//            final @Nonnull Element f = group.getElement(value);
//            final @Nonnull Exponent i = Exponent.get(new BigInteger(Parameters.HASH, new SecureRandom()));
//            final @Nonnull Exponent v = Exponent.get(restrictions.toBlock().getHash());
//            final @Nonnull Exponent o = Exponent.get(Credential.getExposed(account.getIdentity(), issuance, permissions, relation, null).getHash());
//            final @Nonnull Exponent e = Exponent.get(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
//            
//            final @Nonnull Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(o).inverse()).pow(e.inverse(group)).inverse();
//            
//            HostCredentialModule.store(account, e, i, v, signature);
//            
//            return new CredentialReply(account, publicKey, restrictions, issuance, c, e, i);
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("No key was found for the time of the signature.");
//        }
//    }
    
}
