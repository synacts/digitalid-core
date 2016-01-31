package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.validation.annotations.reference.Capturable;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;

import net.digitalid.core.exceptions.RequestException;

/**
 * This class models binary expressions.
 */
@Immutable
final class BinaryExpression extends Expression {
    
    /**
     * Stores the left child of this binary expression.
     */
    private final @Nonnull Expression left;
    
    /**
     * Stores the right child of this binary expression.
     */
    private final @Nonnull Expression right;
    
    /**
     * Stores the operator this binary expression.
     * 
     * @invariant operators.contains(operator) : "The operator is valid.";
     */
    private final char operator;
    
    /**
     * Creates a new binary expression with the given left and right children.
     * 
     * @param entity the entity to which this expression belongs.
     * @param left the left child to parse.
     * @param right the right child to parse.
     * @param operator the operator to use.
     * 
     * @require operators.contains(operator) : "The operator is valid.";
     */
    @NonCommitting
    BinaryExpression(@Nonnull NonHostEntity entity, @Nonnull String left, @Nonnull String right, char operator) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity);
        
        assert operators.contains(operator) : "The operator is valid.";
        
        this.left = Expression.parse(entity, left);
        this.right = Expression.parse(entity, right);
        this.operator = operator;
    }
    
    
    @Pure
    @Override
    boolean isPublic() {
        switch (operator) {
            case '+': return left.isPublic() || right.isPublic();
            case '-': return left.isPublic() && right instanceof EmptyExpression;
            case '*': return left.isPublic() && right.isPublic();
            default: return false;
        }
    }
    
    @Pure
    @Override
    boolean isActive() {
        return left.isActive() && right.isActive();
    }
    
    @Pure
    @Override
    boolean isImpersonal() {
        return left.isImpersonal() && right.isImpersonal();
    }
    
    
    @Pure
    @Override
    @NonCommitting
    @Nonnull @Capturable FreezableSet<Contact> getContacts() throws DatabaseException {
        assert isActive() : "This expression is active.";
        
        final @Nonnull FreezableSet<Contact> leftContacts = left.getContacts();
        final @Nonnull FreezableSet<Contact> rightContacts = right.getContacts();
        switch (operator) {
            case '+': return leftContacts.add(rightContacts);
            case '-': return leftContacts.subtract(rightContacts);
            case '*': return leftContacts.intersect(rightContacts);
            default: return new FreezableLinkedHashSet<>();
        }
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Block attributeContent) {
        assert isImpersonal() : "This expression is impersonal.";
        
        switch (operator) {
            case '+': return left.matches(attributeContent) || right.matches(attributeContent);
            case '-': return left.matches(attributeContent) && !right.matches(attributeContent);
            case '*': return left.matches(attributeContent) && right.matches(attributeContent);
            default: return false;
        }
    }
    
    @Pure
    @Override
    @NonCommitting
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws DatabaseException {
        switch (operator) {
            case '+': return left.matches(signature) || right.matches(signature);
            case '-': return left.matches(signature) && !right.matches(signature);
            case '*': return left.matches(signature) && right.matches(signature);
            default: return false;
        }
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        assert operator == null || operators.contains(operator) : "The operator is valid.";
        
        final @Nonnull String string = this.left.toString(this.operator, false) + this.operator + this.right.toString(this.operator, true);
        
        final boolean parentheses;
        if (operator == null || operator == '+') {
            parentheses = false;
        } else if (operator == '-') {
            parentheses = right && this.operator != '*';
        } else if (operator == '*') {
            parentheses = this.operator != '*';
        } else {
            throw ShouldNeverHappenError.get("The operator '" + operator + "' is invalid.");
        }
        
        return parentheses ? "(" + string + ")" : string;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof BinaryExpression)) { return false; }
        final @Nonnull BinaryExpression other = (BinaryExpression) object;
        return this.left.equals(other.left) && this.right.equals(other.right) && this.operator == other.operator;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + left.hashCode();
        hash = 19 * hash + right.hashCode();
        hash = 19 * hash + operator;
        return hash;
    }
    
}
