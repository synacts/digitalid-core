package net.digitalid.service.core.expression;

import net.digitalid.service.core.block.Block;

import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableSet;

/**
 * This class models everybody expressions.
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
