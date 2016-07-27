package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;

/**
 * Objects of this class store (static) information about a {@link Concept concept}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ConceptInfo<E extends Entity, K, C extends Concept<E, K>> extends RootClass {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Returns the service to which the concept belongs.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns the name of the concept, which has to be unique within the service.
     */
    @Pure
    public abstract @Nonnull @CodeIdentifier String getName();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the index used to cache instances of the concept.
     */
    @Pure
    public abstract @Nonnull ConceptIndex<E, K, C> getIndex();
    
    /* -------------------------------------------------- Converter -------------------------------------------------- */
    
    /**
     * Returns the converter used to convert and recover the concept.
     */
    @Pure
    public abstract @Nonnull Converter<C, E> getConverter();
    
}
