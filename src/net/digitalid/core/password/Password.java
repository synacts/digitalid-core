package net.digitalid.core.password;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.concept.Index;
import net.digitalid.core.concept.NonHostConcept;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.property.ValueValidator;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptPropertyTable;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models a password of a digital identity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Password extends NonHostConcept<Password> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code password@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("password@core.digitalid.net").load(StringWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The validator for this class.
     */
    public static class Validator implements ValueValidator<String> {
        
        /**
         * Creates a new validator.
         */
        private Validator() {}
        
        @Pure
        @Override
        public boolean isValid(@Nonnull String value) {
            return value.length() <= 50;
        }
        
    }
    
    /**
     * Stores the validator of this class.
     */
    public static final @Nonnull Validator VALIDATOR = new Validator();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private static final @Nonnull NonNullableConceptPropertyTable<Password> table = NonNullableConceptPropertyTable.get("password", Password.FACTORY);
    
    /**
     * Stores the value of this password.
     */
    public final @Nonnull NonNullableConceptProperty<String, Password> value;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private static final @Nonnull Index<Password, Void> index = Index.get();
    
    /**
     * Returns the (locally cached) password of the given entity.
     * 
     * @param entity the entity to which the password belongs.
     * 
     * @return a new or existing context with the given entity and number.
     * 
     * @require !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Password get(@Nonnull NonHostEntity entity) throws SQLException {
        assert !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
        
        final @Nonnull String value = PasswordModule.get(entity);
        if (Database.isSingleAccess()) {
            @Nullable Password password = index.get(entity);
            if (password == null) password = index.putIfAbsentElseReturnPresent(entity, new Password(entity, value));
            return password;
        } else {
            return new Password(entity, value);
        }
    }
    
    /**
     * Resets the password of the given entity after having reloaded the passwords module.
     * 
     * @param entity the entity whose password is to be reset.
     */
    @NonCommitting
    public static void reset(@Nonnull NonHostEntity entity) throws SQLException {
        if (Database.isSingleAccess()) {
            final @Nullable Password password = index.get(entity);
            if (password != null) {
                password.value = PasswordModule.get(entity);
                password.notify(RESET);
            }
        }
    }
    
    /**
     * Creates a new password with the given entity and value.
     * 
     * @param entity the entity to which the password belongs.
     * @param value the value of the newly created password.
     * 
     * @require isValid(value) : "The value is valid.";
     */
    private Password(@Nonnull NonHostEntity entity) {
        super(entity);
        
        this.value = NonNullableConceptProperty.get();
    }
    
    /**
     * Replaces the value of this password.
     * 
     * @param oldValue the old value of this password.
     * @param newValue the new value of this password.
     * 
     * @require isValid(oldValue) : "The old value is valid.";
     * @require isValid(newValue) : "The new value is valid.";
     */
    @NonCommitting
    void replaceName(@Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
        PasswordModule.replace(this, oldValue, newValue);
        this.value = newValue;
        notify(VALUE);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Password)) return false;
        final @Nonnull Password other = (Password) object;
        return this.getEntity().equals(other.getEntity()) && this.value.equals(other.value);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 41 * getEntity().hashCode() + value.hashCode();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "The password of " + getEntity().getIdentity().getAddress() + " is '" + value + "'.";
    }
    
}
