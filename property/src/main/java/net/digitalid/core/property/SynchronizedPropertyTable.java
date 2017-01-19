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
import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.annotations.type.loaded.Loaded;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This factory creates a new property for each concept instance and stores the required converters and methods.
 */
@Immutable
public interface SynchronizedPropertyTable<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, ENTRY extends PersistentPropertyEntry<CONCEPT>, VALUE> extends PersistentPropertyTable<CoreSite<?>, CONCEPT, ENTRY> {
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ConceptModule<ENTITY, KEY, CONCEPT> getParentModule();
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Returns the name of the property (unique within the module).
     */
    @Pure
    public @Nonnull RequiredAuthorization<ENTITY, KEY, CONCEPT, VALUE> getRequiredAuthorization();
    
    /* -------------------------------------------------- Action Type -------------------------------------------------- */
    
    /**
     * Returns the type of the synchronized property internal action.
     */
    @Pure
    @TODO(task = "Generate this here?", date = "2016-11-14", author = Author.KASPAR_ETTER)
    public @Nonnull @Loaded SemanticType getActionType();
    
}
