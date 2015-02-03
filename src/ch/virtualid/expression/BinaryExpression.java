package ch.virtualid.expression;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableSet;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models binary expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class BinaryExpression extends Expression implements Immutable {
    
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
    BinaryExpression(@Nonnull NonHostEntity entity, @Nonnull String left, @Nonnull String right, char operator) throws SQLException, IOException, PacketException, ExternalException {
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
    @Nonnull @Capturable FreezableSet<Contact> getContacts() throws SQLException {
        assert isActive() : "This expression is active.";
        
        final @Nonnull FreezableSet<Contact> leftContacts = left.getContacts();
        final @Nonnull FreezableSet<Contact> rightContacts = right.getContacts();
        switch (operator) {
            case '+': return leftContacts.add(rightContacts);
            case '-': return leftContacts.subtract(rightContacts);
            case '*': return leftContacts.intersect(rightContacts);
            default: return new FreezableLinkedHashSet<Contact>();
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
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws SQLException {
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
            throw new ShouldNeverHappenError("The operator '" + operator + "' is invalid.");
        }
        
        return parentheses ? "(" + string + ")" : string;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof BinaryExpression)) return false;
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
