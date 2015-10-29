package net.digitalid.service.core.concept.property;

import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.ConceptSetup;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This factory creates a new property for each concept instance and stores the required factories and methods.
 */
@Immutable
public abstract class ConceptPropertySetup<V, C extends Concept<C, E, ?>, E extends Entity<E>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Property Name –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the name of the property (unique within the module).
     */
    private final @Nonnull @Validated String propertyName;
    
    /**
     * Returns the name of the property (unique within the module).
     * 
     * @return the name of the property (unique within the module).
     */
    @Pure
    public final @Nonnull @Validated String getPropertyName() {
        return propertyName;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the value of the property.
     */
    private final @Nonnull Factories<V, ? super E> valueFactories;
    
    /**
     * Returns the factories to convert and reconstruct the value of the property.
     * 
     * @return the factories to convert and reconstruct the value of the property.
     */
    @Pure
    public final @Nonnull Factories<V, ? super E> getValueFactories() {
        return valueFactories;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Required Authorization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the required authorization to set the property and see its changes.
     */
    private final @Nonnull RequiredAuthorization<C> requiredAuthorization;
    
    /**
     * Returns the required authorization to set the property and see its changes.
     * 
     * @return the required authorization to set the property and see its changes.
     */
    @Pure
    public final @Nonnull RequiredAuthorization<C> getRequiredAuthorization() {
        return requiredAuthorization;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value validator that checks whether the value of the property is valid.
     */
    private final @Nonnull ValueValidator<V> valueValidator;
    
    /**
     * Returns the value validator that checks whether the value of the property is valid.
     * 
     * @return the value validator that checks whether the value of the property is valid.
     */
    @Pure
    public final @Nonnull ValueValidator<V> getValueValidator() {
        return valueValidator;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type to encode the value of the property.
     */
    private final @Nonnull SemanticType propertyType;
    
    /**
     * Returns the semantic type to encode the value of the property.
     * 
     * @return the semantic type to encode the value of the property.
     */
    @Pure
    public @Nonnull SemanticType getPropertyType() {
        return propertyType;
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
     */
    protected ConceptPropertySetup(@Nonnull ConceptSetup<C, E, ?> conceptSetup, @Nonnull @Validated String propertyName, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull RequiredAuthorization<C> requiredAuthorization, @Nonnull ValueValidator<V> valueValidator) {
        this.propertyName = propertyName;
        this.valueFactories = valueFactories;
        this.requiredAuthorization = requiredAuthorization;
        this.valueValidator = valueValidator;
        
        this.propertyType = SemanticType.map(propertyName + conceptSetup.getConceptType().getAddress().getStringWithDot()).load(valueFactories.getEncodingFactory().getType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Action Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the type of the concept property internal action.
     * 
     * @return the type of the concept property internal action.
     */
    @Pure
    public abstract @Nonnull @Loaded SemanticType getActionType();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Property Table –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the database table that stores the value of the property.
     * 
     * @return the database table that stores the value of the property.
     */
    @Pure
    public abstract @Nonnull ConceptPropertyTable<V, C, E> getPropertyTable();
    
}
