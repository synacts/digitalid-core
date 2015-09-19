package net.digitalid.core.property;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.data.StateModule;
import net.digitalid.core.data.StateTable;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.factory.GlobalFactory;

/**
 * This class models a database table that stores a {@link ReadOnlyProperty property} of a {@link Concept concept}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class ConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends StateTable {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory to store and restore the concept.
     */
    private final @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory;
    
    /**
     * Returns the factory to store and restore the concept.
     * 
     * @return the factory to store and restore the concept.
     */
    @Pure
    public final @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> getConceptFactory() {
        return conceptFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory to store and restore the value of the property.
     */
    private final @Nonnull GlobalFactory<V, ? super E> valueFactory;
    
    /**
     * Returns the factory to store and restore the value of the property.
     * 
     * @return the factory to store and restore the value of the property.
     */
    @Pure
    public final @Nonnull GlobalFactory<V, ? super E> getValueFactory() {
        return valueFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept property table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     * @param dumpType the dump type of the new table.
     * @param stateType the state type of the new table.
     * @param conceptFactory the factory to store and restore the concept.
     * @param valueFactory the factory to store and restore the value of the property.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected ConceptPropertyTable(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType, @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory, @Nonnull GlobalFactory<V, ? super E> valueFactory) {
        super(module, name, dumpType, stateType);
        
        this.conceptFactory = conceptFactory;
        this.valueFactory = valueFactory;
    }
    
}
