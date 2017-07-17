package net.digitalid.core.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentPropertyEntry;
import net.digitalid.database.property.PersistentPropertyTable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.property.map.SynchronizedMapPropertyTable;
import net.digitalid.core.property.set.SynchronizedSetPropertyTable;
import net.digitalid.core.property.value.SynchronizedValuePropertyTable;
import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.subject.CoreSubjectModule;
import net.digitalid.core.unit.CoreUnit;

/**
 * A synchronized property table belongs to a {@link CoreSubjectModule core subject module} and stores the {@link PersistentPropertyEntry property entries}.
 * 
 * @see SynchronizedMapPropertyTable
 * @see SynchronizedSetPropertyTable
 * @see SynchronizedValuePropertyTable
 */
@Immutable
public interface SynchronizedPropertyTable<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable ENTRY extends PersistentPropertyEntry<SUBJECT>, VALUE> extends PersistentPropertyTable<CoreUnit, SUBJECT, ENTRY> {
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CoreSubjectModule<ENTITY, KEY, SUBJECT> getParentModule();
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Returns the name of the property (unique within the module).
     */
    @Pure
    public @Nonnull RequiredAuthorization<ENTITY, KEY, SUBJECT, VALUE> getRequiredAuthorization();
    
    /* -------------------------------------------------- Action Type -------------------------------------------------- */
    
    /**
     * Returns the type of the synchronized property internal action.
     */
    @Pure
    @TODO(task = "Generate this here?", date = "2016-11-14", author = Author.KASPAR_ETTER)
    public @Nonnull /* @Loaded */ SemanticType getActionType();
    
}
