package net.digitalid.core.context;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.reference.Capturable;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.synchronizer.Synchronizer;

import net.digitalid.service.core.annotations.OnlyForActions;

import net.digitalid.core.conversion.Block;

import net.digitalid.service.core.block.Blockable;

import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.concept.Observer;
import net.digitalid.service.core.database.SQLizable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.client.annotations.Clients;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.contact.FreezableContactPermissions;
import net.digitalid.core.contact.ReadOnlyAuthentications;
import net.digitalid.core.contact.ReadOnlyContactPermissions;

/**
 * This class models the contexts for {@link Contact contacts}.
 */
@Immutable
public final class Context extends NonHostConcept implements Blockable, SQLizable {
    
    
    /* -------------------------------------------------- Aspects -------------------------------------------------- */
    
    /**
     * Stores the aspect of the name being changed at the observed context.
     */
    public static final @Nonnull Aspect NAME = new Aspect(Context.class, "name changed");
    
    /**
     * Stores the aspect of the preferences being changed at the observed context.
     */
    public static final @Nonnull Aspect PREFERENCES = new Aspect(Context.class, "preferences changed");
    
    /**
     * Stores the aspect of the permissions being changed at the observed context.
     */
    public static final @Nonnull Aspect PERMISSIONS = new Aspect(Context.class, "permissions changed");
    
    /**
     * Stores the aspect of the authentications being changed at the observed context.
     */
    public static final @Nonnull Aspect AUTHENTICATIONS = new Aspect(Context.class, "authentications changed");
    
    /**
     * Stores the aspect of the number of subcontexts being changed at the observed context.
     */
    public static final @Nonnull Aspect SUBCONTEXTS_NUMBER = new Aspect(Context.class, "number of subcontexts changed");
    
    /**
     * Stores the aspect of the order of subcontexts being changed at the observed context.
     */
    public static final @Nonnull Aspect SUBCONTEXTS_ORDER = new Aspect(Context.class, "order of subcontexts changed");
    
    /**
     * Stores the aspect of the contacts being changed at the observed context.
     */
    public static final @Nonnull Aspect CONTACTS = new Aspect(Context.class, "contacts changed");
    
