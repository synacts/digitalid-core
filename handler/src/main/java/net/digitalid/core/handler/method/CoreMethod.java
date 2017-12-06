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
package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.service.CoreService;

/**
 * This interface is implemented by all methods of the core service.
 * 
 * @see CoreService
 */
@Immutable
public interface CoreMethod<ENTITY extends Entity> extends Method<ENTITY>, CoreHandler<ENTITY> {
    
    @Pure
    @Override
    @OrderOfAssignment(2)
    @Derive("subject.getHostIdentifier()")
    public abstract @Nonnull HostIdentifier getRecipient();
    
}
