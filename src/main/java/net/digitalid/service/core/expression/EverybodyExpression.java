package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.annotations.reference.Capturable;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.collections.freezable.FreezableSet;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;

/**
 * This class models everybody expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
final class EverybodyExpression extends Expression {
    
    /**
     * Creates a new everybody expression.
     * 
     * @param entity the entity to which this expression belongs.
     */
    EverybodyExpression(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
    
    @Pure
    @Override
    boolean isPublic() {
        return true;
    }
    
    @Pure
    @Override
    boolean isActive() {
        return false;
    }
    
    @Pure
    @Override
    boolean isImpersonal() {
        return false;
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
        
        return false;
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) {
        return true;
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        assert operator == null || operators.contains(operator) : "The operator is valid.";
        
        return "everybody";
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return object instanceof EverybodyExpression;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 7654321;
    }
    
}
