package net.digitalid.core.password;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.None;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.Index;
import net.digitalid.core.data.StateModule;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.property.ValueValidator;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.core.property.nonnullable.NonNullableConceptPropertyTable;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.wrappers.EmptyWrapper;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models a password of a digital identity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Password extends Concept<Password, NonHostEntity, None> {
    
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
    
    private static final @Nonnull StateModule MODULE = StateModule.get(CoreService.SERVICE, "password");
    
    /**
     * Stores the table to store the password.
     */
    private static final @Nonnull NonNullableConceptPropertyTable<String, Password, NonHostEntity> table = NonNullableConceptPropertyTable.get(MODULE, "password", Password.FACTORY);
    
    /**
     * Stores the value of this password.
     */
    public final @Nonnull NonNullableConceptProperty<String, Password, NonHostEntity> value = NonNullableConceptProperty.get(VALIDATOR, this, table);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the index of this concept.
     */
    private static final @Nonnull Index<Password, NonHostEntity, None> index = Index.get(EmptyWrapper.VALUE_FACTORY);
    
    /**
     * Returns a potentially cached password that might not yet exist in the database.
     * 
     * @param entity the entity to which the password belongs.
     * 
     * @return a new or existing password with the given entity.
     * 
     * @require !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Password get(@Nonnull NonHostEntity entity) {
        assert !(entity instanceof Role) || ((Role) entity).isNative() : "If the entity is a role, it is native.";
        
        return index.get(entity, None.OBJECT);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new password with the given entity and value.
     * 
     * @param entity the entity to which the password belongs.
     * @param value the value of the newly created password.
     * 
     * @require isValid(value) : "The value is valid.";
     */
    private Password(@Nonnull NonHostEntity entity) {
        super(entity, None.OBJECT);
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends Concept.IndexBasedGlobalFactory<Password, NonHostEntity, None> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(EmptyWrapper.VALUE_FACTORY, index);
        }
        
        @Pure
        @Override
        public @Nonnull Password create(@Nonnull NonHostEntity entity, @Nonnull None key) {
            return new Password(entity);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
