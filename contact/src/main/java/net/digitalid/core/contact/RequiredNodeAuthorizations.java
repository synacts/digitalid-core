package net.digitalid.core.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorization;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.RestrictionsBuilder;

/**
 * This interface contains the required authorizations to change the permissions and authentications of contacts and contexts.
 * Unfortunately, these authorizations cannot be in the node class as the node project does (and can) not depend on the restrictions.
 */
@Utility
public interface RequiredNodeAuthorizations {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the permissions.
     */
    public static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity, Long, Contact, SemanticType> PERMISSIONS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity, Long, Contact, SemanticType>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the authentications.
     */
    public static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity, Long, Contact, SemanticType> AUTHENTICATIONS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity, Long, Contact, SemanticType>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, false).freeze()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
}
