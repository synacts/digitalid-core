package net.digitalid.core.password;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.OnlyForActions;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.concept.Aspect;
import net.digitalid.core.concept.Instance;
import net.digitalid.core.concept.NonHostConcept;
import net.digitalid.core.concept.Observer;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models a password of a digital identity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Password extends NonHostConcept {
    
    /**
     * Stores the aspect of the value being changed at the observed password.
     */
    public static final @Nonnull Aspect VALUE = new Aspect(Password.class, "value changed");
    
    /**
     * Stores the aspect of the password being reset after having reloaded the passwords module.
     */
    public static final @Nonnull Aspect RESET = new Aspect(Password.class, "password reset");
    
    
    /**
     * Stores the semantic type {@code password@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("password@core.digitalid.net").load(StringWrapper.TYPE);
    
    
    /**
     * Returns whether the given value is valid.
     * A valid password has at most 50 characters.
     * 
     * @param value the value to be checked.
     * 
     * @return whether the given value is valid.
     */
    @Pure
    public static boolean isValid(@Nonnull String value) {
        return value.length() <= 50;
    }
    
    
    /**
     * Stores the value of this password.
     * 
     * @invariant isValid(value) : "The value is valid.";
     */
    private @Nonnull String value;
    
    /**
     * Creates a new password with the given entity and value.
     * 
     * @param entity the entity to which the password belongs.
     * @param value the value of the newly created password.
     * 
     * @require isValid(value) : "The value is valid.";
     */
    private Password(@Nonnull NonHostEntity entity, @Nonnull String value) {
        super(entity);
        
        assert isValid(value) : "The value is valid.";
        
        this.value = value;
    }
    
    /**
     * Returns the value of this password.
     * 
     * @return the value of this password.
     * 
     * @ensure isValid(return) : "The returned value is valid.";
     */
    @Pure
    public @Nonnull String getValue() {
        return value;
    }
    
    /**
     * Sets the value of this password.
     * 
     * @param newValue the new value of this password.
     * 
     * @require isOnClient() : "The password is on a client.";
     * @require isValid(newValue) : "The new value is valid.";
     */
    @Committing
    public void setValue(@Nonnull String newValue) throws SQLException {
        if (!newValue.equals(value)) {
            Synchronizer.execute(new PasswordValueReplace(this, value, newValue));
        }
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
    @OnlyForActions
    void replaceName(@Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
        PasswordModule.replace(this, oldValue, newValue);
        this.value = newValue;
        notify(VALUE);
    }
    
    
    @Pure
    @Override
    public boolean equals(Object object) {
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
    
    
    /**
     * Caches passwords given their entity.
     */
    private static final @Nonnull ConcurrentMap<NonHostEntity, Password> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
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
    
}
