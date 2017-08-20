package net.digitalid.core.node.contact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.set.FreezableLinkedHashSetBuilder;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.math.modulo.Uneven;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.Person;
import net.digitalid.core.node.ExtendedNode;
import net.digitalid.core.node.context.Context;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;
import net.digitalid.core.typeset.permissions.FreezableNodePermissions;

/**
 * Contacts have certain {@link FreezableNodePermissions permissions} and {@link FreezableAuthentications authentications}.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter(table = "unit_core_Contact_Contact") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
public abstract class Contact extends Node implements ExtendedNode {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull @Uneven Long getKey();
    
    /* -------------------------------------------------- Person -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the person.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Contact, @Nullable Person> PERSON = RequiredAuthorizationBuilder.<NonHostEntity, Long, Contact, Person>withRequiredRestrictionsToExecuteMethod((contact, person) -> RestrictionsBuilder.withWriteToNode(true).withNode(contact).build()).withRequiredRestrictionsToSeeMethod((contact, person) -> RestrictionsBuilder.buildWithNode(contact)).build();
    
    /**
     * Returns the person of this contact.
     */
    @Pure
    @Default("null")
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Contact, @Nullable Person> person();
    
    @Pure
    @TODO(task = "Reimplemented this method temporarily until person is made the key again.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public @Nonnull Person getPerson() {
        try {
            return person().get();
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /* -------------------------------------------------- Supernode -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSupernodeOf(@Nonnull Node node) {
        return equals(node);
    }
    
    /* -------------------------------------------------- Supercontexts -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Implement.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public @Nonnull @NonFrozen @NonNullableElements ReadOnlySet<Context> getSupercontexts() throws DatabaseException, RecoveryException {
        return FreezableLinkedHashSetBuilder.build();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached contact of the given entity with the given key that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull Contact of(@Nonnull NonHostEntity entity, @Uneven long key) throws DatabaseException {
        final @Nonnull Contact contact = ContactSubclass.MODULE.getSubjectIndex().get(entity, key);
        SQL.insertOrReplace(ContactSubclass.SUPER_MODULE.getSubjectTable(), contact, contact.getUnit());
        return contact;
    }
    
    /**
     * Returns the potentially cached contact of the given entity for the given person that might not yet exist in the database.
     */
    @Pure
    @TODO(task = "Maybe we should still make person the key (and use e.g. negative values to denote contexts).", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public static @Nonnull Contact of(@Nonnull NonHostEntity entity, @Nonnull Person person) throws DatabaseException {
        throw new UnsupportedOperationException();
    }
    
}
