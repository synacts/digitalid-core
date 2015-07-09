package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedHashSet;
import net.digitalid.core.collections.FreezableSet;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;

/**
 * This class models empty expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class EmptyExpression extends Expression {
    
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
        
        return new FreezableLinkedHashSet<>();
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
