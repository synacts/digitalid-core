package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.FreezableLinkedHashSetBuilder;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.selfcontained.Selfcontained;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class models empty expressions.
 */
@Immutable
@GenerateSubclass
abstract class EmptyExpression extends Expression {
    
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
        return true;
    }
    
    /* -------------------------------------------------- Aggregations -------------------------------------------------- */
    
    @Pure
    @Override
    @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() {
        Require.that(isActive()).orThrow("This expression has to be active but was $.", this);
        
        return FreezableLinkedHashSetBuilder.build();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Selfcontained attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression has to be impersonal but was $.", this);
        
        return true;
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull CredentialsSignature<?> signature) {
        return false;
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable BinaryOperator operator, boolean right) {
        return "";
    }
    
    // TODO: Check whether the equals and hashCode methods are correctly generated.
    
//    @Pure
//    @Override
//    public boolean equals(@Nullable Object object) {
//        return object instanceof EmptyExpression;
//    }
//    
//    @Pure
//    @Override
//    public int hashCode() {
//        return 1234567;
//    }
//    
}
