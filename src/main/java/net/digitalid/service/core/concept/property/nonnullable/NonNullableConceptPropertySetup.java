package net.digitalid.service.core.concept.property.nonnullable;

import net.digitalid.utility.database.site.Site;

import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.factory.ConceptFactories;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.dataservice.StateModule;
import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.property.ConceptPropertyInternalAction;
import net.digitalid.service.core.concept.property.ConceptPropertySetup;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This is the factory for non-nullable concept properties.
 */
@Immutable
public final class NonNullableConceptPropertySetup<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends ConceptPropertySetup<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Old Value Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of the old value in the internal action.
     */
    private final @Nonnull @Loaded SemanticType oldValueType = SemanticType.map("old" + getPropertyType().getAddress().getStringWithDot()).load(getValueFactories().getEncodingFactory().getType());
    
    /**
     * Returns the type of the old value in the internal action.
     * 
     * @return the type of the old value in the internal action.
     */
    @Pure
    public @Nonnull @Loaded SemanticType getOldValueType() {
        return oldValueType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– New Value Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of the new value in the internal action.
     */
    private final @Nonnull @Loaded SemanticType newValueType = SemanticType.map("new" + getPropertyType().getAddress().getStringWithDot()).load(getValueFactories().getEncodingFactory().getType());
    
    /**
     * Returns the type of the new value in the internal action.
     * 
     * @return the type of the new value in the internal action.
     */
    @Pure
    public @Nonnull @Loaded SemanticType getNewValueType() {
        return newValueType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Action Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of the concept property internal action.
     */
    private final @Nonnull @Loaded SemanticType actionType = SemanticType.map("action" + getPropertyType().getAddress().getStringWithDot()).load(TupleWrapper.TYPE, getConceptFactories().getEncodingFactory().getType(), ConceptPropertyInternalAction.OLD_TIME, ConceptPropertyInternalAction.NEW_TIME, oldValueType, newValueType);
    
    @Pure
    @Override
    public @Nonnull @Loaded SemanticType getActionType() {
        return actionType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Default Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the default value for the properties created by this factory.
     */
    private final @Nonnull V defaultValue;
    
    /**
     * Returns the default value for the properties created by this factory.
     * 
     * @return the default value for the properties created by this factory.
     */
    @Pure
    public @Nonnull V getDefaultValue() {
        return defaultValue;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Property Table –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the database table that stores the value of the property.
     */
    private final @Nonnull NonNullableConceptPropertyTable<V, C, E> propertyTable;
    
    @Pure
    @Override
    public @Nonnull NonNullableConceptPropertyTable<V, C, E> getPropertyTable() {
        return propertyTable;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept property factory with the given parameters.
     * 
     * @param stateModule the module to which the property table belongs.
     * @param propertyName the name of the property (unique within the module).
     * @param entityFactories the factories to convert and reconstruct the entity.
     * @param conceptFactories the factories to convert and reconstruct the concept.
     * @param valueFactories the factories to convert and reconstruct the value of the property.
     * @param requiredAuthorization the required authorization to set the property and see its changes.
     * @param valueValidator the value validator that checks whether the value of the property is valid.
     * @param defaultValue the default value for the properties created by this factory.
     */
    private NonNullableConceptPropertySetup(@Nonnull StateModule stateModule, @Nonnull @Validated String propertyName, @Nonnull Factories<E, Site> entityFactories, @Nonnull ConceptFactories<C, E> conceptFactories, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull RequiredAuthorization<C> requiredAuthorization, @Nonnull ValueValidator<V> valueValidator, @Nonnull V defaultValue) {
        super(stateModule, propertyName, entityFactories, conceptFactories, valueFactories, requiredAuthorization, valueValidator);
        
        this.defaultValue = defaultValue;
        this.propertyTable = new NonNullableConceptPropertyTable<>(this);
        
        new NonNullableConceptPropertyInternalAction.Factory<>(this);
    }
    
    /**
     * Creates a new concept property factory with the given parameters.
     * 
     * @param stateModule the module to which the property table belongs.
     * @param propertyName the name of the property (unique within the module).
     * @param entityFactories the factories to convert and reconstruct the entity.
     * @param conceptFactories the factories to convert and reconstruct the concept.
     * @param valueFactories the factories to convert and reconstruct the value of the property.
     * @param requiredAuthorization the required authorization to set the property and see its changes.
     * @param valueValidator the value validator that checks whether the value of the property is valid.
     * @param defaultValue the default value for the properties created by this factory.
     * 
     * @return a new non-nullable concept property factory with the given parameters.
     */
    @Pure
    public static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity<E>> NonNullableConceptPropertySetup<V, C, E> get(@Nonnull StateModule stateModule, @Nonnull @Validated String propertyName, @Nonnull Factories<E, Site> entityFactories, @Nonnull ConceptFactories<C, E> conceptFactories, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull RequiredAuthorization<C> requiredAuthorization, @Nonnull ValueValidator<V> valueValidator, @Nonnull V defaultValue) {
        return new NonNullableConceptPropertySetup<>(stateModule, propertyName, entityFactories, conceptFactories, valueFactories, requiredAuthorization, valueValidator, defaultValue);
    }
    
}
