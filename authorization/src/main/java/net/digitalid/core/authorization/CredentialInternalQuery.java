package net.digitalid.core.authorization;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.AgentFactory;
import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.credential.ClientCredentialBuilder;
import net.digitalid.core.credential.HostCredential;
import net.digitalid.core.credential.HostCredentialBuilder;
import net.digitalid.core.credential.HostCredentialConverter;
import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.ExposedExponentBuilder;
import net.digitalid.core.credential.utility.HashedOrSaltedAgentPermissions;
import net.digitalid.core.credential.utility.SaltedAgentPermissions;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.GroupWithKnownOrder;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.InternalQuery;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsConverter;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * Requests a new identity-based or role-based credential with the given permissions and relation.
 * 
 * @see CredentialReply
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
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
    
    @Pure
    @Override
    @NonCommitting
    protected @Nonnull CredentialReply execute() throws DatabaseException {
        // TODO: 
        if (isLodged() && getSignature() instanceof CredentialsSignature) {
//            ((CredentialsSignature) getSignature()).checkIsLogded();
        }
        final @Nonnull Agent agent;
        if (getSignature() instanceof ClientSignature) {
            agent = AgentFactory.retrieve(getEntity(), ((ClientSignature<?>) getSignature()).getCommitment());
        } else {
            throw new UnsupportedOperationException("Retrieving credentials with a credentialsSignature is not yet implemented.");
        }

        // TODO: implement permission and restriction checks
//        final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissions();
//        if (!permissions.equals(FreezableAgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
//
//        final @Nonnull Restrictions restrictions = getRequiredRestrictions();
//        if (!restrictions.equals(Restrictions.MIN)) agent.getRestrictions().checkCover(restrictions);

        try {
            final @Nonnull Restrictions restrictions = agent.restrictions().get();
            final @Nonnull Signature signature = getSignature();
//            final @Nonnull NonHostEntity account = getEntity();
            final @Nonnull HostIdentifier hostIdentifier = getEntity().getIdentity().getAddress().getHostIdentifier();
//            final @Nonnull Host host = account.getHost();
    
            // TODO: issuance time must probably be read from the public client credential.
//            final @Nonnull Time issuance = signature instanceof CredentialsSignature ? ((CredentialsSignature) signature).getCredentials().getNonNullable(0).getIssuance() : signature.getNonNullableTime().roundDown(Time.HALF_HOUR);
            final @Nonnull Time issuance = signature.getTime().roundDown(Time.HALF_HOUR);
            
            try {
                final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(hostIdentifier, issuance);
                final @Nonnull PrivateKey privateKey = PrivateKeyRetriever.retrieve(hostIdentifier, issuance);
                final @Nonnull GroupWithKnownOrder group = privateKey.getCompositeGroup();
        
//                Require.that(value != null).orThrow("See the constructor.");
                final @Nonnull Element f = group.getElement(getValue());
                final @Nonnull Exponent i = ExponentBuilder.withValue(new BigInteger(Parameters.HASH_SIZE.get(), new SecureRandom())).build();
                final @Nonnull byte[] restrictionsHash = XDF.hash(RestrictionsConverter.INSTANCE, restrictions);
                final @Nonnull Exponent v = ExponentBuilder.withValue(new BigInteger(restrictionsHash)).build();
                final @Nonnull ExposedExponent exposedExponent = ExposedExponentBuilder.withIssuer(getEntity().getIdentity()).withIssuance(issuance).withHashedOrSaltedPermissions(HashedOrSaltedAgentPermissions.with(getPermissions(), true)).withRole(getRelation()).withAttributeContent(null).build();
    
                final @Nonnull HostCredential hostCredential = HostCredentialBuilder.withExposedExponent(exposedExponent).withI(i).build();
//                final @Nonnull Exponent o = Exponent.withValue(ClientCredentialBuilder.getExposed(account.getIdentity(), issuance, permissions, relation, null).getHash());
                final @Nonnull Exponent e = ExponentBuilder.withValue(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT.get(), new SecureRandom())).build();
        
                final @Nonnull Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(hostCredential.getO()).inverse()).pow(e.inverse(group)).inverse();
    
                SQL.insert(HostCredentialConverter.INSTANCE, hostCredential, Unit.DEFAULT, null);
        
//                final @Nonnull ClientCredential clientCredential = ClientCredentialBuilder.withExposedExponent(exposedExponent).withC(c).withE(e).withB(ExponentBuilder.withValue(BigInteger.ZERO).build()).withU(ExponentBuilder.withValue(BigInteger.ZERO).build()).withV(v).withI(i).build();
                return CredentialReplyBuilder.withEntity(getEntity()).withPublicKey(publicKey).withIssuance(issuance).withC(c).withE(e).withI(i).withRestrictions(restrictions).build();
            } catch (ExternalException e) {
                // TODO: use better exception or throw recovery exception.
                throw new RuntimeException(e);
            }
        } catch (RecoveryException e) {
            // TODO: use better exception or throw recovery exception.
            throw new RuntimeException(e);
        }
    }
    
}
