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
package net.digitalid.core.handler.reply.instances;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.reply.QueryReply;

/**
 * This class models an empty reply.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class EmptyReply extends QueryReply<NonHostEntity> implements CoreHandler<NonHostEntity> {
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<NonHostEntity> method) {
        return method instanceof Action;
    }
    
}
