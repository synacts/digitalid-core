package ch.virtualid.concepts;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.action.internal.PasswordValueReplace;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.both.Passwords;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.xdf.StringWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a password of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Password extends Concept {
    
    /**
     * Stores the aspect of the value being changed at the observed password.
     */
    public static final @Nonnull Aspect VALUE = new Aspect(Password.class, "value changed");
    
    /**
     * Stores the aspect of the password being reset after having reloaded the passwords module.
     */
    public static final @Nonnull Aspect RESET = new Aspect(Password.class, "password reset");
    
    
    /**
     * Stores the semantic type {@code password@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("password@virtualid.ch").load(StringWrapper.TYPE);
    
    
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
    private Password(@Nonnull Entity entity, @Nonnull String value) {
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
    @OnlyForActions
    public void replaceName(@Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
        Passwords.replace(this, oldValue, newValue);
        this.value = newValue;
        notify(VALUE);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "The password of " + getEntity().getIdentity().getAddress() + " is '" + value + "'.";
    }
    
    
    /**
     * Caches passwords given their entity.
     */
    private static final @Nonnull ConcurrentMap<Entity, Password> index = new ConcurrentHashMap<Entity, Password>();
    
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
    public static @Nonnull Password get(@Nonnull Entity entity) throws SQLException {
        assert !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
        
        final @Nonnull String value = Passwords.get(entity);
        if (Database.isSingleAccess()) {
            @Nullable Password password = index.get(entity);
            if (password == null) {
                password = index.putIfAbsentElseReturnPresent(entity, new Password(entity, value));
                entity.observe(new Observer() { @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); } }, Entity.REMOVED);
            }
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
    public static void reset(@Nonnull Entity entity) throws SQLException {
        final @Nullable Password password = index.get(entity);
        if (password != null) {
            password.value = Passwords.get(entity);
            password.notify(RESET);
        }
    }
    
}
