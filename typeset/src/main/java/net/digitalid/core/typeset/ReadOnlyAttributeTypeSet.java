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
package net.digitalid.core.typeset;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;

/**
 * This interface provides read-only access to {@link FreezableAttributeTypeSet attribute type set} and should <em>never</em> be cast away.
 * 
 * @see ReadOnlyAuthentications
 * @see ReadOnlyNodePermissions
 */
@ReadOnly(FreezableAttributeTypeSet.class)
public interface ReadOnlyAttributeTypeSet extends ReadOnlySet<@Nonnull @AttributeType SemanticType> {
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAttributeTypeSet clone();
    
    /* -------------------------------------------------- Exports -------------------------------------------------- */
    
    /**
     * Returns this attribute type set as agent permissions with read access to all attributes.
     */
    @Pure
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions toAgentPermissions();
    
}
