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
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.node.context.Context;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class models context expressions.
 */
@Immutable
@GenerateSubclass
abstract class ContextExpression extends Expression {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the context of this expression.
     */
    @Pure
    abstract @Nonnull Context getContext();
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    boolean isPublic() {
        return false;
    }
    
    @Pure
    @Override
    boolean isActive() {
        return true;
    }
    
    @Pure
    @Override
    boolean isImpersonal() {
        return false;
    }
    
    /* -------------------------------------------------- Aggregations -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() throws DatabaseException, RecoveryException {
        Require.that(isActive()).orThrow("This expression has to be active but was $.", this);
        
        return getContext().getAllContacts();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Pack attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression has to be impersonal but was $.", this);
        
        return false;
    }
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Implement the check.", date = "2016-12-02", author = Author.KASPAR_ETTER, priority = Priority.HIGH)
    boolean matches(@Nonnull CredentialsSignature<?> signature) throws DatabaseException, RecoveryException {
        return true;
//        return signature.isIdentityBased() && !signature.isRoleBased() && context.contains(Contact.get(getEntity(), signature.getIssuer()));
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable BinaryOperator operator, boolean right) {
        return getContext().getKey().toString();
    }
    
}
