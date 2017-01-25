package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.type.Embedded;
import net.digitalid.database.subject.Subject;

import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.subject.annotations.GenerateCoreSubjectModule;

/**
 * This class models a core subject in the {@link DatabaseUtility database}.
 * A core subject always belongs to an {@link Entity entity}.
 * 
 * @param <ENTITY> either {@link Entity} for a general core subject or {@link NonHostEntity} for a core subject that exists only for non-hosts.
 *            (The type has to be a supertype of {@link NonHostEntity}, which cannot be declared in Java, unfortunately!)
 * @param <KEY> the type of the key which identifies an instance among all instances of a core subject at the same entity.
 */
@Immutable
public abstract class CoreSubject<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY> extends RootClass implements Subject<CoreUnit> {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this core subject belongs.
     */
    @Pure
    @Provided
    public abstract @Nonnull ENTITY getEntity();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the key which identifies this core subject.
     */
    @Pure
    @Embedded // TODO: Depends on the key type!
    public abstract @Nonnull KEY getKey();
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CoreUnit getUnit() {
        return getEntity().getUnit();
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Generates and returns the {@link CoreSubjectModule} required to store synchronized properties.
     */
    @Pure
    @Override
    @GenerateCoreSubjectModule
    public abstract @Nonnull CoreSubjectModule<ENTITY, KEY, ?> module();
    
    /* -------------------------------------------------- Properties -------------------------------------------------- */
    
    // TODO
    
//    /**
//     * Stores the properties of this core subject.
//     */
//    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ConceptProperty<?, C, E>> properties = FreezableLinkedList.get();
//    
//    /**
//     * Registers the given property at this core subject.
//     * 
//     * @param property the property to be registered.
//     * 
//     * @require property.getConcept() == this : "The given property belongs to this core subject.";
//     */
//    public void register(@Nonnull ConceptProperty<?, C, E> property) {
//        Require.that(property.getConcept() == this).orThrow("The given property belongs to this core subject.");
//        
//        properties.add(property);
//    }
//    
//    /**
//     * Returns the properties of this core subject.
//     */
//    @Pure
//    public final @Nonnull @NonNullableElements ReadOnlyList<ConceptProperty<?, C, E>> getProperties() {
//        return properties;
//    }
//    
//    /**
//     * Returns the property of this core subject with the given table.
//     */
//    @Pure
//    public final @Nonnull ConceptProperty<?, C, E> getProperty(@Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException { // TODO: Change the parameter to ConceptPropertySetup!
//        for (final @Nonnull ConceptProperty<?, C, E> property : properties) {
//            if (property.getConceptPropertySetup().getPropertyTable().equals(table)) { return property; }
//        }
//        throw DatabaseException.get("No property is registered for the given table.");
//    }
//    
//    /**
//     * Resets the property of this core subject with the given table.
//     * 
//     * @param table the table which initiated the reset of its properties.
//     */
//    @NonCommitting
//    public void reset(@Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException {
//        getProperty(table).reset();
//    }
//    
//    /**
//     * Resets the properties of this core subject.
//     */
//    @NonCommitting
//    public void resetAll() throws DatabaseException {
//        for (final @Nonnull ConceptProperty<?, C, E> property : properties) { property.reset(); }
//    }
    
}
