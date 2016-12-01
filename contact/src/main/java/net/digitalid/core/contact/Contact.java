package net.digitalid.core.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.ExternalPerson;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.Person;
import net.digitalid.core.node.Node;

/**
 * Contacts have certain {@link FreezableNodePermissions permissions} and {@link FreezableAuthentications authentications}.
 * 
 * @see InternalContact
 * @see ExternalContact
 */
@Immutable
@GenerateConverter
public abstract class Contact extends Node implements RequiredNodeAuthorizations {
    
    /* -------------------------------------------------- Person -------------------------------------------------- */
    
    /**
     * Returns the person of this contact.
     */
    @Pure
    public abstract @Nonnull Person getPerson();
    
    /* -------------------------------------------------- Supernode -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSupernodeOf(@Nonnull Node node) {
        return equals(node);
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached contact of the given entity and person that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull Contact of(@Nonnull NonHostEntity entity, @Nonnull Person person) {
        if (person instanceof InternalPerson) { return InternalContact.of(entity, (InternalPerson) person); }
        else if (person instanceof ExternalPerson) { return ExternalContact.of(entity, (ExternalPerson) person); }
        else { throw new RuntimeException("This should never happen."); } // TODO: Throw a more appropriate exception.
    }
    
}
