package net.digitalid.core.resolution;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezableTriplet;
import net.digitalid.utility.collections.tuples.ReadOnlyTriplet;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.annotations.transaction.Locked;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.sql.statement.table.create.SQLReferenceOption;
import net.digitalid.database.core.table.GeneralReference;
import net.digitalid.database.core.table.GeneralTable;

import net.digitalid.core.cache.Cache;
import net.digitalid.core.cache.exceptions.IdentityNotFoundException;
import net.digitalid.core.client.AccountInitialize;
import net.digitalid.core.client.AccountOpen;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.host.Host;
import net.digitalid.core.identifier.ExternalIdentifier;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.IdentifierImplementation;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identifier.NonHostIdentifier;
import net.digitalid.core.identity.ArtificialPerson;
import net.digitalid.core.identity.EmailPerson;
import net.digitalid.core.identity.ExternalPerson;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.MobilePerson;
import net.digitalid.core.identity.NaturalPerson;
import net.digitalid.core.identity.NonHostIdentity;
import net.digitalid.core.identity.Person;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.identity.Type;
import net.digitalid.core.packet.exceptions.InvalidDeclarationException;
import net.digitalid.core.identification.annotations.NonMapped;
import net.digitalid.core.server.Server;

/**
 * The mapper maps between {@link Identifier identifiers} and {@link Identity identities}.
 * <p>
 * <em>Important:</em> Every reference to general_identity.identity needs to be registered
 * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
 * in order that the values can be updated when two {@link Person persons} have been
 * merged! (If the column can only contain {@link Type types}, this is not necessary.)
 */
@Stateless
public final class Mapper {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * The key class with a private constructor. (Methods of other classes can require a non-nullable parameter of this type to give exclusive access to this class.)
     */
    public static final class Key { private Key() {} }
    
    /**
     * Stores the key of this class. (This object is the only instance of the key class and the reference to the object should never be captured by any other class.)
     */
    private static final @Nonnull Key KEY = new Key();
    
    /* -------------------------------------------------- Reference -------------------------------------------------- */
    
    public static final @Nonnull GeneralTable IDENTITY_TABLE = GeneralTable.get("general_identity");
    
    public static final @Nonnull ColumnDeclaration<Identity, Object> IDENTITY_COLUMN;
    
    /**
     * Stores the foreign key constraint used to reference identities.
     * <p>
     * <em>Important:</em> Every reference to general_identity.identity needs to be registered
     * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
     * in order that the values can be updated when two {@link Person persons} have been
     * merged! (If the column can only contain {@link Type types}, this is not necessary.)
     * Additionally, it might be a good idea to establish an index on the referencing column.
     */
    public static final @Nonnull GeneralReference REFERENCE = GeneralReference.get(IDENTITY_TABLE, IDENTITY_COLUMN, SQLReferenceOption.RESTRICT, SQLReferenceOption.RESTRICT);
    
    /**
     * Stores the registered triplets of tables, columns and unique constraint that reference a person.
     */
    private static final @Nonnull Set<ReadOnlyTriplet<String, String, String[]>> references = new LinkedHashSet<>();
    
    /**
     * Adds the given table, columns and unique constraint to the list of registered references.
     * 
     * @param table the name of the table that references a person.
     * @param column the name of the column that references a person.
     * @param uniques the names of all the columns in the same unique constraint or nothing.
     */
    public static void addReference(@Nonnull String table, @Nonnull String column, @Nonnull String... uniques) {
        references.add(FreezableTriplet.get(table, column, uniques).freeze());
    }
    
    /**
     * Removes the given table, columns and unique constraint from the list of registered references.
     * 
     * @param table the name of the table that references a person.
     * @param column the name of the column that references a person.
     * @param uniques the names of all the columns in the same unique constraint or nothing.
     */
    public static void removeReference(@Nonnull String table, @Nonnull String column, @Nonnull String... uniques) {
        references.remove(FreezableTriplet.get(table, column, uniques).freeze());
    }
    
