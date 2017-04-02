package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
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

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;
import net.digitalid.core.unit.CoreUnit;

/**
 * Objects of this class store (static) information about a {@link CoreSubject core subject}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class CoreSubjectModule<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>> extends SubjectModule<CoreUnit, SUBJECT> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Returns the service to which the core subject belongs.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * Returns the factory that can produce a new core subject instance with a given entity and key.
     */
    @Pure
    protected abstract @Nonnull BinaryFunction<@Nonnull ENTITY, @Nonnull KEY, @Nonnull SUBJECT> getSubjectFactory();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the index used to cache instances of the core subject.
     */
    @Pure
    @Derive("new CoreSubjectIndexSubclass<>(this)")
    public abstract @Nonnull CoreSubjectIndex<ENTITY, KEY, SUBJECT> getSubjectIndex();
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Returns the converter used to convert and recover the entity.
     */
    @Pure
    public abstract @Nonnull Converter<ENTITY, @Nonnull CoreUnit> getEntityConverter();
    
    /**
     * Returns the converter used to convert and recover the core subject.
     */
    @Pure
    public abstract @Nonnull Converter<SUBJECT, @Nonnull ENTITY> getCoreSubjectConverter();
    
    @Pure
    @Override
    @Derive("new CoreSubjectConverterSubclass<ENTITY, KEY, SUBJECT>(CoreSubject.class, \"CoreSubjectConverter\", \"net.digitalid.core.subject\", this)")
    public abstract @Nonnull Converter<SUBJECT, @Nonnull CoreUnit> getSubjectConverter();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getName() {
        return getCoreSubjectConverter().getTypeName();
    }
    
}
