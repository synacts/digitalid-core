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
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class models binary expressions.
 */
@Immutable
@GenerateSubclass
abstract class BinaryExpression extends Expression {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the left child of this binary expression.
     */
    @Pure
    abstract @Nonnull Expression getLeftChild();
    
    /**
     * Returns the right child of this binary expression.
     */
    @Pure
    abstract @Nonnull Expression getRightChild();
    
    /**
     * Returns the operator of this binary expression.
     */
    @Pure
    abstract @Nonnull BinaryOperator getOperator();
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    boolean isPublic() {
        switch (getOperator()) {
            case ADDITION: return getLeftChild().isPublic() || getRightChild().isPublic();
            case SUBTRACTION: return getLeftChild().isPublic() && getRightChild() instanceof EmptyExpression;
            case MULTIPLICATION: return getLeftChild().isPublic() && getRightChild().isPublic();
            default: return false;
        }
    }
    
    @Pure
    @Override
    boolean isActive() {
        return getLeftChild().isActive() && getRightChild().isActive();
    }
    
    @Pure
    @Override
    boolean isImpersonal() {
        return getLeftChild().isImpersonal() && getRightChild().isImpersonal();
    }
    
    /* -------------------------------------------------- Aggregations -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() throws DatabaseException, RecoveryException {
        Require.that(isActive()).orThrow("This expression has to be active but was $.", this);
        
        final @Nonnull FreezableSet<Contact> leftContacts = getLeftChild().getContacts();
        final @Nonnull FreezableSet<Contact> rightContacts = getRightChild().getContacts();
        switch (getOperator()) {
            case ADDITION: leftContacts.addAll(rightContacts); break;
            case SUBTRACTION: leftContacts.removeAll(rightContacts); break;
            case MULTIPLICATION: leftContacts.retainAll(rightContacts); break;
            default: throw CaseExceptionBuilder.withVariable("operator").withValue(getOperator()).build();
        }
        return leftContacts;
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Pack attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression has to be impersonal but was $.", this);
        
        switch (getOperator()) {
            case ADDITION: return getLeftChild().matches(attributeContent) || getRightChild().matches(attributeContent);
            case SUBTRACTION: return getLeftChild().matches(attributeContent) && !getRightChild().matches(attributeContent);
            case MULTIPLICATION: return getLeftChild().matches(attributeContent) && getRightChild().matches(attributeContent);
            default: return false;
        }
    }
    
    @Pure
    @Override
    @NonCommitting
    boolean matches(@Nonnull CredentialsSignature<?> signature) throws DatabaseException, RecoveryException {
        switch (getOperator()) {
            case ADDITION: return getLeftChild().matches(signature) || getRightChild().matches(signature);
            case SUBTRACTION: return getLeftChild().matches(signature) && !getRightChild().matches(signature);
            case MULTIPLICATION: return getLeftChild().matches(signature) && getRightChild().matches(signature);
            default: return false;
        }
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable BinaryOperator operator, boolean right) {
        final @Nonnull String string = this.getLeftChild().toString(getOperator(), false) + getOperator().getSymbol() + this.getRightChild().toString(getOperator(), true);
        
        final boolean parentheses;
        if (operator == null || operator == BinaryOperator.ADDITION) {
            parentheses = false;
        } else if (operator == BinaryOperator.SUBTRACTION) {
            parentheses = right && getOperator() != BinaryOperator.MULTIPLICATION;
        } else if (operator == BinaryOperator.MULTIPLICATION) {
            parentheses = getOperator() != BinaryOperator.MULTIPLICATION;
        } else {
            throw CaseExceptionBuilder.withVariable("operator").withValue(operator).build();
        }
        
        return parentheses ? "(" + string + ")" : string;
    }
    
}
