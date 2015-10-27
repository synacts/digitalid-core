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
 * This class models contact expressions.
 */
@Immutable
final class ContactExpression extends Expression {
    
    /**
     * Stores the contact of this expression.
     */
    private final @Nonnull Contact contact;
    
    /**
     * Creates a new contact expression with the given contact.
     * 
     * @param contact the contact to use.
     */
    ContactExpression(@Nonnull NonHostEntity entity, @Nonnull Contact contact) {
        super(entity);
        
        this.contact = contact;
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
        return false;
    }
    
    
    @Pure
    @Override
    @Nonnull @Capturable FreezableSet<Contact> getContacts() {
        assert isActive() : "This expression is active.";
        
        return new FreezableLinkedHashSet<>(contact);
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
        return signature.isIdentityBased() && !signature.isRoleBased() && signature.getIssuer().equals(contact.getPerson());
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        assert operator == null || operators.contains(operator) : "The operator is valid.";
        
        return addQuotesIfNecessary(contact.getPerson());
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof ContactExpression)) return false;
        final @Nonnull ContactExpression other = (ContactExpression) object;
        return this.contact.equals(other.contact);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return contact.hashCode();
    }
    
}
