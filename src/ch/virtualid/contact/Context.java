package ch.virtualid.contact;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Contexts;
import ch.xdf.Block;
import ch.xdf.Int64Wrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class represents the context for contacts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.4
 */
public final class Context extends Concept implements Immutable, Blockable, SQLizable {
    
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
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = "REFERENCES context (entity, context) ON DELETE CASCADE ON UPDATE CASCADE";
    
    
    /**
     * Stores the semantic type {@code context@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("context@virtualid.ch").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code flat.context@virtualid.ch}.
     */
    public static final @Nonnull SemanticType FLAT = SemanticType.create("flat.context@virtualid.ch");    
    
    /**
     * Stores the number of the root context.
     */
    public static final long ROOT = 0L;
    
    
    /**
     * Stores the number that denotes the context.
     */
    private final long number;
    
    /**
     * Creates a new context with the given entity and number.
     * 
     * @param entity the entity to which the context belongs.
     * @param number the number that denotes the context.
     */
    private Context(@Nonnull Entity entity, long number) {
        super(entity);
        
        this.number = number;
    }
    
    /**
     * Creates a new context at the given role.
     * 
     * @param role the role to which the context belongs.
     */
    public static @Nonnull Context create(@Nonnull Role role) {
        final @Nonnull Context context = get(role, new SecureRandom().nextLong());
        Synchronizer.execute(new ContextCreate(context));
        return context;
    }
    
    /**
     * Creates the given context in the database.
     * 
     * @param context the context to create in the database.
     */
    @OnlyForActions
    public static void createForActions(@Nonnull Context context) {
        Contexts.create(context);
        context.notify(CREATED);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int64Wrapper(TYPE, number).toBlock();
    }
    
    
    /**
     * Returns the number that denotes this context.
     * 
     * @return the number that denotes this context.
     */
    @Pure
    public long getNumber() {
        return number;
    }
    
    /**
     * Returns whether this context is the root.
     * 
     * @return whether this context is the root.
     */
    @Pure
    public boolean isRoot() {
        return number == ROOT;
    }
    
    
    /**
     * Stores the name of this context.
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
    public @Nonnull String getName() throws SQLException {
        if (name == null) {
            name = Contexts.getName(this);
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
    public void setName(@Nonnull String newName) throws SQLException {
        assert isValid(newName) : "The new name is valid.";
        
        final @Nonnull String oldName = getName();
        if (!newName.equals(oldName)) {
            Synchronizer.execute(new ContextNameReplace(this, oldName, newName));
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
    @OnlyForActions
    public void replaceName(@Nonnull String oldName, @Nonnull String newName) throws SQLException {
        assert isValid(oldName) : "The old name is valid.";
        assert isValid(newName) : "The new name is valid.";
        
        Contexts.replaceName(this, oldName, newName);
        name = newName;
        notify(NAME);
    }
    
    
    /**
     * Stores the permissions of this context.
     */
    private @Nullable ContactPermissions permissions;
    
    /**
     * Returns the permissions of this context.
     * 
     * @return the permissions of this context.
     */
    @Pure
    public @Nonnull ReadonlyContactPermissions getPermissions() throws SQLException {
        if (permissions == null) {
            permissions = Contexts.getPermissions(this);
        }
        return permissions;
    }
    
