package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentPropertyEntry;
import net.digitalid.database.property.PersistentPropertyTable;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptModule;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.annotations.type.loaded.Loaded;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This factory creates a new property for each concept instance and stores the required converters and methods.
 */
@Immutable
public interface SynchronizedPropertyTable<E extends Entity, K, C extends Concept<E, K>, N extends PersistentPropertyEntry<C>, A extends PropertyRequiredAuthorization<E, K, C>> extends PersistentPropertyTable<C, N> {
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ConceptModule<E, K, C> getParentModule();
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Returns the name of the property (unique within the module).
     */
    @Pure
    public @Nonnull A getRequiredAuthorization();
    
    /* -------------------------------------------------- Action Type -------------------------------------------------- */
    
    /**
     * Returns the type of the synchronized property internal action.
     */
    @Pure
    @TODO(task = "Generate this here?", date = "2016-11-14", author = Author.KASPAR_ETTER)
    public @Nonnull @Loaded SemanticType getActionType();
    
}