    /**
     * Stores the aspect of the observed context being created in the database.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(Context.class, "created");
    
    /**
     * Stores the aspect of the observed context being reset after having reloaded the contexts module.
     */
    public static final @Nonnull Aspect RESET = new Aspect(Context.class, "reset");
    
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code context@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("context@core.digitalid.net").load(Integer64Wrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code flat.context@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType FLAT = SemanticType.map("flat.context@core.digitalid.net");    
    
    /**
     * Stores the semantic type {@code name.context@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NAME_TYPE = SemanticType.map("name.context@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    
    /* -------------------------------------------------- Number -------------------------------------------------- */
    
    /**
     * Stores the number that denotes the context.
     */
    private final long number;
    
    /**
     * Returns the number that denotes this context.
     * 
     * @return the number that denotes this context.
     */
    @Pure
    public long getNumber() {
        return number;
    }
    
    /* -------------------------------------------------- Root -------------------------------------------------- */
    
    /**
     * Stores the number of the root context.
     */
    public static final long ROOT = 0L;
    
    /**
     * Returns whether this context is the root.
     * 
     * @return whether this context is the root.
     */
    @Pure
    public boolean isRoot() {
        return number == ROOT;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Stores the name of this context.
     * 
     * @invariant isValid(name) : "The name is valid.";
     */
    private @Nullable String name;
    
    /**
     * Returns whether the given name is valid.
     * A valid name has at most 50 characters.
     * 
     * @param name the name to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public static boolean isValid(@Nonnull String name) {
        return name.length() <= 50;
    }
    
    /**
     * Returns the name of this context.
     * 
     * @return the name of this context.
     * 
     * @ensure isValid(return) : "The returned name is valid.";
     */
    @Pure
    @NonCommitting
    public @Nonnull String getName() throws DatabaseException {
        if (name == null) {
            throw new SQLException();
//            name = Contexts.getName(this);
        }
        return name;
    }
    
    /**
     * Sets the name of this context.
     * 
     * @param newName the new name of this context.
     * 
     * @require isValid(name) : "The given name is valid.";
     */
    @Committing
    public void setName(@Nonnull String newName) throws DatabaseException {
        assert isValid(newName) : "The new name is valid.";
        
        final @Nonnull String oldName = getName();
        if (!newName.equals(oldName)) {
//            Synchronizer.execute(new ContextNameReplace(this, oldName, newName));
        }
    }
    
    /**
     * Replaces the name of this context.
     * 
     * @param oldName the old name of this context.
     * @param newName the new name of this context.
     * 
     * @require isValid(oldName) : "The old name is valid.";
     * @require isValid(newName) : "The new name is valid.";
     */
    @NonCommitting
    @OnlyForActions
    public void replaceName(@Nonnull String oldName, @Nonnull String newName) throws DatabaseException {
        assert isValid(oldName) : "The old name is valid.";
        assert isValid(newName) : "The new name is valid.";
        
//        Contexts.replaceName(this, oldName, newName);
        name = newName;
        notify(NAME);
    }
    
    /* -------------------------------------------------- Preferences -------------------------------------------------- */
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the permissions of this context.
     */
    private @Nullable FreezableContactPermissions permissions;
    
    /**
     * Returns the permissions of this context.
     * 
     * @return the permissions of this context.
     */
    @Pure
    @NonCommitting
    public @Nonnull ReadOnlyContactPermissions getPermissions() throws DatabaseException {
        if (permissions == null) {
            throw new SQLException();
//            permissions = Contexts.getPermissions(this);
        }
        return permissions;
    }
    
    /**
     * Adds the given permissions to this context.
     * 
     * @param permissions the permissions to be added to this context.
     */
    @Committing
    public void addPermissions(@Nonnull ReadOnlyContactPermissions permissions) throws DatabaseException {
        if (!permissions.isEmpty()) {
//            Synchronizer.execute(new ContextPermissionsAdd(this, permissions));
        }
    }
    
    /**
     * Adds the given permissions to this context.
     * 
     * @param newPermissions the permissions to be added to this context.
     * 
     * @require !newPermissions.isEmpty() : "The new permissions are not empty.";
     */
    @NonCommitting
    @OnlyForActions
    public void addPermissionsForActions(@Nonnull ReadOnlyContactPermissions newPermissions) throws DatabaseException {
        assert !newPermissions.isEmpty() : "The new permissions are not empty.";
        
//        Contexts.addPermissions(this, newPermissions);
        if (permissions != null) { permissions.addAll(newPermissions); }
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this context.
     * 
     * @param permissions the permissions to be removed from this context.
     */
    @Committing
    public void removePermissions(@Nonnull ReadOnlyContactPermissions permissions) throws DatabaseException {
        if (!permissions.isEmpty()) {
//            Synchronizer.execute(new ContextPermissionsRemove(this, permissions));
        }
    }
    
    /**
     * Removes the given permissions from this context.
     * 
     * @param oldPermissions the permissions to be removed from this context.
     * 
     * @require !oldPermissions.isEmpty() : "The old permissions are not empty.";
     */
    @NonCommitting
    @OnlyForActions
    public void removePermissionsForActions(@Nonnull ReadOnlyContactPermissions oldPermissions) throws DatabaseException {
        assert !oldPermissions.isEmpty() : "The old permissions are not empty.";
        
//        Contexts.removePermissions(this, oldPermissions);
        if (permissions != null) { permissions.removeAll(permissions); }
        notify(PERMISSIONS);
    }
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this context.
     * 
     * @return the authentications of this context.
     */
    @NonCommitting
    public @Nonnull ReadOnlyAuthentications getAuthentications() throws DatabaseException {
        throw new SQLException();
    }
    
    /**
     * Adds the given authentications to this context.
     * 
     * @param authentications the authentications to be added to this context.
     */
    @Committing
    public void addAuthentications(@Nonnull ReadOnlyAuthentications authentications) throws DatabaseException {
        
    }
    
    /**
     * Removes the given authentications from this context.
     * 
     * @param authentications the authentications to be removed from this context.
     */
    @Committing
    public void removeAuthentications(@Nonnull ReadOnlyAuthentications authentications) throws DatabaseException {
        
    }
    
    /* -------------------------------------------------- Subcontexts -------------------------------------------------- */
    
    /**
     * Returns a list of the subcontexts in the specified sequence.
     * 
     * @return a list of the subcontexts in the specified sequence.
     */
    @NonCommitting
    public @Nonnull List<Context> getSubcontexts() throws DatabaseException {
        throw new SQLException();
    }
    
    /**
     * Adds the given subcontexts to this context at the given position.
     * 
     * @param subcontexts the subcontexts to be added to this context.
     * @param position the position the add the given subcontexts.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    @Committing
    public void addSubcontexts(@Nonnull List<Context> subcontexts, byte position) throws DatabaseException {
        throw new SQLException();
    }
    
    /**
     * Removes the given subcontexts from this context.
     * 
     * @param subcontexts the subcontexts to be removed from this context.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    @Committing
    public void removeSubcontexts(@Nonnull List<Context> subcontexts) throws DatabaseException {
        throw new SQLException();
    }
    
    /**
     * Returns whether this context is a supercontext of the given context.
     * Please note that this relation is reflexive (i.e. the method returns {@code true} for the same context).
     * 
     * @param context the context to compare with.
     * 
     * @return whether this context is a supercontext of the given context.
     * 
     * @require context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
     */
    @NonCommitting
    public boolean isSupercontextOf(@Nonnull Context context) throws DatabaseException {
        if (equals(context)) { return true; }
        else { throw new SQLException(); }
    }
    
    /**
     * Returns a set with all subcontexts of this context (including this context).
     * 
     * @return a set with all subcontexts of this context (including this context).
     */
    @NonCommitting
    public @Nonnull Set<Context> getAllSubcontexts() throws DatabaseException {
        throw new SQLException();
    }
    
    /* -------------------------------------------------- Supercontexts -------------------------------------------------- */
    
    /**
     * Returns a set with the subcontexts of this context.
     * 
     * @return a set with the subcontexts of this context.
     */
    @NonCommitting
    public @Nonnull Set<Context> getSupercontexts() throws DatabaseException {
        throw new SQLException();
    }
    
    /**
     * Returns whether this context is a subcontext of the given context.
     * Please note that this relation is reflexive (i.e. the method returns {@code true} for the same context).
     * 
     * @param context the context to compare with.
     * @return whether this context is a subcontext of the given context.
     * @require context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
     */
    @NonCommitting
    public boolean isSubcontextOf(@Nonnull Context context) throws DatabaseException {
//        assert context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
        
        return context.isSupercontextOf(this);
    }
    
    /* -------------------------------------------------- Contacts -------------------------------------------------- */
    
    /**
     * Stores the contacts of this context or null if not yet loaded.
     */
    private @Nullable @NonFrozen FreezableContacts contacts;
    
    /**
     * Returns the contacts of this context.
     * 
     * @return the contacts of this context.
     */
    @Locked
    @NonCommitting
    public @Nonnull @NonFrozen ReadOnlyContacts getContacts() throws DatabaseException {
        if (contacts == null) { contacts = ContextModule.getContacts(this); }
        return contacts;
    }
    
    /**
     * Adds the given contacts to this context.
     * 
     * @param contacts the contacts to be added to this context.
     */
    @Committing
    @Clients
    public void addContacts(@Nonnull @Frozen ReadOnlyContacts contacts) throws DatabaseException {
        if (!contacts.isEmpty()) { Synchronizer.execute(new ContactsAdd(this, contacts)); }
    }
    
    /**
     * Adds the given contacts to this context.
     * 
     * @param newContacts the contacts to be added to this context.
     */
    @NonCommitting
    @OnlyForActions
    void addContactsForActions(@Nonnull @Frozen ReadOnlyContacts newContacts) throws DatabaseException {
        ContextModule.addContacts(this, newContacts);
        if (contacts != null) { contacts.addAll(newContacts); }
        notify(CONTACTS);
    }
    
    /**
     * Removes the given contacts from this context.
     * 
     * @param contacts the contacts to be removed from this context.
     */
    @Committing
    @Clients
    public void removeContacts(@Nonnull @Frozen ReadOnlyContacts contacts) throws DatabaseException {
        if (!contacts.isEmpty()) { Synchronizer.execute(new ContactsRemove(this, contacts)); }
    }
    
    /**
     * Removes the given contacts from this context.
     * 
     * @param oldContacts the contacts to be removed from this context.
     */
    @NonCommitting
    @OnlyForActions
    void removeContactsForActions(@Nonnull @Frozen ReadOnlyContacts oldContacts) throws DatabaseException {
        ContextModule.removeContacts(this, oldContacts);
        if (contacts != null) { contacts.removeAll(oldContacts); }
        notify(CONTACTS);
    }
    
    /**
     * Returns a set with all the contacts of this context (i.e. including the contacts from subcontexts).
     * 
     * @return a set with all the contacts of this context (i.e. including the contacts from subcontexts).
     */
    @NonCommitting
    public @Nonnull @Capturable @NonFrozen FreezableContacts getAllContacts() throws DatabaseException {
        return getContacts().clone(); // TODO: Make a real aggregation.
    }
    
    /**
     * Returns whether this context contains the given contact.
     * 
     * @param contact the contact to check this context for.
     * 
     * @return whether this context contains the given contact.
     */
    @NonCommitting
    public boolean contains(@Nonnull Contact contact) throws DatabaseException {
        return getAllContacts().contains(contact);
    }
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    /**
     * Creates a new context at the given role.
     * 
     * @param role the role to which the context belongs.
     */
    public static @Nonnull Context create(@Nonnull Role role) {
        final @Nonnull Context context = get(role, new Random().nextLong());
//        Synchronizer.execute(new ContextCreate(context));
        return context;
    }
    
    /**
     * Creates this context in the database.
     */
    @NonCommitting
    @OnlyForActions
    public void createForActions() throws DatabaseException {
        ContextModule.create(this);
        notify(CREATED);
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    /**
     * Caches contexts given their entity and number.
     */
    private static final @Nonnull ConcurrentMap<NonHostEntity, ConcurrentMap<Long, Context>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /**
     * Resets this context.
     */
    public void reset() {
        this.name = null;
        this.permissions = null;
        this.contacts = null;
        // TODO: Add the other fields once declared.
        notify(RESET);
    }
    
    /**
     * Resets the contexts of the given entity after having reloaded the context module.
     * 
     * @param entity the entity whose contexts are to be reset.
     */
    public static void reset(@Nonnull NonHostEntity entity) {
        final @Nullable ConcurrentMap<Long, Context> map = index.get(entity);
        if (map != null) {
            final @Nonnull Collection<Context> contexts = map.values();
            for (final @Nonnull Context context : contexts) { context.reset(); }
        }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new context with the given entity and number.
     * 
     * @param entity the entity to which the context belongs.
     * @param number the number that denotes the context.
     */
    private Context(@Nonnull NonHostEntity entity, long number) {
        super(entity);
        
        this.number = number;
    }
    
    /**
     * Returns a (locally cached) context that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the context belongs.
     * @param number the number that denotes the context.
     * 
     * @return a new or existing context with the given entity and number.
     */
    @Pure
    public static @Nonnull Context get(@Nonnull NonHostEntity entity, long number) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, Context> map = index.get(entity);
            if (map == null) { map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<Long, Context>()); }
            @Nullable Context context = map.get(number);
            if (context == null) { context = map.putIfAbsentElseReturnPresent(number, new Context(entity, number)); }
            return context;
        } else {
            return new Context(entity, number);
        }
    }
    
    /**
     * Returns the root context that might not exist in the database.
     * 
     * @param entity the entity whose root context is to be returned.
     * 
     * @return the root context that might not exist in the database.
     */
    @Pure
    public static @Nonnull Context getRoot(@Nonnull NonHostEntity entity) {
        return get(entity, ROOT);
    }
    
    /**
     * Returns the context with the number given by the string.
     * 
     * @param entity the entity to which the context belongs.
     * @param string a string encoding the context number.
     */
    @Pure
    public static @Nonnull Context get(@Nonnull NonHostEntity entity, @Nonnull String string) throws InvalidEncodingException, InternalException {
        try { return get(entity, Long.parseLong(string)); } catch (@Nonnull NumberFormatException exception) { throw InvalidParameterValueException.get("context key", string); }
    }
    
    /* -------------------------------------------------- Blockable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return Integer64Wrapper.encode(TYPE, number);
    }
    
    /**
     * Returns the context with the number given by the block.
     * 
     * @param entity the entity to which the context belongs.
     * @param block a block containing the number of the context.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull Context get(@Nonnull NonHostEntity entity, @Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        return get(entity, Integer64Wrapper.decode(block));
    }
    
    /* -------------------------------------------------- SQLizable -------------------------------------------------- */
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Returns the foreign key constraint used to reference instances of this class.
     * 
     * @param site the site at which the foreign key constraint is declared.
     * 
     * @return the foreign key constraint used to reference instances of this class.
     */
    @NonCommitting
    public static @Nonnull String getReference(@Nonnull Site site) throws DatabaseException {
        ContextModule.createReferenceTable(site);
        return "REFERENCES " + site + "context_name (entity, context) ON DELETE CASCADE";
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the context belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nullable Context get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        final long number = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) { return null; }
        else { return get(entity, number); }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the context belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Context getNotNull(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        return get(entity, resultSet.getLong(columnIndex));
    }
    
    @Override
    @NonCommitting
    public void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given context.
     * 
     * @param context the context to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Context context, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        if (context == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { context.set(preparedStatement, parameterIndex); }
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean equals(Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Context)) { return false; }
        final @Nonnull Context other = (Context) object;
        return this.getEntity().equals(other.getEntity()) && this.number == other.number;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 41 * getEntity().hashCode() + (int) (number ^ (number >>> 32));
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(number);
    }
    
}