    /**
     * Updates the references from the old to the new number using the given statement.
     * 
     * @param statement the statement on which the updates are executed.
     * @param oldNumber the old number of the person which is updated.
     * @param newNumber the new number of the person which is updated.
     */
    @NonCommitting
    private static void updateReferences(@Nonnull Statement statement, long oldNumber, long newNumber) throws DatabaseException {
        final @Nonnull String IGNORE = Database.getConfiguration().IGNORE();
        for (final @Nonnull ReadOnlyTriplet<String, String, String[]> reference : references) {
            final @Nonnull String table = reference.getElement0();
            final @Nonnull String column = reference.getElement1();
            if (IGNORE.isEmpty()) {
                final @Nonnull String[] uniques = reference.getElement2();
                if (uniques.length > 0) {
                    final @Nonnull StringBuilder SQL = new StringBuilder("DELETE FROM ").append(table).append(" AS a WHERE ").append(column).append(" = ").append(oldNumber);
                    SQL.append(" AND WHERE EXISTS (SELECT 1 FROM ").append(table).append(" AS b WHERE ");
                    boolean first = true;
                    for (final @Nonnull String unique : uniques) {
                        if (first) { first = false; }
                        else { SQL.append(" AND "); }
                        SQL.append("b.").append(unique).append(" = ");
                        if (unique.equals(column)) { SQL.append(newNumber); }
                        else { SQL.append("a.").append(unique); }
                    }
                    SQL.append(")");
                    statement.executeUpdate(SQL.toString());
                }
                statement.executeUpdate("UPDATE " + table + " SET " + column + " = " + newNumber + " WHERE " + column + " = " + oldNumber);
            } else {
                statement.executeUpdate("UPDATE" + IGNORE + " " + table + " SET " + column + " = " + newNumber + " WHERE " + column + " = " + oldNumber);
                statement.executeUpdate("DELETE FROM " + table + " WHERE " + column + " = " + oldNumber);
            }
        }
    }
    
    
    static {
        Require.that(Threading.isMainThread()).orThrow("This static block is called in the main thread.");
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            // Make sure that no type initializations are triggered during the creation of the database tables! (This is why the format of the category column is not taken from the category class.)
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address " + IdentifierImplementation.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identifier (identifier " + IdentifierImplementation.FORMAT + " NOT NULL, identity " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (identifier), FOREIGN KEY (identity) " + Mapper.REFERENCE + ")");
            addReference("general_identifier", "identity");
        } catch (@Nonnull SQLException exception) {
            throw InitializationError.get("The database tables of the mapper could not be created.", exception);
        }
    }
    
    
    /**
     * Creates a new identity of the given category with the given number and address.
     * 
     * @param category the category of the identity to be created.
     * @param number the number of the identity to be created.
     * @param address the address of the identity to be created.
     * 
     * @return the newly created identity of the right subtype.
     * 
     * @ensure return.getCategory().equals(category) : "The category of the returned identity equals the given category.";
     * @ensure !(return instanceof Type) || !((Type) return).isLoaded() : "If a type is returned, its declaration has not yet been loaded.";
     */
    @Pure
    @NonCommitting
    private static @Nonnull Identity createIdentity(@Nonnull Category category, long number, @Nonnull Identifier address) throws DatabaseException {
        try {
            switch (category) {
                case HOST: return new HostIdentity(number, address.castTo(HostIdentifier.class));
                case SYNTACTIC_TYPE: return new SyntacticType(number, address.castTo(InternalNonHostIdentifier.class));
                case SEMANTIC_TYPE: return new SemanticType(number, address.castTo(InternalNonHostIdentifier.class));
                case NATURAL_PERSON: return new NaturalPerson(number, address.castTo(InternalNonHostIdentifier.class));
                case ARTIFICIAL_PERSON: return new ArtificialPerson(number, address.castTo(InternalNonHostIdentifier.class));
                case EMAIL_PERSON: return new EmailPerson(number, address.castTo(EmailIdentifier.class));
                case MOBILE_PERSON: return new MobilePerson(number, address.castTo(MobileIdentifier.class));
                default: throw UnexpectedValueException.with("category", category);
            }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The address " + address + " does not match the category '" + category.name() + "'.", exception);
        }
    }
    
    
    /**
     * Maps numbers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Long, Identity> numbers = new ConcurrentHashMap<>();
    
    /**
     * Maps identifiers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Identifier, Identity> identifiers = new ConcurrentHashMap<>();
    
    /**
     * Clears the local maps of the mapper.
     */
    public static void clearLocalMaps() {
        numbers.clear();
        identifiers.clear();
    }
    
    /**
     * Removes the given identity from the local maps.
     * 
     * @param identity the identity which is to be removed.
     */
    public static void unmap(@Nonnull Identity identity) {
        numbers.remove(identity.getKey());
        identifiers.remove(identity.getAddress());
        Log.debugging("The identity of " + identity.getAddress() + " was unmapped.");
    }
    
    /**
     * Loads the identity with the given number from the database into the local hash map.
     * 
     * @param number the number of the identity to load.
     * 
     * @return the identity with the given number.
     * 
     * @require !numbers.containsKey(number) : "The given number is not yet loaded.";
     */
    @Locked
    @NonCommitting
    private static @Nonnull Identity loadIdentity(long number) throws DatabaseException {
        Require.that(!numbers.containsKey(number)).orThrow("The given number is not yet loaded.");
        
        final @Nonnull String SQL = "SELECT category, address FROM general_identity WHERE identity = " + number;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet.getByte(1));
                final @Nonnull Identifier address = IdentifierImplementation.get(resultSet, 2);
                final @Nonnull Identity identity = createIdentity(category, number, address);
                
                numbers.put(number, identity);
                identifiers.put(address, identity);
                
                return identity;
            } else {
                throw new SQLException("There exists no identity with the number " + number + ".");
            }
        }
    }
    
    /**
     * Returns the identity with the given number.
     * 
     * @param number the number of the identity.
     * 
     * @return the identity with the given number.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Locked
    @NonCommitting
    public static @Nonnull Identity getIdentity(long number) throws DatabaseException {
        @Nullable Identity identity = numbers.get(number);
        if (identity == null) { identity = loadIdentity(number); }
        try {
            if (identity instanceof Type) { ((Type) identity).ensureLoaded(); }
        } catch (@Nonnull RequestException | ExternalException | NetworkException exception) {
            throw UnexpectedFailureException.with("The type declaration and the referenced identities should already be cached.", exception);
        }
        return identity;
    }
    
    
    /**
     * Loads the identity and address of the given identifier from the database into the local hash map.
     * 
     * @param identifier the identifier of the identity to load.
     * 
     * @return whether the identity was successfully loaded.
     * 
     * @require !identifiers.containsKey(identifier) : "The given identifier is not yet loaded.";
     */
    @Locked
    @NonCommitting
    private static boolean loadIdentity(@Nonnull Identifier identifier) throws DatabaseException {
        Require.that(!identifiers.containsKey(identifier)).orThrow("The given identifier is not yet loaded.");
        
        Log.verbose("Try to load the identifier " + identifier + " from the database.");
        
        final @Nonnull String SQL = "SELECT general_identity.category, general_identity.identity, general_identity.address FROM general_identifier INNER JOIN general_identity ON general_identifier.identity = general_identity.identity WHERE general_identifier.identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet, 1);
                final long number = resultSet.getLong(2);
                final @Nonnull Identifier address = IdentifierImplementation.get(resultSet, 3);
                
                @Nullable Identity identity = numbers.get(number);
                if (identity instanceof InternalNonHostIdentity) {
                    if (!(address instanceof InternalNonHostIdentifier)) { throw new SQLException("The address " + address + " should be an internal non-host identifier."); }
                    if (identity instanceof InternalPerson) { ((InternalPerson) identity).setAddress((InternalNonHostIdentifier) address); }
                    else if (identity instanceof Type) { ((Type) identity).setAddress((InternalNonHostIdentifier) address); }
                } else {
                    final @Nonnull Identity newIdentity = createIdentity(category, number, address);
                    numbers.put(number, newIdentity);
                    if (identity instanceof ExternalPerson) {
                        if (!(address instanceof InternalNonHostIdentifier)) { throw new SQLException("The address " + address + " should be an internal non-host identifier."); }
                        ((ExternalPerson) identity).setAddress((InternalNonHostIdentifier) address);
                    }
                    identity = newIdentity;
                }
                
                identifiers.put(identifier, identity);
                if (!address.equals(identifier)) {
                    identifiers.put(address, identity);
                }
                
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Returns whether the given identifier is mapped.
     * 
     * @param identifier the identifier of interest.
     * 
     * @return whether the given identifier is mapped.
     */
    @Locked
    @NonCommitting
    public static boolean isMapped(@Nonnull Identifier identifier) throws DatabaseException {
        return identifiers.containsKey(identifier) || loadIdentity(identifier);
    }
    
    /**
     * Returns the mapped identity of the given identifier.
     * 
     * @param identifier the identifier whose identity is to be returned.
     * 
     * @return the mapped identity of the given identifier.
     * 
     * @require isMapped(identifier) : "The identifier is mapped.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Identity getMappedIdentity(@Nonnull Identifier identifier) throws DatabaseException {
        Require.that(isMapped(identifier)).orThrow("The identifier is mapped.");
        
        return identifiers.get(identifier);
    }
    
    
    /**
     * Maps the given identifier to a new number with the given category.
     * This method should only be called by {@link AccountOpen} outside of this class.
     * 
     * @param identifier the identifier to be mapped.
     * @param category the category of the identity to map.
     * @param reply the reply containing the category of the identity.
     * 
     * @return the newly mapped identity with the given identifier.
     * 
     * @ensure return.getCategory().equals(category) : "The category of the returned identity equals the given category.";
     */
    @Locked
    @NonCommitting
    public static @Nonnull Identity mapIdentity(@Nonnull Identifier identifier, @Nonnull Category category, @Nullable Reply reply) throws DatabaseException {
        if (isMapped(identifier)) {
            final @Nonnull Identity identity = identifiers.get(identifier);
            if (!identity.getCategory().equals(category)) { throw new SQLException("The identifier " + identifier + " should have been mapped with the category " + category + " but has already been mapped with the category " + identity.getCategory() + "."); }
            return identity;
        } else {
            try (@Nonnull Statement statement = Database.createStatement()) {
                final long key = Database.executeInsert(statement, "INSERT INTO general_identity (category, address, reply) VALUES (" + category + ", " + identifier + ", " + reply + ")");
                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + identifier + ", " + key + ")");
                Log.debugging("The identity with the identifier " + identifier + " was succesfully mapped.");
                // The identity is not added to the map since the transaction might be rolled back later on.
                return createIdentity(category, key, identifier);
            }
        }
    }
    
    /**
     * Maps the given host identifier to a host identity.
     * This method should only be called by {@link Host}.
     * 
     * @param identifier the identifier of the host to map.
     * 
     * @return the host identity of the mapped identifier.
     */
    @Locked
    @NonCommitting
    public static @Nonnull HostIdentity mapHostIdentity(@Nonnull HostIdentifier identifier) throws DatabaseException {
        try {
            return mapIdentity(identifier, Category.HOST, null).castTo(HostIdentity.class);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw ShouldNeverHappenError.get("The host with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a syntactic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the syntactic type of the mapped identifier.
     */
    @MainThread
    static @Nonnull SyntacticType mapSyntacticType(@Nonnull InternalNonHostIdentifier identifier) {
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        try {
            final @Nonnull SyntacticType type = mapIdentity(identifier, Category.SYNTACTIC_TYPE, null).castTo(SyntacticType.class);
            numbers.put(type.getKey(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            throw InitializationError.get("The syntactic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a semantic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the semantic type of the mapped identifier.
     */
    @MainThread
    static @Nonnull SemanticType mapSemanticType(@Nonnull InternalNonHostIdentifier identifier) {
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        try {
            final @Nonnull SemanticType type = mapIdentity(identifier, Category.SEMANTIC_TYPE, null).castTo(SemanticType.class);
            numbers.put(type.getKey(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            throw InitializationError.get("The semantic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given external identifier to an external person.
     * 
     * @param identifier the identifier of the external identifier to map.
     * 
     * @return the external person of the mapped identifier.
     */
    @Locked
    @NonCommitting
    private static @Nonnull ExternalPerson mapExternalIdentity(@Nonnull ExternalIdentifier identifier) throws DatabaseException, InvalidEncodingException {
        return mapIdentity(identifier, identifier.getCategory(), null).castTo(ExternalPerson.class);
    }
    
    
    /**
     * Merges the given identities into the new identity.
     * (This method should only be called by {@link AccountInitialize}.)
     * 
     * @param identities the identities which are to be merged.
     * @param newIdentity the new identity of the given identities.
     */
    @Locked
    @NonCommitting
    public static void mergeIdentities(@Nonnull ReadOnlyList<NonHostIdentity> identities, @Nonnull InternalNonHostIdentity newIdentity) throws DatabaseException {
        final long newNumber = newIdentity.getKey();
        try (@Nonnull Statement statement = Database.createStatement()) {
            for (final @Nonnull NonHostIdentity identity : identities) {
                final long oldNumber = identity.getKey();
                if (oldNumber != newNumber) {
                    updateReferences(statement, oldNumber, newNumber);
                    statement.executeUpdate("UPDATE general_identifier SET identity = " + newNumber + " WHERE identity = " + oldNumber);
                    statement.executeUpdate("DELETE FROM general_identity WHERE identity = " + oldNumber);
                    Log.debugging("The identity of " + identity.getAddress() + " was succesfully merged into " + newIdentity.getAddress() + ".");
                    unmap(identity);
                }
            }
        }
        if (identities.size() > 1) { Cache.invalidateCachedAttributeValues(newIdentity); }
    }
    
    /**
     * Establishes the identity of the given internal non-host identifier by checking
     * its existence and requesting its category, predecessors and successor.
     * 
     * @param identifier the identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given identifier.
     * 
     * @throws IdentityNotFoundException if no identity with the given identifier was found.
     */
    @Locked
    @NonCommitting
    private static @Nonnull InternalNonHostIdentity establishInternalNonHostIdentity(@Nonnull @NonMapped InternalNonHostIdentifier identifier) throws ExternalException {
        Require.that(!isMapped(identifier)).orThrow("The identifier is not mapped.");
        
        if (Server.hasHost(identifier.getHostIdentifier())) { throw IdentityNotFoundException.get(identifier); }
        
        // Query the identity of the given identifier.
        final @Nonnull IdentityReply reply;
        try {
            reply = new IdentityQuery(identifier).sendNotNull();
        } catch (@Nonnull RequestException exception) {
            if (exception.getCode() == RequestErrorCode.IDENTIFIER) { throw IdentityNotFoundException.get(identifier); else throw exception; }
        }
        final @Nonnull Category category = reply.getCategory();
        Log.verbose("The category of " + identifier + " is '" + category.name() + "'.");
        
        // Store all the predecessors of the given identifier into the database.
        final @Nonnull ReadOnlyPredecessors predecessors = reply.getPredecessors();
        predecessors.set(identifier, reply);
        Log.verbose("The " + identifier + " has the following predecessors: " + predecessors + ".");
        
        // Check that all the claimed and mapped predecessors have the right category, the indicated predecessors and do link back.
        final @Nonnull ReadOnlyList<NonHostIdentity> identities = predecessors.getIdentities();
        for (final @Nonnull NonHostIdentity identity : identities) {
            final @Nonnull NonHostIdentifier address = identity.getAddress();
            final @Nonnull String message = "The claimed predecessor " + address + " of " + identifier;
            if (!(identity.getCategory().isExternalPerson() && category.isInternalPerson() || identity.getCategory() == category)) { throw InvalidDeclarationException.get(message + " has a wrong category.", identifier, reply); }
            final @Nonnull Predecessor predecessor = new Predecessor(address);
            if (!predecessors.contains(predecessor)) { throw InvalidDeclarationException.get(message + " has other predecessors.", identifier, reply); }
            if (!Successor.getReloaded(address).equals(identifier)) { throw InvalidDeclarationException.get(message + " does not link back.", identifier, reply); }
        }
        
        final @Nonnull InternalNonHostIdentity identity;
        // Relocate the existing identity in case there is exactly one internal predecessor.
        if (identities.size() == 1 && identities.getNonNullable(0).getCategory().isInternalNonHostIdentity()) {
            identity = identities.getNonNullable(0).castTo(InternalNonHostIdentity.class);
            try (@Nonnull Statement statement = Database.createStatement()) {
                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + identifier + ", " + identity + ")");
                statement.executeUpdate("UPDATE general_identity SET address = " + identifier + " WHERE identity = " + identity);
            }
            unmap(identity);
            Log.debugging("The identity of " + identity.getAddress() + " was succesfully relocated to " + identifier + ".");
            
        // Create a new identity and merge existing predecessors into this new identity.
        } else {
            identity = mapIdentity(identifier, category, reply).castTo(InternalNonHostIdentity.class);
            if (identities.size() > 1 && !category.isInternalPerson()) { throw InvalidDeclarationException.get("Only internal persons may have more than one predecessor.", identifier, reply); }
            mergeIdentities(identities, identity);
        }
        Log.debugging("The identity of " + identifier + " was succesfully established.");
        
        // Store the successor of the given identifier into the database if available.
        final @Nullable InternalNonHostIdentifier successor = reply.getSuccessor();
        if (successor != null) {
            Successor.set(identifier, successor, reply);
            Log.verbose("The successor of " + identifier + " is " + successor + ".");
            if (!successor.getIdentity().equals(identifier.getIdentity())) { throw InvalidDeclarationException.get("The claimed successor " + successor + " of " + identifier + " does not link back.", identifier, reply); }
            return successor.getIdentity();
        } else {
            return identity;
        }
    }
    
    /**
     * Establishes the identity of the given external identifier by mapping it.
     * 
     * @param identifier the identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given identifier.
     */
    @Pure
    @Locked
    @NonCommitting
    private static @Nonnull Person establishExternalIdentity(@Nonnull @NonMapped ExternalIdentifier identifier) throws ExternalException {
        Require.that(!isMapped(identifier)).orThrow("The identifier is not mapped.");
        
        final @Nonnull Person person = mapExternalIdentity(identifier);
        try {
            final @Nonnull InternalNonHostIdentity identity = Successor.getReloaded(identifier).getIdentity();
            if (!identity.equals(identifier.getIdentity())) { throw InvalidDeclarationException.get("The claimed successor " + identity.getAddress() + " of " + identifier + " does not link back.", identifier); }
            return identity.castTo(Person.class);
        } catch (@Nonnull RequestException exception) {
            // TODO: Why is the external request error captured? Document (and change)!
            if (exception.getCode() == RequestErrorCode.EXTERNAL) { return person; }
            else { throw exception; }
        }
    }
    
    /**
     * Returns the identity of the given identifier.
     * The identity is also established if required.
     * (Only to be called from the identifier package.)
     * 
     * @param identifier the identifier of interest.
     * 
     * @return the identity of the given identifier.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws ExternalException {
        if (isMapped(identifier)) {
            Log.verbose("The identifier " + identifier + " is already mapped.");
            return identifiers.get(identifier);
        } else {
            if (identifier instanceof HostIdentifier) {
                Log.verbose("The host identifier " + identifier + " needs to be established.");
                return Cache.establishHostIdentity((HostIdentifier) identifier);
            } else if (identifier instanceof InternalNonHostIdentifier) {
                Log.verbose("The internal non-host identifier " + identifier + " needs to be established.");
                return establishInternalNonHostIdentity((InternalNonHostIdentifier) identifier);
            } else { assert identifier instanceof ExternalIdentifier;
                Log.verbose("The external identifier " + identifier + " needs to be established.");
                return establishExternalIdentity((ExternalIdentifier) identifier);
            }
        }
    }
    
}
