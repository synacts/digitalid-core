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
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * This class models a non-host account.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class NonHostAccount extends Account implements NonHostEntity {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull InternalNonHostIdentity getIdentity();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull NonHostAccount with(@Nonnull Host host, @Nonnull InternalNonHostIdentity identity) {
        return new NonHostAccountSubclass(host, identity);
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    // TODO: Figure out whether/how we can use the ConceptIndex for this.
    
//    /**
//     * Caches non-host accounts given their host and identity.
//     */
//    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<InternalNonHostIdentity, NonHostAccount>> index = ConcurrentHashMap.get();
//    
//    static {
//        if (Database.isSingleAccess()) {
//            Instance.observeAspects(new Observer() {
//                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
//            }, Host.DELETED);
//        }
//    }
//    
//    /**
//     * Returns a potentially locally cached non-host account.
//     * 
//     * @param host the host of the non-host account to return.
//     * @param identity the identity of the non-host account to return.
//     * 
//     * @return a new or existing non-host account with the given host and identity.
//     */
//    @Pure
//    public static @Nonnull NonHostAccount get(@Nonnull Host host, @Nonnull InternalNonHostIdentity identity) {
//        if (Database.isSingleAccess()) {
//            @Nullable ConcurrentMap<InternalNonHostIdentity, NonHostAccount> map = index.get(host);
//            if (map == null) { map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<InternalNonHostIdentity, NonHostAccount>()); }
//            @Nullable NonHostAccount account = map.get(identity);
//            if (account == null) { account = map.putIfAbsentElseReturnPresent(identity, new NonHostAccount(host, identity)); }
//            return account;
//        } else {
//            return new NonHostAccount(host, identity);
//        }
//    }
    
}
