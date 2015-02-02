package ch.virtualid.expression;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.entity.NonHostEntity;
import static ch.virtualid.expression.Expression.operators;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableSet;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models contact expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
        
        return new FreezableLinkedHashSet<Contact>(contact);
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
