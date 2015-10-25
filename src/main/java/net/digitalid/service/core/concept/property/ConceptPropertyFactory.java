package net.digitalid.service.core.concept.property;

import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.Service;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.factories.ConceptFactories;
import net.digitalid.service.core.factories.Factories;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.RequiredAuthorization;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.column.Site;

/**
 * This factory creates a new property for each concept instance and stores the required factories and methods.
 */
@Immutable
public abstract class ConceptPropertyFactory<V, C extends Concept<C, E, ?>, E extends Entity<E>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the module to which the property table belongs.
     */
    private final @Nonnull StateModule stateModule;
    
    /**
     * Returns the module to which the property table belongs.
     * 
     * @return the module to which the property table belongs.
     */
    @Pure
    public final @Nonnull StateModule getStateModule() {
        return stateModule;
    }
    
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
    public final @Nonnull String getPropertyName() {
        return propertyName;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Entity Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the entity.
     */
    private final @Nonnull Factories<E, Site> entityFactories;
    
    /**
     * Returns the factories to convert and reconstruct the entity.
     * 
     * @return the factories to convert and reconstruct the entity.
     */
    @Pure
    public final @Nonnull Factories<E, Site> getEntityFactories() {
        return entityFactories;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the concept.
     */
    private final @Nonnull ConceptFactories<C, E> conceptFactories;
    
    /**
     * Returns the factories to convert and reconstruct the concept.
     * 
     * @return the factories to convert and reconstruct the concept.
     */
    @Pure
    public final @Nonnull ConceptFactories<C, E> getConceptFactories() {
        return conceptFactories;
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type Suffix –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the suffix of all types of the concept property action.
     */
    private final @Nonnull String typeSuffix;
    
    /**
     * Returns the suffix of all types of the concept property action.
     * 
     * @return the suffix of all types of the concept property action.
     */
    protected final @Nonnull String getTypeSuffix() {
        return typeSuffix;
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
     * 
     * @require !(stateModule instanceof Service) : "The state module is not a service.";
     */
    protected ConceptPropertyFactory(@Nonnull StateModule stateModule, @Nonnull @Validated String propertyName, @Nonnull Factories<E, Site> entityFactories, @Nonnull ConceptFactories<C, E> conceptFactories, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull RequiredAuthorization<C> requiredAuthorization, @Nonnull ValueValidator<V> valueValidator) {
        assert !(stateModule instanceof Service) : "The state module is not a service.";
        
        this.stateModule = stateModule;
        this.propertyName = propertyName;
        this.entityFactories = entityFactories;
        this.conceptFactories = conceptFactories;
        this.valueFactories = valueFactories;
        this.requiredAuthorization = requiredAuthorization;
        this.valueValidator = valueValidator;
        
        this.typeSuffix = valueFactories.getEncodingFactory().getType().getAddress().getStringWithDot();
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Property Creation –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept property with the given concept.
     * 
     * @param concept the concept to which the property belongs.
     * 
     * @return a new concept property with the given concept.
     */
    @Pure
    public abstract @Nonnull ConceptProperty<V, C, E> createConceptProperty(@Nonnull C concept);
    
}
