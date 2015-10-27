package net.digitalid.service.core.concepts.password;

import net.digitalid.service.core.block.wrappers.EmptyWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;

import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.ConceptSetup;
import net.digitalid.service.core.concept.Index;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptPropertySetup;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a password of a digital identity.
 */
public final class Password extends Concept<Password, NonHostEntity, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new password that belongs to the given entity.
     * 
     * @param entity the entity to which the password belongs.
     */
    private Password(@Nonnull NonHostEntity entity) {
        super(entity, None.OBJECT, SETUP);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    private static final class Factory extends Concept.Factory<Password, NonHostEntity, Object> {
        
        @Pure
        @Override
        public @Nonnull Password create(@Nonnull NonHostEntity entity, @Nonnull Object key) {
            return new Password(entity);
        }
        
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the index of this concept.
     */
    private static final @Nonnull Index<Password, NonHostEntity, Object> INDEX = Index.get(new Factory());
    
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
        
        return INDEX.get(entity, None.OBJECT);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Setup –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the setup of this concept.
     */
    public static final @Nonnull ConceptSetup<Password, NonHostEntity, Object> SETUP = ConceptSetup.get(CoreService.SERVICE, "password", INDEX, EmptyWrapper.getValueFactories(EmptyWrapper.SEMANTIC), NonHostEntity.FACTORIES);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Required Authorization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the required authorization to set the property and see its changes.
     */
    public static final @Nonnull RequiredAuthorization<Password> REQUIRED_AUTHORIZATION = new RequiredAuthorization<Password>() {
        
        @Pure
        @Override
        public @Nonnull String getStateFilter(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) {
            return Database.toBoolean(restrictions.isClient());
        }
        
        @Pure
        @Override
        public @Nonnull ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull Password password) {
            return FreezableAgentPermissions.GENERAL_WRITE; // TODO
        }
        
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value validator of the value property.
     */
    public static final @Nonnull ValueValidator<String> VALUE_VALIDATOR = new ValueValidator<String>() {
        @Pure
        @Override
        public boolean isValid(@Nonnull String value) {
            return value.length() <= 50;
        }
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Property –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the setup of the value property.
     */
    private static final @Nonnull NonNullableConceptPropertySetup<String, Password, NonHostEntity> VALUE_PROPERTY_SETUP = NonNullableConceptPropertySetup.get(SETUP, "value", StringWrapper.getValueFactories(StringWrapper.SEMANTIC), REQUIRED_AUTHORIZATION, VALUE_VALIDATOR, "");
    
    /**
     * Stores the value of this password.
     */
    public final @Nonnull NonNullableConceptProperty<String, Password, NonHostEntity> value = NonNullableConceptProperty.get(VALUE_PROPERTY_SETUP, this);
    
}
