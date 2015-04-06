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
 * This class models contact expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
final class ContactExpression extends Expression implements Immutable {
    
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
