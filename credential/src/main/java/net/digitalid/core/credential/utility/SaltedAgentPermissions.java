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
package net.digitalid.core.credential.utility;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

/**
 * This class models the salted {@link ReadOnlyAgentPermissions permissions} of outgoing roles.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class SaltedAgentPermissions extends RootClass {
    
    /* -------------------------------------------------- Salt -------------------------------------------------- */
    
    /**
     * Returns the salt.
     */
    @Pure
    protected abstract @Nonnull BigInteger getSalt();
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions.
     */
    @Pure
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getPermissions();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates new salted permissions with the given permissions.
     */
    @Pure
    public static @Nonnull SaltedAgentPermissions with(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) {
        return new SaltedAgentPermissionsSubclass(new BigInteger(Parameters.EXPONENT.get(), new SecureRandom()), permissions);
    }
    
}
