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
package net.digitalid.core.expression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
 */
@Immutable
abstract class AbstractExpression extends RootClass {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this expression belongs.
     */
    @Pure
    @Provided
    public abstract @Nonnull NonHostEntity getEntity();
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns the string which is to be parsed.
     */
    @Pure
    public abstract @Nonnull String getString();
    
    /* -------------------------------------------------- Expression -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Support throwing exceptions with derived fields.", date = "2016-12-02", author = Author.KASPAR_ETTER)
    @Nonnull Expression parse(@Nonnull NonHostEntity entity, @Nonnull String string) {
        try {
            return ExpressionParser.parse(entity, string);
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Returns the expression of this abstract expression.
     */
    @Pure
    @Derive("parse(entity, string)")
//    @Derive("ExpressionParser.parse(entity, string)")
    abstract @Nonnull Expression getExpression();
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return getExpression().toString();
    }
    
}
