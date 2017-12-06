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
package net.digitalid.core.audit.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.handler.method.query.InternalQuery;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Queries the audit of the given service for the given role.
 * 
 * @see AuditReply
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
@TODO(task = "Make sure that this query gets registered as a handler. Maybe with an annotation?", date = "2016-11-09", author = Author.KASPAR_ETTER)
public abstract class AuditQuery extends InternalQuery {
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * For non-core services, it is important that the querying agent can pass its permissions in order to get the right credentials.
     */
    @Pure
    @Override
    @Default("ReadOnlyAgentPermissions.NONE")
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod();
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @OnHostRecipient
    @PureWithSideEffects
    public @Nonnull AuditReply execute() {
        return null; // TODO: AuditReply(getNonHostAccount(), service);
    }
    
}
