package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.functional.interfaces.BinaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.storage.Table;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.subject.SubjectModule;

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
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Module getParentModule() {
        return getService().getModule();
    }
    
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
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    /**
     * Returns the converter used to convert and recover the entity.
     */
    @Pure
    public abstract @Nonnull Table<ENTITY, @Nonnull CoreUnit> getEntityTable();
    
    /**
     * Returns the converter used to convert and recover the core subject.
     */
    @Pure
    public abstract @Nonnull Table<SUBJECT, @Nonnull ENTITY> getCoreSubjectTable();
    
    @Pure
    @Override
    @Derive("new CoreSubjectTableSubclass<ENTITY, KEY, SUBJECT>(this)")
    public abstract @Nonnull CoreSubjectTable<ENTITY, KEY, SUBJECT> getSubjectTable();
    
}
