package net.digitalid.service.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.CredentialsSignatureWrapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableSet;

/**
 * This class models empty expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
