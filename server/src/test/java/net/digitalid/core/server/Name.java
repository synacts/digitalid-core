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
package net.digitalid.core.server;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.pack.Packable;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface Name extends Packable {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    public static final @Nonnull @Loaded SemanticType TYPE = SemanticType.map(NameConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.STRING).withCategories(Category.INTERNAL_NON_HOST_IDENTITIES).withCachingPeriod(Time.MONTH).build());
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    @Pure
    public @Nonnull String getValue();
    
}
