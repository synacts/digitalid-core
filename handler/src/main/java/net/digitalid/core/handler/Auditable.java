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
package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingle;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Handlers that can be added to the audit have to implement this interface.
 * 
 * @see Action
 * @see ActionReply
 */
@Immutable
public interface Auditable {
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nonnull @EmptyOrSingle ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return ReadOnlyAgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return Restrictions.MIN;
    }
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public default @Nullable Agent getRequiredAgentToSeeMethod() {
        return null;
    }
    
}
