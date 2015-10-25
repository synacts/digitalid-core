package net.digitalid.service.core.expression;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.contact.Contact;
import net.digitalid.service.core.contact.Context;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.CredentialsSignatureWrapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models context expressions.
 */
@Immutable
final class ContextExpression extends Expression {
    
    /**
     * Stores the context of this expression.
     */
    private final @Nonnull Context context;
    
    /**
     * Creates a new context expression with the given context.
     * 
     * @param context the context to use.
     */
    ContextExpression(@Nonnull NonHostEntity entity, @Nonnull Context context) {
        super(entity);
        
        this.context = context;
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
    @NonCommitting
    @Nonnull @Capturable FreezableSet<Contact> getContacts() throws AbortException {
        assert isActive() : "This expression is active.";
        
        return context.getAllContacts();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Block attributeContent) {
        assert isImpersonal() : "This expression is impersonal.";
        
        return false;
    }
    
    @Pure
    @Override
    @NonCommitting
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws AbortException {
        return signature.isIdentityBased() && !signature.isRoleBased() && context.contains(Contact.get(getEntity(), signature.getIssuer()));
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        assert operator == null || operators.contains(operator) : "The operator is valid.";
        
        return context.toString();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof ContextExpression)) return false;
        final @Nonnull ContextExpression other = (ContextExpression) object;
        return this.context.equals(other.context);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return context.hashCode();
    }
    
}
