package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedHashSet;
import net.digitalid.core.collections.FreezableSet;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;

/**
 * This class models everybody expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
final class EverybodyExpression extends Expression implements Immutable {
    
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
        
        return new FreezableLinkedHashSet<Contact>();
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
