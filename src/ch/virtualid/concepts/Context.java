package ch.virtualid.concepts;

import ch.virtualid.concept.Concept;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.Int64Wrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class represents the context for contacts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.4
 */
public abstract class Context extends Concept implements Immutable {
    
    /**
     * Stores the semantic type {@code flat.context@virtualid.ch}.
     */
    public static final @Nonnull SemanticType FLAT = mapSemanticType(NonHostIdentifier.FLAT_CONTEXT);    
    
    /**
     * Stores the number of the root context.
     */
    public static final long ROOT = 0L;
    
    /**
     * Stores the number that denotes the context.
     */
    private final long number;
    
    /**
     * Creates a new context from the given connection, identity and number.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity to which this context belongs.
     * @param number the number that denotes the context.
     */
    public Context(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, long number) {
        super(connection, identity);
        this.number = number;
    }
    
    /**
     * Creates a new context from the given connection, identity and string in hexadecimal notation.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity to which this context belongs.
     * @param string a string in hexadecimal notation encoding the context number.
     */
    public Context(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull String string) throws InvalidEncodingException {
        this(connection, identity, parse(string));
    }
    
    /**
     * Creates a new context from the given connection, identity and block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity to which this context belongs.
     * @param block a block of the syntactic type {@code int64@xdf.ch}. // TODO: Rewrite with type.
     */
    public Context(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException {
        this(connection, identity, new Int64Wrapper(block).getValue());
    }
    
    /**
     * Creates a new root context from the given connection and identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity to which this context belongs.
     */
    public Context(@Nonnull Connection connection, @Nonnull NonHostIdentity identity) {
        this(connection, identity, ROOT); // new SecureRandom().nextLong()
    }
    
    /**
     * Returns the number that denotes this context.
     * 
     * @return the number that denotes this context.
     */
    public final long getNumber() {
        return number;
    }
    
    /**
     * Returns whether this context is the root.
     * 
     * @return whether this context is the root.
     */
    public final boolean isRoot() {
        return number == ROOT;
    }
    
    
    /**
     * Returns the name of this context.
     * 
     * @return the name of this context.
     * @ensure getName().length() <= 50 : "The context name may have at most 50 characters.";
     */
    public abstract @Nonnull String getName() throws SQLException;
    
    /**
     * Sets the name of this context.
     * 
     * @param name the name of this context.
     * @require name.length() <= 50 : "The context name may have at most 50 characters.";
     */
    public abstract void setName(@Nonnull String name) throws SQLException;
    
    
    /**
     * Returns the permissions of this context.
     * 
     * @return the permissions of this context.
     */
    public abstract @Nonnull Set<SemanticType> getPermissions() throws SQLException;
    
    /**
     * Adds the given permissions to this context.
     * 
     * @param permissions the permissions to be added to this context.
     */
    public abstract void addPermissions(@Nonnull Set<SemanticType> permissions) throws SQLException;
    
    /**
     * Removes the given permissions from this context.
     * 
     * @param permissions the permissions to be removed from this context.
     */
    public abstract void removePermissions(@Nonnull Set<SemanticType> permissions) throws SQLException;
    
    
    /**
     * Returns the authentications of this context.
     * 
     * @return the authentications of this context.
     */
    public abstract @Nonnull Set<SemanticType> getAuthentications() throws SQLException;
    
    /**
     * Adds the given authentications to this context.
     * 
     * @param authentications the authentications to be added to this context.
     */
    public abstract void addAuthentications(@Nonnull Set<SemanticType> authentications) throws SQLException;
    
    /**
     * Removes the given authentications from this context.
     * 
     * @param authentications the authentications to be removed from this context.
     */
    public abstract void removeAuthentications(@Nonnull Set<SemanticType> authentications) throws SQLException;
    
    
    /**
     * Returns a list of the subcontexts in the specified sequence.
     * 
     * @return a list of the subcontexts in the specified sequence.
     */
    public abstract @Nonnull List<Context> getSubcontexts() throws SQLException;
    
    /**
     * Adds the given subcontexts to this context at the given position.
     * 
     * @param subcontexts the subcontexts to be added to this context.
     * @param position the position the add the given subcontexts.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    public abstract void addSubcontexts(@Nonnull List<Context> subcontexts, byte position) throws SQLException;
    
    /**
     * Removes the given subcontexts from this context.
     * 
     * @param subcontexts the subcontexts to be removed from this context.
     * @require (Context subcontext : subcontexts).getIdentity().equals(getIdentity()) : "The identity of all contexts have to be the same.";
     */
    public abstract void removeSubcontexts(@Nonnull List<Context> subcontexts) throws SQLException;
    
    /**
     * Returns whether this context is a supercontext of the given context.
     * Please note that this relation is reflexive (i.e. the method returns {@code true} for the same context).
     * 
     * @param context the context to compare with.
     * @return whether this context is a supercontext of the given context.
     * @require context.getIdentity().equals(getIdentity()) : "The identity of the given context is the same.";
     */
    public abstract boolean isSupercontextOf(@Nonnull Context context) throws SQLException;
    
    /**
     * Returns a set with all subcontexts of this context (including this context).
     * 
     * @return a set with all subcontexts of this context (including this context).
     */
    public abstract @Nonnull Set<Context> getAllSubcontexts() throws SQLException;
    
    
    /**
     * Returns a set with the subcontexts of this context.
     * 
     * @return a set with the subcontexts of this context.
     */
    public abstract @Nonnull Set<Context> getSupercontexts() throws SQLException;
    
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
    public abstract @Nonnull Set<Person> getContacts() throws SQLException;
    
    /**
     * Adds the given contacts to this context.
     * 
     * @param contacts the contacts to be added to this context.
     */
    public abstract void addContacts(@Nonnull Set<Person> contacts) throws SQLException;
    
    /**
     * Removes the given contacts from this context.
     * 
     * @param contacts the contacts to be removed from this context.
     */
    public abstract void removeContacts(@Nonnull Set<Person> contacts) throws SQLException;
    
    /**
     * Returns a set with all the contacts of this context (i.e. the contacts from subcontexts are included as well).
     * 
     * @return a set with all the contacts of this context (i.e. the contacts from subcontexts are included as well).
     */
    public abstract @Nonnull Set<Person> getAllContacts() throws SQLException;
    
    
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Context)) return false;
        @Nonnull Context other = (Context) object;
        return number == other.number;
    }
    
    @Override
    public int hashCode() {
        return (int) (this.number ^ (this.number >>> 32));
    }
    
    @Override
    public @Nonnull String toString() {
        return String.valueOf(number);
    }
    
    /**
     * Returns a hexadecimal representation of this context.
     * 
     * @return a hexadecimal representation of this context.
     */
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
    private static long parse(@Nonnull String string) throws InvalidEncodingException {
        if (string.length() != 18 || !string.startsWith("0x")) throw new InvalidEncodingException("'" + string + "' does not consist of 18 characters and start with '0x'.");
        return (Long.parseLong(string.substring(2, 10), 16) << 32) | Long.parseLong(string.substring(10, 18), 16);
    }
    
    @Override
    public @Nonnull Block toBlock() {
        return new Int64Wrapper(number).toBlock();
    }
    
}
