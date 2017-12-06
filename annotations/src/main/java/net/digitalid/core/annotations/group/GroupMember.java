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
package net.digitalid.core.annotations.group;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A group member belongs to a mathematical {@link GroupInterface group}.
 */
@Immutable
public interface GroupMember extends RootInterface {
    
    /**
     * Returns the group of this member.
     */
    @Pure
    public @Nonnull GroupInterface getGroup();
    
    /**
     * Returns whether this member is in the given group.
     */
    @Pure
    public default boolean isIn(@Nonnull GroupInterface group) {
        return getGroup().equals(group);
    }
    
}
