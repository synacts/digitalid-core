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
package net.digitalid.core.expression.operators;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.validation.annotations.type.Immutable;


/**
 * This class enumerates the operators that can be used in attribute restrictions.
 */
@Immutable
public enum RestrictionOperator {
    
    EQUAL("="),
    UNEQUAL("≠"),
    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("≤"),
    GREATER_OR_EQUAL("≥"),
    PREFIX("/"),
    NOT_PREFIX("!/"),
    INFIX("|"),
    NOT_INFIX("!|"),
    POSTFIX("\\"),
    NOT_POSTFIX("!\\");
    
    /* -------------------------------------------------- Symbol -------------------------------------------------- */
    
    private final @Nonnull String symbol;
    
    /**
     * Returns the symbol of this operator.
     */
    @Pure
    public @Nonnull String getSymbol() {
        return symbol;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private RestrictionOperator(@Nonnull String symbol) {
        this.symbol = symbol;
    }
    
    /**
     * Returns the restriction operator denoted by the given symbol.
     */
    @Pure
    public static @Nonnull RestrictionOperator of(@Nonnull String symbol) {
        for (@Nonnull RestrictionOperator operator : values()) {
            if (operator.getSymbol() == symbol) { return operator; }
        }
        
        throw CaseExceptionBuilder.withVariable("symbol").withValue(symbol).build();
    }
    
}
