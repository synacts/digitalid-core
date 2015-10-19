package net.digitalid.service.core.password;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.Index;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.StateSelector;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.service.core.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.service.core.property.nonnullable.NonNullableConceptPropertyTable;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.wrappers.EmptyWrapper;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a password of a digital identity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class Password extends Concept<Password, NonHostEntity, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code password@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("password@core.digitalid.net").load(StringWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the password module.
     */
    private static final @Nonnull StateModule MODULE = StateModule.get(CoreService.SERVICE, "password");
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Selector –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the validator of this class.
     */
    public static final @Nonnull StateSelector SELECTOR = new StateSelector() {
        @Pure
        @Override
        public @Nonnull String getCondition(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) {
            return Database.toBoolean(restrictions.isClient());
        }
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the table to store the password.
     */
    private static final @Nonnull NonNullableConceptPropertyTable<String, Password, NonHostEntity> TABLE = NonNullableConceptPropertyTable.get(MODULE, "", NonHostEntity.FACTORY, Password.FACTORY, StringWrapper.getValueFactory(TYPE), SELECTOR);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the validator of this class.
     */
    public static final @Nonnull ValueValidator<String> VALIDATOR = new ValueValidator<String>() {
        @Pure
        @Override
        public boolean isValid(@Nonnull String value) {
            return value.length() <= 50;
        }
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this password.
     */
    public final @Nonnull NonNullableConceptProperty<String, Password, NonHostEntity> value = NonNullableConceptProperty.get(VALIDATOR, this, TABLE);
    
    // Alternative:
//    public final @Nonnull NonNullableConceptProperty<String, Password, NonHostEntity> value = new NonNullableConceptProperty(this, TABLE) {
//        @Pure
//        @Override
//        public boolean isValid(@Nonnull String value) {
//            return value.length() <= 50;
//        }
//    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the index of this concept.
     */
    private static final @Nonnull Index<Password, NonHostEntity, Object> index = Index.get(EmptyWrapper.VALUE_FACTORY);
    
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
    public static final class Factory extends Concept.IndexBasedGlobalFactory<Password, NonHostEntity, Object> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(EmptyWrapper.VALUE_FACTORY, index);
        }
        
        @Pure
        @Override
        public @Nonnull Password create(@Nonnull NonHostEntity entity, @Nonnull Object key) {
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
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull ReadOnlyPair<BlockableFactory, StorableFactory> FACTORIES = FreezablePair.get(BLOCKABLE_FACTORY, STORABLE_FACTORY).freeze();
    
}
