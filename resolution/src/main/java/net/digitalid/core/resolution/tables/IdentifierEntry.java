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
package net.digitalid.core.resolution.tables;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This type models an entry in the identifier table.
 * 
 * @see IdentityEntry
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateTableConverter(schema = "general")
public interface IdentifierEntry extends RootInterface {
    
    @Pure
    @PrimaryKey
    public @Nonnull Identifier getIdentifier();
    
    @Pure
    @TODO(task = "This should generate a foreign key on the identity table.", date = "2017-02-24", author = Author.KASPAR_ETTER)
    public long getKey();
    
}
