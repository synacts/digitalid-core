package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.subject.SubjectModule;

import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;

/**
 * Objects of this class store (static) information about a {@link CoreSubject concept}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class CoreSubjectModule<ENTITY extends Entity<?>, KEY, CONCEPT extends CoreSubject<ENTITY, KEY>> extends SubjectModule<CoreSite<?>, CONCEPT> {
    
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
    protected abstract @Nonnull BinaryFunction<@Nonnull ENTITY, @Nonnull KEY, @Nonnull CONCEPT> getConceptFactory();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the index used to cache instances of the concept.
     */
    @Pure
    @Derive("new ConceptIndexSubclass<>(this)")
    public abstract @Nonnull CoreSubjectIndex<ENTITY, KEY, CONCEPT> getConceptIndex();
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Returns the converter used to convert and recover the entity.
     */
    @Pure
    public abstract @Nonnull Converter<ENTITY, @Nonnull CoreSite<?>> getEntityConverter();
    
    /**
     * Returns the converter used to convert and recover the concept.
     */
    @Pure
    public abstract @Nonnull Converter<CONCEPT, @Nonnull ENTITY> getConceptConverter();
    
    @Pure
    @Override
    @Derive("new SubjectConverterSubclass<ENTITY, KEY, CONCEPT>(Concept.class, \"SubjectConverter\", \"net.digitalid.core.concept\", this)")
    public abstract @Nonnull Converter<CONCEPT, @Nonnull CoreSite<?>> getSubjectConverter();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getName() {
        return getConceptConverter().getTypeName();
    }
    
}
