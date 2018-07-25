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
package net.digitalid.core.node.context;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;

/**
 * The following code cannot be in the context class because instantiating the initializer would trigger the context class to get loaded which in turn would trigger an IdentifierResolver not initialized error because the CoreService maps a SemanticType.
 */
@Utility
@TODO(task = "Find a better solution for this problem!", date = "2017-08-20", author = Author.KASPAR_ETTER)
public abstract class ContextModuleInitializer {
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE).addDependency(IdentifierResolver.configuration);
    
    /**
     * Loads the context subclass after having initialized the identifier resolver.
     */
    @PureWithSideEffects
    @Initialize(target = ContextModuleInitializer.class)
    public static void initializeSubclass() {
        FreezableAuthentications.IDENTITY_BASED_TYPE.getKey();
        ContextSubclass.MODULE.getName();
    }
    
}
