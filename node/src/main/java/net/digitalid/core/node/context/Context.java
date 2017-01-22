package net.digitalid.core.node.context;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.set.WritablePersistentSimpleSetProperty;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.ExtendedNode;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorization;
import net.digitalid.core.property.set.SetPropertyRequiredAuthorizationBuilder;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorization;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.RestrictionsBuilder;

/**
 * This class models the contexts for {@link Contact contacts}.
 */
@Immutable
// TODO: @GenerateSubclass
@GenerateConverter
public abstract class Context extends ExtendedNode {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code flat.context@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType FLAT = SemanticType.map("flat.context@core.digitalid.net");
    
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
    static final @Nonnull ValuePropertyRequiredAuthorization<NonHostEntity<?>, Long, Context, String> NAME_AUTHORIZATION = ValuePropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, Long, Context, String>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /**
     * Returns the name of this context.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Context, @Nonnull @MaxSize(50) String> name();
    
    /* -------------------------------------------------- Preferences -------------------------------------------------- */
    
    // TODO: Declare a set property with the attribute types that are requested if a new contact is added (indirectly) to this context.
    
    /* -------------------------------------------------- Subcontexts -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the subcontexts.
     */
    static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity<?>, Long, Context, Context> SUBCONTEXTS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, Long, Context, Context>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /**
     * Returns the direct subcontexts of this context.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentSimpleSetProperty<Context, /* TODO: @Matching */ Context> subcontexts();
    
    /**
     * Returns all subcontexts of this context (including this context).
     */
    @Pure
    @NonCommitting
    public @Nonnull @Capturable @NonFrozen FreezableSet<Context> getAllSubcontexts() throws DatabaseException {
        // TODO: Either retrieve all subcontexts by traversing the direct subcontexts or querying the database.
        throw new RuntimeException("TODO");
    }
    
    @Pure
    @Override
    @NonCommitting
    public boolean isSupernodeOf(@Nonnull Node node) throws DatabaseException {
        // TODO: Probably replace with a simple database query.
        return getAllSubcontexts().contains(node);
    }
    
    /* -------------------------------------------------- Contacts -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the subcontexts.
     */
    static final @Nonnull SetPropertyRequiredAuthorization<NonHostEntity<?>, Long, Context, Contact> CONTACTS_AUTHORIZATION = SetPropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, Long, Context, Contact>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()).withRequiredRestrictionsToSeeMethod((concept, value) -> RestrictionsBuilder.withNode(concept).build()).build();
    
    /**
     * Returns the direct contacts of this context.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentSimpleSetProperty<Context, /* TODO: @Matching */ Contact> contacts();
    
    /**
     * Returns all the contacts of this context (i.e. including the contacts from subcontexts).
     */
    @Pure
    @NonCommitting
    public @Capturable @Nonnull @NonFrozen FreezableSet<Contact> getAllContacts() throws DatabaseException {
        return contacts().get().clone(); // TODO: Make a real aggregation.
    }
    
    /**
     * Returns whether this context contains (directly or indirectly) the given contact.
     */
    @Pure
    @NonCommitting
    public boolean contains(@Nonnull Contact contact) throws DatabaseException {
        return getAllContacts().contains(contact);
    }
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    // TODO: Think about how to synchronize the creation of contexts.
    
//    /**
//     * Creates a new context at the given role.
//     * 
//     * @param role the role to which the context belongs.
//     */
//    public static @Nonnull Context create(@Nonnull Role role) {
//        final @Nonnull Context context = get(role, new Random().nextLong());
////        Synchronizer.execute(new ContextCreate(context));
//        return context;
//    }
//    
//    /**
//     * Creates this context in the database.
//     */
//    @NonCommitting
//    @OnlyForActions
//    public void createForActions() throws DatabaseException {
//        ContextModule.create(this);
//        notify(CREATED);
//    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached context of the given entity with the given key that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull Context of(@Nonnull NonHostEntity<?> entity, long key) {
        return null; // TODO
    }
    
    /**
     * Returns the potentially cached root context of the given entity that might not yet exist in the database.
     */
    @Pure
    public static @Nonnull Context of(@Nonnull NonHostEntity<?> entity) {
        return of(entity, ROOT);
    }
    
}
