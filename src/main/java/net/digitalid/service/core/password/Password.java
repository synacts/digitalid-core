package net.digitalid.service.core.password;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.ConceptEncodingFactory;
import net.digitalid.service.core.concept.ConceptStoringFactory;
import net.digitalid.service.core.concept.Index;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.factories.ConceptFactories;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptProperty;
import net.digitalid.service.core.concept.property.nonnullable.NonNullableConceptPropertyFactory;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.wrappers.EmptyWrapper;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a password of a digital identity.
 */
public final class Password extends Concept<Password, NonHostEntity, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    // TODO: Create the types dynamically?
    
    /**
     * Stores the semantic type {@code password@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("password@core.digitalid.net").load(EmptyWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code value.password@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType VALUE_TYPE = SemanticType.map("value.password@core.digitalid.net").load(StringWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the password module.
     */
    private static final @Nonnull StateModule MODULE = StateModule.get(CoreService.SERVICE, "password");
    
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
     * Stores the factory of the value property.
     */
    private static final @Nonnull NonNullableConceptPropertyFactory<String, Password, NonHostEntity> VALUE_PROPERTY_FACTORY = NonNullableConceptPropertyFactory.get(MODULE, "value", NonHostEntity.FACTORIES, Password.FACTORIES, StringWrapper.getValueFactories(VALUE_TYPE), REQUIRED_AUTHORIZATION, VALUE_VALIDATOR, "");
    
    /**
     * Stores the value of this password.
     */
    public final @Nonnull NonNullableConceptProperty<String, Password, NonHostEntity> value = VALUE_PROPERTY_FACTORY.createConceptProperty(this);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new password that belongs to the given entity.
     * 
     * @param entity the entity to which the password belongs.
     */
    private Password(@Nonnull NonHostEntity entity) {
        super(entity, None.OBJECT);
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encoding factory which is used to encode and decode this concept.
     */
    public static final @Nonnull ConceptEncodingFactory<Password, NonHostEntity, Object> ENCODING_FACTORY = ConceptEncodingFactory.get(EmptyWrapper.getValueEncodingFactory(TYPE), INDEX);
    
    @Pure
    @Override
    public @Nonnull ConceptEncodingFactory<Password, NonHostEntity, Object> getEncodingFactory() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory which is used to store and restore this concept.
     */
    public static final @Nonnull ConceptStoringFactory<Password, NonHostEntity, Object> STORING_FACTORY = ConceptStoringFactory.get(EmptyWrapper.getValueStoringFactory(TYPE), INDEX);
    
    @Pure
    @Override
    public @Nonnull ConceptStoringFactory<Password, NonHostEntity, Object> getStoringFactory() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories which are used to convert and reconstruct this concept.
     */
    public static final @Nonnull ConceptFactories<Password, NonHostEntity> FACTORIES = ConceptFactories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
