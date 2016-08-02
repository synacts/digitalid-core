package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptInfo;

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
public abstract class ConceptPropertyInfo<V, C extends Concept<C, E, ?>, E extends Entity> {
    
    /* -------------------------------------------------- Property Name -------------------------------------------------- */
    
    /**
     * Returns the name of the property (unique within the module).
     */
    @Pure
    public abstract @Nonnull @Validated String getPropertyName();
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
    
    /**
     * Returns the converters to convert and recover the value of the property.
     */
    @Pure
    public abstract @Nonnull Converters<V, ? super E> getValueConverters();
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Returns the required authorization to set the property and see its changes.
     */
    @Pure
    public abstract @Nonnull RequiredAuthorization<C> getRequiredAuthorization();
    
    /* -------------------------------------------------- Value Validator -------------------------------------------------- */
    
    /**
     * Returns the value validator that checks whether the value of the property is valid.
     */
    @Pure
    public abstract @Nonnull ValueValidator<V> getValueValidator();
    
    /* -------------------------------------------------- Concept Info -------------------------------------------------- */
    
    /**
     * Returns the concept info.
     */
    @Pure
    public abstract @Nonnull ConceptInfo<C, E, ?> getConceptInfo();
    
    /* -------------------------------------------------- Concept Type -------------------------------------------------- */
    
    /**
     * Returns the semantic type to encode the value of the property.
     */
    @Pure
    // TODO: Derive SemanticType.map(propertyName + conceptSetup.getConceptType().getAddress().getStringWithDot()).load(valueConverters.getXDFConverter().getType())
    public abstract @Nonnull SemanticType getPropertyType();
    
    /* -------------------------------------------------- Action Type -------------------------------------------------- */
    
    /**
     * Returns the type of the concept property internal action.
     */
    @Pure
    public abstract @Nonnull @Loaded SemanticType getActionType();
    
    /* -------------------------------------------------- Property Table -------------------------------------------------- */
    
    /**
     * Returns the database table that stores the value of the property.
     */
    @Pure
    public abstract @Nonnull ConceptPropertyTable<V, C, E> getPropertyTable();
    
}
