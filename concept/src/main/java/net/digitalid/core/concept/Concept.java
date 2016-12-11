package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.type.Embedded;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.subject.Subject;
import net.digitalid.database.subject.site.Site;

import net.digitalid.core.concept.annotations.GenerateConceptModule;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;

/**
 * This class models a concept in the {@link Database database}.
 * A concept always belongs to an {@link Entity entity}.
 * 
 * @param <E> either {@link Entity} for a general concept or {@link NonHostEntity} for a concept that exists only for non-hosts.
 *            (The type has to be a supertype of {@link NonHostEntity}, which cannot be declared in Java, unfortunately!)
 * @param <K> the type of the key which identifies an instance among all instances of a concept at the same entity.
 */
@Immutable
public abstract class Concept<E extends Entity, K> extends RootClass implements Subject {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this concept belongs.
     */
    @Pure
    @Provided
    public abstract @Nonnull E getEntity();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the key which identifies this concept.
     */
    @Pure
    @Embedded // TODO: Depends on the key type!
    public abstract @Nonnull K getKey();
    
    /* -------------------------------------------------- Site -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Site getSite() {
        return getEntity().getSite();
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Generates and returns the {@link ConceptModule} required to store synchronized properties.
     */
    @Pure
    @Override
    @GenerateConceptModule
    public abstract @Nonnull ConceptModule<E, K, ?> module();
    
    /* -------------------------------------------------- Properties -------------------------------------------------- */
    
    // TODO
    
//    /**
//     * Stores the properties of this concept.
//     */
//    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ConceptProperty<?, C, E>> properties = FreezableLinkedList.get();
//    
//    /**
//     * Registers the given property at this concept.
//     * 
//     * @param property the property to be registered.
//     * 
//     * @require property.getConcept() == this : "The given property belongs to this concept.";
//     */
//    public void register(@Nonnull ConceptProperty<?, C, E> property) {
//        Require.that(property.getConcept() == this).orThrow("The given property belongs to this concept.");
//        
//        properties.add(property);
//    }
//    
//    /**
//     * Returns the properties of this concept.
//     */
//    @Pure
//    public final @Nonnull @NonNullableElements ReadOnlyList<ConceptProperty<?, C, E>> getProperties() {
//        return properties;
//    }
//    
//    /**
//     * Returns the property of this concept with the given table.
//     * 
//     * @return the property of this concept with the given table.
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
//     * Resets the property of this concept with the given table.
//     * 
//     * @param table the table which initiated the reset of its properties.
//     */
//    @NonCommitting
//    public void reset(@Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException {
//        getProperty(table).reset();
//    }
//    
//    /**
//     * Resets the properties of this concept.
//     */
//    @NonCommitting
//    public void resetAll() throws DatabaseException {
//        for (final @Nonnull ConceptProperty<?, C, E> property : properties) { property.reset(); }
//    }
    
}
