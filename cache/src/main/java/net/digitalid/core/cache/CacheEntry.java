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
package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;

/**
 * This type models an entry in the role table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateTableConverter(schema = "general")
public interface CacheEntry extends RootInterface {
    
    @Pure
    @PrimaryKey
    public @Nonnull /* Role */ Long getRequester();
    
    @Pure
    @PrimaryKey
    public @Nonnull InternalIdentity getRequestee();
    
    @Pure
    @PrimaryKey
    public @Nonnull @AttributeType SemanticType getAttributeType();
    
    @Pure
    @PrimaryKey
    public boolean isFound();
    
    @Pure
    public @Nonnull Time getExpirationTime();
    
    @Pure
    public @Nullable Pack getAttributeValue();
    
    // TODO (as soon as replies can be converted)
//    @Pure
//    public @Nullable Reply<?> getReply();
    
}
