package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.subject.SubjectModule;
import net.digitalid.database.subject.site.Site;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;

/**
 * Objects of this class store (static) information about a {@link Concept concept}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ConceptModule<E extends Entity, K, C extends Concept<E, K>> extends SubjectModule<C> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Returns the service to which the concept belongs.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * Returns the factory that can produce a new concept instance with a given entity and key.
     */
    @Pure
    protected abstract @Nonnull BinaryFunction<@Nonnull E, @Nonnull K, @Nonnull C> getConceptFactory();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the index used to cache instances of the concept.
     */
    @Pure
    @Derive("new ConceptIndexSubclass<>(this)")
    public abstract @Nonnull ConceptIndex<E, K, C> getConceptIndex();
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Returns the converter used to convert and recover the entity.
     */
    @Pure
    public abstract @Nonnull Converter<E, @Nonnull Site> getEntityConverter();
    
    /**
     * Returns the converter used to convert and recover the concept.
     */
    @Pure
    public abstract @Nonnull Converter<C, @Nonnull E> getConceptConverter();
    
    @Pure
    @Override
    @Derive("new SubjectConverterSubclass<E, K, C>(\"SubjectConverter\", this)")
    public abstract @Nonnull Converter<C, @Nonnull Site> getSubjectConverter();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getName() {
        return getConceptConverter().getName();
    }
    
}
