/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.node;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.node.context.Context;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.NodeFactory;
import net.digitalid.core.restrictions.RestrictionsBuilder;

/**
 * Description.
 * 
 * @see Contact
 * @see Context
 */
@Immutable
public interface ExtendedNode {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the permissions.
     */
    public static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Node, SemanticType> PERMISSIONS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Node, SemanticType>withRequiredPermissionsToExecuteMethod((node, type) -> FreezableAgentPermissions.withPermission(type, false).freeze()).withRequiredRestrictionsToExecuteMethod((node, type) -> RestrictionsBuilder.withWriteToNode(true).withNode(node).build()).withRequiredPermissionsToSeeMethod((node, type) -> FreezableAgentPermissions.withPermission(type, false).freeze()).withRequiredRestrictionsToSeeMethod((node, type) -> RestrictionsBuilder.buildWithNode(node)).build();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the authentications.
     */
    public static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Node, SemanticType> AUTHENTICATIONS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Node, SemanticType>withRequiredPermissionsToExecuteMethod((node, type) -> FreezableAgentPermissions.withPermission(type, false).freeze()).withRequiredRestrictionsToExecuteMethod((node, type) -> RestrictionsBuilder.withWriteToNode(true).withNode(node).build()).withRequiredPermissionsToSeeMethod((node, type) -> FreezableAgentPermissions.withPermission(type, false).freeze()).withRequiredRestrictionsToSeeMethod((node, type) -> RestrictionsBuilder.buildWithNode(node)).build();
    
    /* -------------------------------------------------- Supercontexts -------------------------------------------------- */
    
    /**
     * Returns the direct supercontexts of this node.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull @NonFrozen @NonNullableElements ReadOnlySet<Context> getSupercontexts() throws DatabaseException, RecoveryException;
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the node factory.
     */
    @PureWithSideEffects
    @Initialize(target = NodeFactory.class)
    public static void initializeNodeFactory() {
        NodeFactory.configuration.set((entity, key) -> key % 2 == 0 ? Context.of(entity, key) : Contact.of(entity, key));
    }
    
}
