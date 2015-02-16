package ch.virtualid.expression;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.entity.NonHostEntity;
import static ch.virtualid.expression.Expression.operators;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.collections.FreezableLinkedHashSet;
import ch.virtualid.collections.FreezableSet;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models empty expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class EmptyExpression extends Expression implements Immutable {
    
    /**
     * Creates a new empty expression.
     * 
     * @param entity the entity to which this expression belongs.
     */
    EmptyExpression(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
    
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
    
    
    @Pure
    @Override
    @Nonnull @Capturable FreezableSet<Contact> getContacts() {
        assert isActive() : "This expression is active.";
        
        return new FreezableLinkedHashSet<Contact>();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Block attributeContent) {
        assert isImpersonal() : "This expression is impersonal.";
        
        return true;
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) {
        return false;
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        assert operator == null || operators.contains(operator) : "The operator is valid.";
        
        return "";
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return object instanceof EmptyExpression;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 1234567;
    }
    
}
