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
package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.unit.CoreModuleBuilder;
import net.digitalid.core.unit.CoreUnit;

/**
 * This class models the core service.
 */
@Immutable
@GenerateSubclass
public abstract class CoreService extends Service {
    
    /* -------------------------------------------------- Singleton -------------------------------------------------- */
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService INSTANCE = new CoreServiceSubclass(SemanticType.map("@digitalid.net") /* TODO: Identity.IDENTIFIER */, "Core Service", "1.0", CoreModuleBuilder.withName("core").withParentModule(CoreUnit.MODULE).build());
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nonnull NonHostEntity entity) {
        return entity.getIdentity().getAddress().getHostIdentifier();
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nonnull InternalPerson subject, @Nullable NonHostEntity entity) {
        return subject.getAddress().getHostIdentifier();
    }
    
}
