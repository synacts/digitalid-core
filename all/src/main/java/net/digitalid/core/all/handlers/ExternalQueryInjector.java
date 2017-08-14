package net.digitalid.core.all.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.concurrency.map.ConcurrentHashMapBuilder;
import net.digitalid.utility.concurrency.map.ConcurrentMap;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.size.NonEmpty;

import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.credential.utility.HashedOrSaltedAgentPermissions;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;

/**
 *
 */
public class ExternalQueryInjector {
    
    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential>> identityBasedCredentials = ConcurrentHashMapBuilder.build();
    
    @Pure
    private static @Nonnull ClientCredential getAttributeBased(@Nonnull Role role, @Nonnull CertifiedAttributeValue certifiedAttributeValue, @Nonnull @NonEmpty ReadOnlyAgentPermissions permissions) {
        // TODO: implement attribute based client credential
        return null;
    }
    
    @Pure
    private static @Nonnull ClientCredential getIdentityBased(@Nonnull Role role, @Nonnull @NonEmpty ReadOnlyAgentPermissions permissions) {
        @Nullable ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential> map = identityBasedCredentials.get(role);
        if (map == null) map = identityBasedCredentials.putIfAbsentElseReturnPresent(role, ConcurrentHashMapBuilder.build());
        @Nullable ClientCredential credential = map.get(permissions);
        
        if (credential == null || !credential.isActive()) {
            final @Nonnull HashedOrSaltedAgentPermissions randomizedPermissions = HashedOrSaltedAgentPermissions.with(permissions, true);
            // TODO: uncomment and fix
//            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions).sendNotNull();
//            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getInternalCredential(randomizedPermissions, null, BigInteger.ZERO, role.getUnit().secret.get()));
        }
        return credential;
    }
    
    @Pure
    private static final @Nonnull FreezableList<@Nonnull ClientCredential> retrieveClientCredentialsFunction(@Nonnull ExternalQuery<?> externalQuery, @Nonnull ReadOnlyAgentPermissions permissions) throws ExternalException {
        final @Nonnull FreezableList<@Nonnull ClientCredential> credentials;
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull Role role = (Role) externalQuery.getEntity();
        if (externalQuery.getAuthentications().contains(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
            final @Nonnull ClientCredential credential = getIdentityBased(role, permissions);
            credentials = FreezableArrayList.withElement(credential);
        } else {
            credentials = FreezableArrayList.withInitialCapacity(externalQuery.getAuthentications().size());
            for (final @Nonnull SemanticType type : externalQuery.getAuthentications()) {
                final @Nullable AttributeValue attributeValue = Attribute.of(externalQuery.getEntity(), type).value().get();
                if (attributeValue != null && attributeValue.isCertified()) {
                    final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
                    if (certifiedAttributeValue.isValid(time)) { credentials.add(getAttributeBased(role, certifiedAttributeValue, permissions)); }
                }
            }
        }
        return credentials;
    }
    
    @Pure
    private static final @Nonnull FreezableList<@Nonnull CertifiedAttributeValue> retrieveCertificatesFunction(@Nonnull ExternalQuery<?> externalQuery) throws ExternalException {
        final @Nonnull FreezableList<@Nonnull CertifiedAttributeValue> certificates;
        final @Nonnull Time time = TimeBuilder.build();
        if (externalQuery.getAuthentications().contains(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
            certificates = FreezableArrayList.withInitialCapacity(externalQuery.getAuthentications().size() - 1);
            // TODO: implement the method to get the verifiedAttributes
            for (final @Nonnull SemanticType type : externalQuery.getAuthentications()) {
                if (!type.equals(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
                    final @Nullable AttributeValue attributeValue = Attribute.of(externalQuery.getEntity(), type).value().get();
                    if (attributeValue != null && attributeValue.isCertified()) {
                        final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
                        if (certifiedAttributeValue.isValid(time)) { certificates.add(certifiedAttributeValue); }
                    }
                }
            }
            certificates.freeze();
        } else {
            certificates = null;
        }
        return certificates;
    }
    
    /**
     * Injects the methods to retrieve credentials and certificates into the configuration of the external query class.
     */
    @PureWithSideEffects
    @Initialize(target = ExternalQuery.class)
    public static void injectExternalQueryFunctions() {
        ExternalQuery.clientCredentialsInjection.set(ExternalQueryInjector::retrieveClientCredentialsFunction);
        ExternalQuery.certificatesInjection.set(ExternalQueryInjector::retrieveCertificatesFunction);
    }
    
}
