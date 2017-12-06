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
package net.digitalid.core.unit.annotations;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This interface allows to use annotations on unit-based objects.
 * 
 * @see OnHost
 * @see OnClient
 * @see OnHostRecipient
 * @see OnClientRecipient
 */
@Immutable
public interface CoreUnitBased extends RootInterface {
    
    /**
     * Returns whether this object is on a host.
     */
    @Pure
    public boolean isOnHost();
    
    /**
     * Returns whether this object is on a client.
     */
    @Pure
    public boolean isOnClient();
    
}
