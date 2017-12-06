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
package net.digitalid.core.host.account;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * This class models an account on the host.
 * 
 * @see HostAccount
 * @see NonHostAccount
 */
@Immutable
@GenerateConverter
public abstract class Account implements Entity {
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    public abstract @Nonnull Host getUnit();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    @Pure
    @Override
    public long getKey() {
        return getIdentity().getKey();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns a potentially locally cached account.
     */
    @Pure
    @Recover
    public static @Nonnull Account with(@Nonnull Host unit, @Nonnull InternalIdentity identity) {
        if (identity instanceof HostIdentity) {
            return HostAccount.with(unit, (HostIdentity) identity);
        } else if (identity instanceof InternalNonHostIdentity) {
            return NonHostAccount.with(unit, (InternalNonHostIdentity) identity);
        } else {
            throw CaseExceptionBuilder.withVariable("identity").withValue(identity).build();
        }
    }
    
    /* -------------------------------------------------- Initializer -------------------------------------------------- */
    
    /**
     * Initializes the account factory.
     */
    @PureWithSideEffects
    @Initialize(target = AccountFactory.class)
    public static void initializeAccountFactory() {
        AccountFactory.configuration.set((host, identity) -> Account.with((Host) host, identity));
    }
    
}
