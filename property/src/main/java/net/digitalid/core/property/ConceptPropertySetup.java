package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptSetup;

import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;

/**
 * This factory creates a new property for each concept instance and stores the required converters and methods.
 */
@Immutable
public abstract class ConceptPropertySetup<V, C extends Concept<C, E, ?>, E extends Entity> {
    
    /* -------------------------------------------------- Property Name -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
    
    /**
     * Stores the converters to convert and recover the value of the property.
     */
    private final @Nonnull Converters<V, ? super E> valueConverters;
    
    /**
     * Returns the converters to convert and recover the value of the property.
     * 
     * @return the converters to convert and recover the value of the property.
     */
    @Pure
    public final @Nonnull Converters<V, ? super E> getValueConverters() {
        return valueConverters;
    }
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Value Validator -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Concept Setup -------------------------------------------------- */
    
    /**
     * Stores the setup of a concept.
     */
    private final @Nonnull ConceptSetup<C, E, ?> conceptSetup;
    
    /**
     * Returns the concept setup.
     * 
     * @return the concept setup.
     */
    @Pure
    public @Nonnull ConceptSetup<C, E, ?> getConceptSetup() {
        return conceptSetup;
    }
    
    /* -------------------------------------------------- Concept Type -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new concept property factory with the given parameters.
     * 
     * @param stateModule the module to which the property table belongs.
     * @param propertyName the name of the property (unique within the module).
     * @param entityConverters the converters to convert and recover the entity.
     * @param conceptConverters the converters to convert and recover the concept.
     * @param valueConverters the converters to convert and recover the value of the property.
     * @param requiredAuthorization the required authorization to set the property and see its changes.
     * @param valueValidator the value validator that checks whether the value of the property is valid.
     */
    protected ConceptPropertySetup(@Nonnull ConceptSetup<C, E, ?> conceptSetup, @Nonnull @Validated String propertyName, @Nonnull Converters<V, ? super E> valueConverters, @Nonnull RequiredAuthorization<C> requiredAuthorization, @Nonnull ValueValidator<V> valueValidator) {
        this.propertyName = propertyName;
        this.valueConverters = valueConverters;
        this.requiredAuthorization = requiredAuthorization;
        this.valueValidator = valueValidator;
        
        this.conceptSetup = conceptSetup;
        
        this.propertyType = SemanticType.map(propertyName + conceptSetup.getConceptType().getAddress().getStringWithDot()).load(valueConverters.getXDFConverter().getType());
    }
    
    /* -------------------------------------------------- Action Type -------------------------------------------------- */
    
    /**
     * Returns the type of the concept property internal action.
     * 
     * @return the type of the concept property internal action.
     */
    @Pure
    public abstract @Nonnull @Loaded SemanticType getActionType();
    
    /* -------------------------------------------------- Property Table -------------------------------------------------- */
    
    /**
     * Returns the database table that stores the value of the property.
     * 
     * @return the database table that stores the value of the property.
     */
    @Pure
    public abstract @Nonnull ConceptPropertyTable<V, C, E> getPropertyTable();
    
}
