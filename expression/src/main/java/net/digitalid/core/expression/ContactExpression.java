package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;

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
        Require.that(isActive()).orThrow("This expression is active.");
        
        return new FreezableLinkedHashSet<>(contact);
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Block attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression is impersonal.");
        
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
        Require.that(operator == null || operators.contains(operator)).orThrow("The operator is valid.");
        
        return addQuotesIfNecessary(contact.getPerson());
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof ContactExpression)) { return false; }
        final @Nonnull ContactExpression other = (ContactExpression) object;
        return this.contact.equals(other.contact);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return contact.hashCode();
    }
    
}
