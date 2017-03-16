// TODO

package net.digitalid.core.client;
//
//import java.math.BigInteger;
//import java.security.SecureRandom;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.freezable.annotations.Frozen;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.validation.annotations.size.NonEmpty;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//
//import net.digitalid.core.credential.ClientCredential;
//import net.digitalid.core.credential.annotations.Active;
//import net.digitalid.core.credential.utility.SaltedAgentPermissions;
//import net.digitalid.core.entity.annotations.OfInternalPerson;
//
///**
// * Description.
// */
public class CredentialsIndex {
//    
//    /* -------------------------------------------------- Credentials Index -------------------------------------------------- */
//    
//    // TODO: The following code was moved from the ClientCredentials class.
//    
//    /**
//     * Caches the role-based client credentials given their role and permissions.
//     */
//    private static final @Nonnull ConcurrentMap<NonNativeRole, ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential>> roleBasedCredentials = new ConcurrentHashMap<>();
//    
//    /**
//     * Returns a role-based credential for the given role and permissions.
//     * 
//     * @param role the role for which the credential is to be returned.
//     * @param permissions the permissions which are to be contained.
//     * 
//     * @return a role-based credential for the given role and permissions.
//     */
//    @NonCommitting
//    public static @Nonnull @Active ClientCredential getRoleBased(@Nonnull @OfInternalPerson NonNativeRole role, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws ExternalException {
//        @Nullable ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential> map = roleBasedCredentials.get(role);
//        if (map == null) { map = roleBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadOnlyAgentPermissions, ClientCredential>()); }
//        @Nullable ClientCredential credential = map.get(permissions);
//        
//        if (credential == null || !credential.isActive()) {
//            final @Nonnull SaltedAgentPermissions randomizedPermissions = new SaltedAgentPermissions(permissions);
//            final @Nonnull BigInteger value = new BigInteger(Parameters.BLINDING_EXPONENT, new SecureRandom());
//            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions, value).sendNotNull();
//            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getInternalCredential(randomizedPermissions, role.getRelation(), value, role.getClient().getSecret()));
//        }
//        
//        return credential;
//    }
//    
//    
//    /**
//     * Caches the identity-based client credentials given their role and permissions.
//     */
//    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential>> identityBasedCredentials = new ConcurrentHashMap<>();
//    
//    /**
//     * Returns an identity-based credential for the given role and permissions.
//     * 
//     * @param role the role for which the credential is to be returned.
//     * @param permissions the permissions which are to be contained.
//     * 
//     * @return an identity-based credential for the given role and permissions.
//     */
//    @NonCommitting
//    public static @Nonnull @Active ClientCredential getIdentityBased(@Nonnull @OfInternalPerson Role role, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws ExternalException {
//        @Nullable ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential> map = identityBasedCredentials.get(role);
//        if (map == null) { map = identityBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadOnlyAgentPermissions, ClientCredential>()); }
//        @Nullable ClientCredential credential = map.get(permissions);
//        
//        if (credential == null || !credential.isActive()) {
//            final @Nonnull SaltedAgentPermissions randomizedPermissions = new SaltedAgentPermissions(permissions);
//            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions).sendNotNull();
//            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getInternalCredential(randomizedPermissions, null, BigInteger.ZERO, role.getClient().getSecret()));
//        }
//        
//        return credential;
//    }
//    
//    
//    /**
//     * Caches the attribute-based client credentials given their role, value and permissions.
//     */
//    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadOnlyPair<CertifiedAttributeValue, ReadOnlyAgentPermissions>, ClientCredential>> attributeBasedCredentials = new ConcurrentHashMap<>();
//    
//    /**
//     * Returns an attribute-based credential for the given role, value and permissions.
//     * 
//     * @param role the role for which the credential is to be returned.
//     * @param value the certified attribute value which is to be shortened.
//     * @param permissions the permissions which are to be contained.
//     * 
//     * @return an attribute-based credential for the given role, value and permissions.
//     */
//    @NonCommitting
//    public static @Nonnull @Active ClientCredential getAttributeBased(@Nonnull @OfInternalPerson Role role, @Nonnull CertifiedAttributeValue value, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws ExternalException {
//        // TODO: Shortening with CredentialExternalQuery.
//        throw new UnsupportedOperationException("Credentials for attribute-based access control are not yet supported!");
//    }
//    
//    
//    /**
//     * Removes the credentials of the given role.
//     * 
//     * @param role the role whose credentials are to be removed.
//     */
//    public static void remove(@Nonnull Role role) {
//        roleBasedCredentials.remove(role);
//        identityBasedCredentials.remove(role);
//        attributeBasedCredentials.remove(role);
//    }
//    
}
