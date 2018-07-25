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
package net.digitalid.core.initializer;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.account.OpenAccount;
import net.digitalid.core.account.OpenAccountConverter;
import net.digitalid.core.attribute.AttributeTypes;
import net.digitalid.core.authorization.CredentialInternalQueryConverter;
import net.digitalid.core.cache.attributes.AttributesQueryConverter;
import net.digitalid.core.cache.attributes.AttributesReplyConverter;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.handler.reply.instances.EmptyReplyConverter;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.resolution.handlers.IdentityQuery;
import net.digitalid.core.resolution.handlers.IdentityQueryConverter;
import net.digitalid.core.resolution.handlers.IdentityReplyConverter;

/**
 * This class initializes the core classes.
 */
@Utility
public abstract class CoreInitializer {
    
    /**
     * Initializes the method index.
     */
    @PureWithSideEffects
    @Initialize(target = MethodIndex.class)
    public static void initializeMethodIndex() {
        MethodIndex.add(IdentityQueryConverter.INSTANCE);
        MethodIndex.add(OpenAccountConverter.INSTANCE);
        MethodIndex.add(AttributesQueryConverter.INSTANCE);
        MethodIndex.add(CredentialInternalQueryConverter.INSTANCE);
        
        SemanticType.map(AttributesReplyConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build()); // TODO: Load the right attributes.
        SemanticType.map(IdentityReplyConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build()); // TODO: Load the right attributes.
        SemanticType.map(EmptyReplyConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build()); // TODO: Load the right attributes.
        
        AttributeTypes.NAME.isLoaded(); // Maps the type in the main thread.
        IdentityQuery.TYPE.isLoaded(); // Maps the type in the main thread.
        OpenAccount.TYPE.isLoaded(); // Maps the type in the main thread.
    }
    
}
