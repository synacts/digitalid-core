package net.digitalid.core.node.context;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.set.FreezableLinkedHashSetBuilder;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.math.modulo.Even;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.node.ExtendedNode;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models the contexts for {@link Contact contacts}.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter(table = "unit_core_Context_Context") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
public abstract class Context extends Node implements ExtendedNode {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull @Even Long getKey();
    
    /* -------------------------------------------------- Root -------------------------------------------------- */
    
    /**
     * Stores the number of the root context.
     */
    public static final long ROOT = 0L;
    
    /**
     * Returns whether this context is the root.
     */
    @Pure
    public boolean isRoot() {
        return getKey() == ROOT;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the password.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Context, String> NAME = RequiredAuthorizationBuilder.<NonHostEntity, Long, Context, String>withRequiredRestrictionsToExecuteMethod((context, name) -> RestrictionsBuilder.withWriteToNode(true).withNode(context).build()).withRequiredRestrictionsToSeeMethod((context, name) -> RestrictionsBuilder.buildWithNode(context)).build();
    
    /**
     * Returns the name of this context.
     */
    @Pure
    @Default("\"New Context\"")
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Context, @Nonnull @MaxSize(50) String> name();
    
    /* -------------------------------------------------- Preferences -------------------------------------------------- */
    
    // TODO: Declare a set property with the attribute types that are requested if a new contact is added (indirectly) to this context.
    
    /* -------------------------------------------------- Subcontexts -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the subcontexts.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Context, Context> SUBCONTEXTS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Context, Context>withRequiredRestrictionsToExecuteMethod((context, subcontext) -> RestrictionsBuilder.withWriteToNode(true).withNode(context).build()).withRequiredRestrictionsToSeeMethod((context, subcontext) -> RestrictionsBuilder.buildWithNode(context)).build();
    
    /**
     * Returns the direct subcontexts of this context.
     */
    // TODO: The generated foreign key reference is still wrong because it does not include the provided entity and gets the column name wrong.
//    @Pure
//    @GenerateSynchronizedProperty
//    @TODO(task = "Support simple synchronized set properties?", date = "2017-08-20", author = Author.KASPAR_ETTER)
//    public abstract @Nonnull WritablePersistentSetProperty<Context, /* TODO: @Matching */ Context, ReadOnlySet<Context>, FreezableSet<Context>> subcontexts();
    
    /**
     * Returns all subcontexts of this context (including this context).
     */
    @Pure
    @NonCommitting
    @TODO(task = "Either retrieve all subcontexts by traversing the direct subcontexts or querying the database.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public @Capturable @Nonnull @NonFrozen FreezableSet<Context> getAllSubcontexts() throws DatabaseException {
        throw new UnsupportedOperationException();
    }
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Probably replace with a simple database query.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public boolean isSupernodeOf(@Nonnull Node node) throws DatabaseException {
        return getAllSubcontexts().contains(node);
    }
    
    /* -------------------------------------------------- Contacts -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the contacts.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Context, Contact> CONTACTS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Context, Contact>withRequiredRestrictionsToExecuteMethod((context, contact) -> RestrictionsBuilder.withWriteToNode(true).withNode(context).build()).withRequiredRestrictionsToSeeMethod((context, contact) -> RestrictionsBuilder.buildWithNode(context)).build();
    
    /**
     * Returns the direct contacts of this context.
     */
    // TODO: The generated foreign key reference is still wrong because it does not include the provided entity and gets the column name wrong.
//    @Pure
//    @GenerateSynchronizedProperty
//    @TODO(task = "Support simple synchronized set properties?", date = "2017-08-20", author = Author.KASPAR_ETTER)
//    public abstract @Nonnull WritablePersistentSetProperty<Context, /* TODO: @Matching */ Contact, ReadOnlySet<Contact>, FreezableSet<Contact>> contacts();
    
    /**
     * Returns all the contacts of this context (including the contacts from subcontexts).
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make a real aggregation.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public @Capturable @Nonnull @NonFrozen FreezableSet<Contact> getAllContacts() throws DatabaseException, RecoveryException {
        return FreezableLinkedHashSetBuilder.build();
//        return contacts().get().clone();
    }
    
    /**
     * Returns whether this context contains (directly or indirectly) the given contact.
     */
    @Pure
    @NonCommitting
    public boolean contains(@Nonnull Contact contact) throws DatabaseException, RecoveryException {
        return getAllContacts().contains(contact);
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
     * Returns the potentially cached context of the given entity with the given key that might not yet exist in the database.
     */
    @Pure
    @Recover
    @TODO(task = "The entry in the node table should rather be created in the core subejct index.", date = "2017-08-20", author = Author.KASPAR_ETTER)
    public static @Nonnull Context of(@Nonnull NonHostEntity entity, @Even long key) throws DatabaseException {
        final @Nonnull Context context = ContextSubclass.MODULE.getSubjectIndex().get(entity, key);
        SQL.insertOrReplace(ContextSubclass.SUPER_MODULE.getSubjectTable(), context, context.getUnit());
        return context;
    }
    
    /**
     * Returns the potentially cached root context of the given entity that might not yet exist in the database.
     */
    @Pure
    public static @Nonnull Context of(@Nonnull NonHostEntity entity) throws DatabaseException {
        return of(entity, ROOT);
    }
    
}
