package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class represents an expression.
 * 
 * @see BinaryExpression
 * @see ContactExpression
 * @see ContextExpression
 * @see EmptyExpression
 * @see EverybodyExpression
 * @see RestrictionExpression
 */
@Immutable
abstract class Expression extends RootClass {
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this expression is public.
     */
    @Pure
    abstract boolean isPublic();
    
    /**
     * Returns whether this expression is active.
     */
    @Pure
    abstract boolean isActive();
    
    /**
     * Returns whether this expression is impersonal.
     */
    @Pure
    abstract boolean isImpersonal();
    
    /* -------------------------------------------------- Aggregations -------------------------------------------------- */
    
    /**
     * Returns the contacts denoted by this expression.
     * 
     * @require isActive() : "This expression is active.";
     */
    @Pure
    @NonCommitting
    abstract @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() throws DatabaseException, RecoveryException;
    
    /**
     * Returns whether this expression matches the given attribute content.
     * 
     * @require isImpersonal() : "This expression is impersonal.";
     */
    @Pure
    abstract boolean matches(@Nonnull Pack attributeContent);
    
    /**
     * Returns whether this expression matches the given signature.
     */
    @Pure
    @NonCommitting
    abstract boolean matches(@Nonnull CredentialsSignature<?> signature) throws DatabaseException, RecoveryException;
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns this expression as a string.
     * 
     * @param operator the operator of the parent binary expression or null otherwise.
     * @param right whether this expression is the right child of a binary expression.
     */
    @Pure
    abstract @Nonnull String toString(@Nullable BinaryOperator operator, boolean right);
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return toString(null, false);
    }
    
}
