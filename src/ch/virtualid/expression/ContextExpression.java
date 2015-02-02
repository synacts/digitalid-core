package ch.virtualid.expression;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.Context;
import ch.virtualid.entity.NonHostEntity;
import static ch.virtualid.expression.Expression.operators;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableSet;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models context expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class ContextExpression extends Expression implements Immutable {
    
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
    @Nonnull @Capturable FreezableSet<Contact> getContacts() throws SQLException {
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
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws SQLException {
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
