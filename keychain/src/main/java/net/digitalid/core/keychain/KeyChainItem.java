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
package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.interfaces.CustomComparable;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.AsymmetricKey;

/**
 * This class models an item in the key chain.
 */
@Immutable
public abstract class KeyChainItem<KEY extends AsymmetricKey> implements CustomComparable<KeyChainItem<KEY>> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the time from when on the key is valid.
     */
    @Pure
    public abstract @Nonnull Time getTime();
    
    /**
     * Returns the key.
     */
    @Pure
    public abstract @Nonnull KEY getKey();
    
    /* -------------------------------------------------- Comparable -------------------------------------------------- */
    
    @Pure
    @Override
    public int compareTo(@Nonnull KeyChainItem<KEY> keyChainItem) {
        return getTime().compareTo(keyChainItem.getTime());
    }
    
}