    /**
     * Adds the given permissions to this context.
     * 
     * @param permissions the permissions to be added to this context.
     */
    public void addPermissions(@Nonnull ReadonlyContactPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) {
            Synchronizer.execute(new ContextPermissionsAdd(this, permissions));
        }
    }
    
    /**
     * Adds the given permissions to this context.
     * 
     * @param newPermissions the permissions to be added to this context.
     * 
     * @require !newPermissions.isEmpty() : "The new permissions are not empty.";
     */
    @OnlyForActions
    public void addPermissionsForActions(@Nonnull ReadonlyContactPermissions newPermissions) throws SQLException {
        assert !newPermissions.isEmpty() : "The new permissions are not empty.";
        
        Contexts.addPermissions(this, newPermissions);
        if (permissions != null) permissions.addAll(newPermissions);
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this context.
     * 
     * @param permissions the permissions to be removed from this context.
     */
    public void removePermissions(@Nonnull ReadonlyContactPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) {
            Synchronizer.execute(new ContextPermissionsRemove(this, permissions));
        }
    }
    
    /**
     * Removes the given permissions from this context.
     * 
     * @param oldPermissions the permissions to be removed from this context.
     * 
     * @require !oldPermissions.isEmpty() : "The old permissions are not empty.";
     */
    @OnlyForActions
    public void removePermissionsForActions(@Nonnull ReadonlyContactPermissions oldPermissions) throws SQLException {
        assert !oldPermissions.isEmpty() : "The old permissions are not empty.";
        
        Contexts.removePermissions(this, oldPermissions);
        if (permissions != null) permissions.removeAll(permissions);
        notify(PERMISSIONS);
    }
    
    
    /**
     * Returns the authentications of this context.
     * 
     * @return the authentications of this context.
     */
    public @Nonnull ReadonlyAuthentications getAuthentications() throws SQLException {
        
    }
    
    /**
     * Adds the given authentications to this context.
     * 
     * @param authentications the authentications to be added to this context.
     */
    public void addAuthentications(@Nonnull ReadonlyAuthentications authentications) throws SQLException {
        
    }
    
    /**
     * Removes the given authentications from this context.
     * 
     * @param authentications the authentications to be removed from this context.
     */
    public void removeAuthentications(@Nonnull ReadonlyAuthentications authentications) throws SQLException {
        
    }
    
    
    /**
     * Returns a list of the subcontexts in the specified sequence.
     * 
     * @return a list of the subcontexts in the specified sequence.
     */
    public @Nonnull List<Context> getSubcontexts() throws SQLException;
    
    /**
     * Adds the given subcontexts to this context at the given position.
     * 
     * @param subcontexts the subcontexts to be added to this context.
     * @param position the position the add the given subcontexts.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    public void addSubcontexts(@Nonnull List<Context> subcontexts, byte position) throws SQLException;
    
    /**
     * Removes the given subcontexts from this context.
     * 
     * @param subcontexts the subcontexts to be removed from this context.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    public void removeSubcontexts(@Nonnull List<Context> subcontexts) throws SQLException;
    
    /**
     * Returns whether this context is a supercontext of the given context.
     * Please note that this relation is reflexive (i.e. the method returns {@code true} for the same context).
     * 
     * @param context the context to compare with.
     * @return whether this context is a supercontext of the given context.
     * @require context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
     */
    public boolean isSupercontextOf(@Nonnull Context context) throws SQLException;
    
    /**
     * Returns a set with all subcontexts of this context (including this context).
     * 
     * @return a set with all subcontexts of this context (including this context).
     */
    public @Nonnull Set<Context> getAllSubcontexts() throws SQLException;
    
    
    /**
     * Returns a set with the subcontexts of this context.
     * 
     * @return a set with the subcontexts of this context.
     */
    public @Nonnull Set<Context> getSupercontexts() throws SQLException;
    
    /**
     * Returns whether this context is a subcontext of the given context.
     * Please note that this relation is reflexive (i.e. the method returns {@code true} for the same context).
     * 
     * @param context the context to compare with.
     * @return whether this context is a subcontext of the given context.
     * @require context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
     */
    public final boolean isSubcontextOf(@Nonnull Context context) throws SQLException {
        assert context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
        
        return context.isSupercontextOf(this);
    }
    
    
    /**
     * Returns a set with the contacts of this context.
     * 
     * @return a set with the contacts of this context.
     */
    public @Nonnull Set<Contact> getContacts() throws SQLException;
    
    /**
     * Returns whether this context contains the given contact.
     * 
     * @param contact the contact to check this context for.
     * 
     * @return whether this context contains the given contact.
     */
    public boolean contains(@Nonnull Contact contact) throws SQLException;
    
    /**
     * Adds the given contacts to this context.
     * 
     * @param contacts the contacts to be added to this context.
     */
    public void addContacts(@Nonnull Set<Contact> contacts) throws SQLException;
    
    /**
     * Removes the given contacts from this context.
     * 
     * @param contacts the contacts to be removed from this context.
     */
    public void removeContacts(@Nonnull Set<Contact> contacts) throws SQLException;
    
    /**
     * Returns a set with all the contacts of this context (i.e. the contacts from subcontexts are included as well).
     * 
     * @return a set with all the contacts of this context (i.e. the contacts from subcontexts are included as well).
     */
    public @Nonnull Set<Contact> getAllContacts() throws SQLException;
    
    
    /**
     * Caches contexts given their entity and number.
     */
    private static final @Nonnull Map<Pair<Entity, Long>, Context> index = new HashMap<Pair<Entity, Long>, Context>();
    
    /**
     * Returns a (locally cached) context that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the context belongs.
     * @param number the number that denotes the context.
     * 
     * @return a new or existing context with the given entity and number.
     */
    @Pure
    public static @Nonnull Context get(@Nonnull Entity entity, long number) {
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Entity, Long> pair = new Pair<Entity, Long>(entity, number);
                @Nullable Context context = index.get(pair);
                if (context == null) {
                    context = new Context(entity, number);
                    index.put(pair, context);
                }
                return context;
            }
        } else {
            return new Context(entity, number);
        }
    }
    
    /**
     * Returns the context with the number given by the string in hexadecimal notation.
     * 
     * @param entity the entity to which the context belongs.
     * @param string a string in hexadecimal notation encoding the context number.
     */
    @Pure
    public static @Nonnull Context get(@Nonnull Entity entity, @Nonnull String string) throws InvalidEncodingException {
        return get(entity, parse(string));
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
    public static @Nonnull Context get(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        return get(entity, new Int64Wrapper(block).getValue());
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
    public static @Nonnull Context get(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return get(entity, resultSet.getLong(columnIndex));
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    
    @Pure
    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Context)) return false;
        final @Nonnull Context other = (Context) object;
        return this.number == other.number;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return (int) (this.number ^ (this.number >>> 32));
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(number);
    }
    
    /**
     * Returns a hexadecimal representation of this context.
     * 
     * @return a hexadecimal representation of this context.
     */
    @Pure
    public @Nonnull String toHexString() {
        return String.format("0x%016X", number);
    }
    
    /**
     * Returns the given string in hexadecimal notation as long.
     * 
     * @param string the string to parse in hexadecimal notation.
     * 
     * @return the given string in hexadecimal notation as long.
     */
    @Pure
    private static long parse(@Nonnull String string) throws InvalidEncodingException {
        if (string.length() != 18 || !string.startsWith("0x")) throw new InvalidEncodingException("'" + string + "' does not consist of 18 characters and start with '0x'.");
        return (Long.parseLong(string.substring(2, 10), 16) << 32) | Long.parseLong(string.substring(10, 18), 16);
    }
    
}
