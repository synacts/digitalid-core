package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;

/**
 * This class stores the setup of a {@link Concept concept}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ConceptSetup<E extends Entity, K, C extends Concept<E, K>> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Returns the service to which the concept belongs.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /* -------------------------------------------------- Concept Name -------------------------------------------------- */
    
    /**
     * Returns the name of the concept (unique within the service).
     */
    @Pure
    public abstract @Nonnull @CodeIdentifier String getConceptName();
    
    /* -------------------------------------------------- Concept ConceptIndex -------------------------------------------------- */
    
    /**
     * Returns the index used to cache instances of the concept.
     */
    @Pure
    public abstract @Nonnull ConceptIndex<E, K, C> getConceptIndex();
    
}
