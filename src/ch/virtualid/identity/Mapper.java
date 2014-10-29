package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.EmailIdentifier;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.MobileIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Triplet;

/**
 * The mapper maps between identifiers and identities.
 * <p>
 * <em>Important:</em> Every reference to general_identity.identity needs to be registered
 * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
 * in order that the values can be updated when two {@link Person persons} have been
 * merged! (If the column can only contain {@link Type types}, this is not necessary.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.4
 */
public final class Mapper {
    
    /**
     * Stores the data type used to store identities in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference identities.
     * <p>
     * <em>Important:</em> Every reference to general_identity.identity needs to be registered
     * with {@link #addReference(java.lang.String, java.lang.String, java.lang.String...)}
     * in order that the values can be updated when two {@link Person persons} have been
     * merged! (If the column can only contain {@link Type types}, this is not necessary.)
     * <p>
     * Additionally, it might be a good idea to establish an index on the referencing column.
     */
    public static final @Nonnull String REFERENCE = new String("REFERENCES general_identity (identity) ON DELETE RESTRICT ON UPDATE RESTRICT");
    
    
    /**
     * Stores the logger of the identity mapper.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Mapper.log");
    
    
    /**
     * Stores the registered triplets of tables, columns and unique constraint that reference a person.
     */
    private static final @Nonnull List<Triplet<String, String, String[]>> references = new ArrayList<Triplet<String, String, String[]>>();
    
    /**
     * Adds the given table, columns and unique constraint to the list of registered references.
     * 
     * @param table the name of the table that references a person.
     * @param column the name of the column that references a person.
     * @param uniques the names of all the columns in the same unique constraint or nothing.
     */
    public static void addReference(@Nonnull String table, @Nonnull String column, @Nonnull String... uniques) {
        references.add(new Triplet<String, String, String[]>(table, column, uniques));
    }
    
    /**
     * Updates the references from the old to the new number using the given statement.
     * 
     * @param statement the statement on which the updates are executed.
     * @param oldNumber the old number of the person which is updated.
     * @param newNumber the new number of the person which is updated.
     */
    private static void updateReferences(@Nonnull Statement statement, long oldNumber, long newNumber) throws SQLException {
        for (final @Nonnull Triplet<String, String, String[]> reference : references) {
            final @Nonnull String table = reference.getValue0();
            final @Nonnull String column = reference.getValue1();
            final @Nonnull String IGNORE = Database.getConfiguration().IGNORE();
            if (IGNORE.isEmpty()) {
                final @Nonnull String[] uniques = reference.getValue2();
                if (uniques.length > 0) {
                    final @Nonnull StringBuilder SQL = new StringBuilder("DELETE FROM ").append(table).append(" AS a WHERE ").append(column).append(" = ").append(oldNumber);
                    SQL.append(" AND WHERE EXISTS (SELECT 1 FROM ").append(table).append(" AS b WHERE ");
                    boolean first = true;
                    for (final @Nonnull String unique : uniques) {
                        if (first) first = false;
                        else SQL.append(" AND ");
                        SQL.append("b.").append(unique).append(" = ");
                        if (unique.equals(column)) SQL.append(newNumber);
                        else SQL.append("a.").append(unique);
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
        assert Database.isMainThread() : "This method block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address " + Identifier.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_identifier (identifier " + Identifier.FORMAT + " NOT NULL, identity " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (identifier), FOREIGN KEY (identity) " + Mapper.REFERENCE + ")");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_predecessors (identifier " + Identifier.FORMAT + " NOT NULL, predecessor " + Database.getConfiguration().BLOB() + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_successor (identifier " + Identifier.FORMAT + " NOT NULL, successor " + Identifier.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database tables of the mapper could not be created.", exception);
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
     * @require (category == Category.HOST) == address.isHostIdentifier() : "The category is a host if and only if the identifier denotes a host.";
     * 
     * @ensure !(result instanceof Type) || !((Type) result).isLoaded() : "If the result is a type, its declaration has not yet been loaded.";
     */
    @Pure
    private static @Nonnull Identity createIdentity(@Nonnull Category category, long number, @Nonnull Identifier address) {
        assert (category == Category.HOST) == address instanceof HostIdentifier : "The category is a host if and only if the identifier denotes a host.";
        
        if (category == Category.HOST) return new HostIdentity(number, (HostIdentifier) address);
        
        final @Nonnull NonHostIdentifier identifier = (NonHostIdentifier) address;
        switch (category) {
            case SYNTACTIC_TYPE: return new SyntacticType(number, identifier);
            case SEMANTIC_TYPE: return new SemanticType(number, identifier);
            case NATURAL_PERSON: return new NaturalPerson(number, identifier);
            case ARTIFICIAL_PERSON: return new ArtificialPerson(number, identifier);
            case EMAIL_PERSON: return new EmailPerson(number, (EmailIdentifier) address);
            case MOBILE_PERSON: return new MobilePerson(number, (MobileIdentifier) address);
            default: throw new ShouldNeverHappenError("The category '" + category.name() + "' is not supported.");
        }
    }
    
    
    /**
     * Maps numbers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Long, Identity> numbers = new ConcurrentHashMap<Long, Identity>();
    
    /**
     * Maps identifiers onto identities by caching the corresponding entries from the database.
     */
    private static final @Nonnull Map<Identifier, Identity> identifiers = new ConcurrentHashMap<Identifier, Identity>();
    
    
    /**
     * Loads the identity with the given number from the database into the local hash map.
     * 
     * @param number the number of the identity to load.
     * 
     * @return the identity with the given number.
     */
    private static @Nonnull Identity loadIdentity(long number) throws SQLException {
        final @Nonnull String query = "SELECT category, address FROM general_identity WHERE identity = " + number;
        try (@Nonnull Statement statement = Database.getConnection().createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet.getByte(1));
                final @Nonnull Identifier address = Identifier.get(resultSet, 2);
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
    static @Nonnull Identity getIdentity(long number) throws SQLException {
        @Nullable Identity identity = numbers.get(number);
        if (identity == null) identity = loadIdentity(number);
        try {
            if (identity instanceof Type) ((Type) identity).ensureLoaded();
        } catch (@Nonnull IOException | PacketException | ExternalException  exception) {
            throw new ShouldNeverHappenError("The type declaration and the referenced identities should already be cached.", exception);
        }
        return identity;
    }
    
    
    /**
     * Loads the identity and address of the given identifier from the database into the local hash map.
     * 
     * @param identifier the identifier of the identity to load.
     * 
     * @return whether the identity was successfully loaded.
     */
    private static boolean loadIdentity(@Nonnull Identifier identifier) throws SQLException {
        final @Nonnull String query = "SELECT general_identity.category, general_identity.identity, general_identity.address FROM general_identifier INNER JOIN general_identity ON general_identifier.identity = general_identity.identity WHERE general_identifier.identifier = " + identifier;
        try (@Nonnull Statement statement = Database.getConnection().createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                final @Nonnull Category category = Category.get(resultSet, 1);
                final long number = resultSet.getLong(2);
                final @Nonnull Identifier address = Identifier.get(resultSet, 3);
                final @Nonnull Identity identity = createIdentity(category, number, address);
                
                numbers.put(number, identity);
                identifiers.put(identifier, identity);
                
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
    public static boolean isMapped(@Nonnull Identifier identifier) throws SQLException {
        return identifiers.containsKey(identifier) || loadIdentity(identifier);
    }
    
    /**
     * Maps the given identifier to a new number with the given category.
     * 
     * @param identifier the identifier to be mapped.
     * @param category the category of the identity to map.
     * @param reply the reply containing the category of the identity.
     * 
     * @return the newly mapped identity with the given identifier.
     * 
     * @require identifier instanceof HostIdentifier == (category == Category.HOST) : "The identifier denotes a host if and only if the given category is 'HOST'.";
     * 
     * @ensure return.getCategory().equals(category) : "The category of the returned identity equals the given category.";
     */
    private static @Nonnull Identity mapIdentity(@Nonnull Identifier identifier, @Nonnull Category category, @Nullable Reply reply) throws SQLException, InvalidEncodingException {
        assert identifier instanceof HostIdentifier == (category == Category.HOST) : "The identifier denotes a host if and only if the given category is 'HOST'.";
        
        if (isMapped(identifier)) {
            @Nonnull Identity identity =  identifiers.get(identifier);
            if (!identity.getCategory().equals(category)) throw new InvalidEncodingException("The identifier " + identifier + " should have been mapped with the category " + category + " but has already been mapped with the category " + identity.getCategory() + ".");
            return identity;
        } else {
            try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
                final long key = Database.getConfiguration().executeInsert(statement, "INSERT INTO general_identity (category, address, reply) VALUES (" + category + ", " + identifier + ", " + reply + ")");
                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + identifier + ", " + key + ")");
                LOGGER.log(Level.INFORMATION, "The identity with the identifier " + identifier + " was succesfully mapped.");
                // The identity is not added to the map since the transaction might be rollbacked later on.
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
    public static @Nonnull HostIdentity mapHostIdentity(@Nonnull HostIdentifier identifier) throws SQLException {
        try {
            return mapIdentity(identifier, Category.HOST, null).toHostIdentity();
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("The host with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a syntactic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the syntactic type of the mapped identifier.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    static @Nonnull SyntacticType mapSyntacticType(@Nonnull NonHostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            final @Nonnull SyntacticType type = mapIdentity(identifier, Category.SYNTACTIC_TYPE, null).toSyntacticType();
            numbers.put(type.getNumber(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            throw new InitializationError("The syntactic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given identifier to a semantic type.
     * 
     * @param identifier the non-host identifier to map.
     * 
     * @return the semantic type of the mapped identifier.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    static @Nonnull SemanticType mapSemanticType(@Nonnull NonHostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            final @Nonnull SemanticType type = mapIdentity(identifier, Category.SEMANTIC_TYPE, null).toSemanticType();
            numbers.put(type.getNumber(), type);
            identifiers.put(identifier, type);
            return type;
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            throw new InitializationError("The semantic type with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Maps the given external identifier to an external identity.
     * This method should only be called by {@link EmailPerson} and {@link MobilePerson}.
     * 
     * @param identifier the identifier of the external identifier to map.
     * 
     * @return the external identity of the mapped identifier.
     */
    public static @Nonnull ExternalIdentity mapExternalIdentity(@Nonnull ExternalIdentifier identifier) throws SQLException, InvalidEncodingException {
        return mapIdentity(identifier, identifier.getCategory(), null).toExternalIdentity();
    }
    
    
    /**
     * Returns the identity of the given identifier.
     * The identity is also established if required.
     * 
     * @param identifier the identifier of interest.
     * 
     * @return the identity of the given identifier.
     */
    public static @Nonnull Identity getIdentity(@Nonnull InternalIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        if (isMapped(identifier)) {
            return identifiers.get(identifier);
        } else {
            return establishIdentity(identifier);
        }
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
    public static @Nonnull Identity getMappedIdentity(@Nonnull Identifier identifier) throws SQLException {
        assert isMapped(identifier) : "The identifier is mapped.";
        
        return identifiers.get(identifier);
    }
    
    /**
     * Relocates the identity with the given identifier.
     * 
     * @param identifier the identifier of the identity that has been relocated.
     * 
     * @return the new address of the identity with the given identifier.
     * 
     * @throws PacketException if the identity with the given identifier has not been relocated.
     */
    public static @Nonnull NonHostIdentifier relocate(@Nonnull NonHostIdentifier identifier) throws PacketException {
        // TODO: Write a real implementation.
        
//        if (reply instanceof IdentityReply) {
//            final @Nonnull Identifier subject = reply.getSubject();
//            final @Nullable NonHostIdentifier successor = ((IdentityReply) reply).getSuccessor();
//            if (successor == null) throw new InvalidEncodingException("The successor of the unexpected identity reply to a query for " + subject + " may not be null.");
//            if (!(subject instanceof NonHostIdentifier)) throw new InvalidEncodingException("An unexpected identity reply may only be returned for non-host identities and not for " + subject + ".");
//            Mapper.setSuccessor((NonHostIdentifier) subject, successor, reply);
//            if (!successor.getIdentity().equals(subject.getIdentity())) throw new InvalidDeclarationException("The indicated successor " + successor + " is not an identifier of the identity denoted by " + subject + ".", subject, reply);
//            subject = successor;
//            recipient = subject.getHostIdentifier();
//        }
        
        throw new PacketException(PacketError.EXTERNAL, "The identity with the identifier " + identifier + " has not been relocated.");
    }
    
    /**
     * Establishes the identity of the given identifier by checking its existence and requesting its category, predecessors and successor.
     * 
     * @param identifier the identifier whose identity is to be established.
     * 
     * @return the newly established identity of the given identifier.
     * 
     * @throws IdentityNotFoundException if no identity with the given identifier could be found.
     */
    private static @Nonnull InternalIdentity establishIdentity(@Nonnull InternalIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        throw new IdentityNotFoundException(identifier);
        
        // TODO: Make sure that the merging also works if the successor is just loaded from the database. Or are existing identities intensionally left untouched?
        
        // TODO: In case of a host identifier, query the public key with a new method in the cache class.
        
//        try {
//            // TODO: Make an identity request and verify predecessors only if already mapped.
//            @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(NonHostIdentifier.IDENTITY_REQUEST, Block.EMPTY);
//            @Nullable Identity identity = null;
//            
//            if (identifier instanceof HostIdentifier) {
//                @Nonnull Response response = new Request(content, (HostIdentifier) identifier, identifier).send(false);
//                @Nonnull Block[] elements = new TupleWrapper(response.getContent().getElement()).getElementsNotNull(3);
//                @Nonnull Category category = Category.get(elements[0]);
//                if (category != HOST) throw new InvalidDeclarationException("The request to get the category of " + identifier + " returned the invalid value '" + category + "'.");
//                identity = mapHostIdentity((HostIdentifier) identifier);
//                response.getSignature().verify();
//            } else {
//                @Nonnull NonHostIdentifier nonHostIdentifier = (NonHostIdentifier) identifier;
//                try {
//                    @Nonnull Response response = new Request(content, nonHostIdentifier).send();
//                    @Nonnull Block[] elements = new TupleWrapper(response.getContents().getElement()).getElementsNotNull(3);
//                    @Nonnull Category category = Category.get(elements[0]);
//                    if (category == HOST) throw new InvalidDeclarationException("The request to get the category of " + nonHostIdentifier + " returned the invalid value '" + category + "'.");
//                    
//                    // Store all the predecessors of the given identifier into the database.
//                    @Nonnull List<NonHostIdentifier> predecessors = new ListWrapper(elements[1]).getElements(NonHostIdentifier.class);
//                    setPredecessors(nonHostIdentifier, predecessors);
//                    
//                    // Store the successor of the given identifier into the database if available.
//                    if (elements[2].isNotEmpty()) {
//                        @Nonnull NonHostIdentifier successor = new NonHostIdentifier(elements[2]);
//                        setSuccessor(nonHostIdentifier, successor);
//                        if (isMapped(successor)) {
//                            identity = getIdentity(successor);
//                            if (identity.getCategory() != category) throw new InvalidDeclarationException("The claimed successor " + successor + " of " + nonHostIdentifier + " is of a different category.");
//                            if (!getPredecessors(successor).contains(nonHostIdentifier)) throw new InvalidDeclarationException("The claimed successor " + successor + " of " + nonHostIdentifier + " does not link back.");
//                        }
//                    }
//                    
//                    // Verify the cagetory and successor of all predecessors and remove identities that cannot be retrieved from the list of predecessors.
//                    @Nonnull Iterator<NonHostIdentifier> iterator = predecessors.iterator();
//                    while (iterator.hasNext()) {
//                        try {
//                            @Nonnull NonHostIdentifier predecessor = iterator.next();
//                            @Nonnull Category oldCategory = getIdentity(predecessor).getCategory();
//                            if (oldCategory == EMAIL_PERSON) {
//                                if (category != NATURAL_PERSON && category != ARTIFICIAL_PERSON) throw new InvalidDeclarationException("The email address " + predecessor + " can only be claimed by a natural or artificial person and thus not by " + nonHostIdentifier + ".");
//                            } else {
//                                if (oldCategory != category) throw new InvalidDeclarationException("The claimed predecessor " + predecessor + " of " + nonHostIdentifier + " is of a different category.");
//                            }
//                            if (!nonHostIdentifier.equals(getSuccessorReloaded(predecessor))) throw new InvalidDeclarationException("The claimed predecessor " + predecessor + " of " + nonHostIdentifier + " does not link back.");
//                        } catch (@Nonnull IdentityNotFoundException exception) {
//                            iterator.remove();
//                        }
//                    }
//                    
//                    if (identity == null) {
//                        // Relocate the existing identity in case there is only one predecessor.
//                        if (predecessors.size() == 1) {
//                            identity = getIdentity(predecessors.get(0));
//                            try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
//                                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + nonHostIdentifier + ", " + identity + ")");
//                                statement.executeUpdate("UPDATE general_identity SET identifier = " + nonHostIdentifier + " WHERE identity = " + identity);
//                                connection.commit();
//                            }
//                            identifiers.put(nonHostIdentifier, identity);
//                            
//                        // Create a new identity and merge existing predecessors into this new identity.
//                        } else {
//                            identity = mapIdentity(nonHostIdentifier, category);
//                        }
//                    }
//                    
//                    // Merge existing predecessors into this new identity if necessary.
//                    if (predecessors.size() > 0) {
//                        if (predecessors.size() > 1 && !(identity instanceof Person)) throw new InvalidDeclarationException("Only person identities may have more than one predecessor: " + nonHostIdentifier + " is a " + category.name() + ".");
//                        long newNumber = identity.getNumber();
//                        for (@Nonnull NonHostIdentifier predecessor : predecessors) {
//                            long oldNumber = getIdentity(predecessor).getNumber();
//                            updateIdentities(oldNumber, newNumber, nonHostIdentifier);
//                            if (oldNumber != newNumber) {
//                                try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
//                                    statement.executeUpdate("UPDATE general_identifier SET identity = " + newNumber + " WHERE identity = " + oldNumber);
//                                    statement.executeUpdate("DELETE FROM general_identity WHERE identity = " + oldNumber);
//                                    updateReferences(statement, oldNumber, newNumber);
//                                    connection.commit();
//                                }
//                                logger.log(INFORMATION, "The identity with the identifier " + predecessor + " was succesfully merged into " + nonHostIdentifier + ".");
//                            } else {
//                                logger.log(INFORMATION, "The identity with the identifier " + predecessor + " was succesfully relocated to " + nonHostIdentifier + ".");
//                            }
//                        }
//                    }
//                } catch (@Nonnull FailedRequestException exception) {
//                    // Determine whether the given identifier denotes an email address and remember the identifier as unreachable otherwise.
//                    if (EmailPerson.providerExists(nonHostIdentifier)) {
//                        @Nullable NonHostIdentifier successor = getSuccessorReloaded(nonHostIdentifier);
//                        if (successor != null && isMapped(successor)) {
//                            identity = getIdentity(successor);
//                            if (identity.getCategory() != NATURAL_PERSON && identity.getCategory() != ARTIFICIAL_PERSON) throw new InvalidDeclarationException("The claimed successor " + successor + " of the email address " + nonHostIdentifier + " is a natural or an artificial person.");
//                            if (!getPredecessors(successor).contains(nonHostIdentifier)) throw new InvalidDeclarationException("The claimed successor " + successor + " of " + nonHostIdentifier + " does not link back.");
//                            try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
//                                statement.executeUpdate("INSERT INTO general_identifier (identifier, identity) VALUES (" + nonHostIdentifier + ", " + identity + ")");
//                                connection.commit();
//                            }
//                            identifiers.put(nonHostIdentifier, identity);
//                            logger.log(INFORMATION, "The email address " + nonHostIdentifier + " was succesfully attributed to " + successor + ".");
//                        } else {
//                            identity = mapIdentity(nonHostIdentifier, EMAIL_PERSON, null);
//                        }
//                    } else {
//                        // TODO: general_unreachable no longer exists!
//                        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
//                            statement.executeUpdate("REPLACE INTO general_unreachable (identifier, time) VALUES (" + nonHostIdentifier + ", " + (System.currentTimeMillis() + 60000) + ")");
//                            connection.commit();
//                        }
//                        throw new IdentityNotFoundException(nonHostIdentifier, exception);
//                    }
//                }
//            }
//            
//            logger.log(INFORMATION, "The identity of " + identifier + " was succesfully established.");
//            return identity;
//        } catch (@Nonnull FailedEncodingException | FailedRequestException | InvalidEncodingException | InvalidSignatureException | PacketException | InvalidDeclarationException | IdentityNotFoundExceptionexception) {
//            logger.log(WARNING, "The identity of " + identifier + " could not be established.", exception);
//            if (exception instanceof IdentityNotFoundException) throw (IdentityNotFoundException) exception;
//            else throw new IdentityNotFoundException(identifier, exception);
//        }
    }
    
    
    /**
     * Returns the predecessors of the given identifier as stored in the database.
     * Please note that the returned predecessors are only claimed and not yet verified.
     * 
     * @param identifier the identifier of interest.
     * 
     * @return the predecessors of the given identifier as stored in the database.
     */
    public static @Nonnull ReadonlyList<NonHostIdentifier> getPredecessors(@Nonnull NonHostIdentifier identifier) throws SQLException {
        FreezableList<NonHostIdentifier> predecessors = new FreezableLinkedList<NonHostIdentifier>();
        @Nonnull String query = "SELECT predecessor FROM general_predecessors WHERE identifier = " + identifier;
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                predecessors.add(new NonHostIdentifier(resultSet.getString(1)));
            }
            connection.commit();
        }
        return predecessors.freeze();
    }
    
    /**
     * Sets the predecessors of the given identifier to the given values.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param predecessors the predecessors to be set for the given identifier.
     */
    public static void setPredecessors(@Nonnull NonHostIdentifier identifier, @Nonnull List<NonHostIdentifier> predecessors) throws SQLException, InvalidDeclarationException {
        if (!predecessors.isEmpty()) {
            @Nonnull String statement = "INSERT" + Database.getConfiguration().IGNORE() + " INTO general_predecessors (identifier, predecessor) VALUES (?, ?)";
            try (@Nonnull Connection connection = Database.getConnection(); @Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, identifier.getString());
                for (@Nonnull NonHostIdentifier predecessor : predecessors) {
                    if (getSetOfPredecessors(predecessor).contains(identifier)) throw new InvalidDeclarationException("" + predecessor + " cannot be set as a predecessor of " + identifier + " as the latter is already a predecessor of the former.", identifier, null); // TODO: The reply shouldn't be null!
                    preparedStatement.setString(2, predecessor.getString());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            }
        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database.
     * Please note that the returned successor is only claimed and not yet verified.
     * 
     * @param identifier the identifier of interest.
     * @return the successor of the given identifier as stored in the database.
     */
    public static @Nullable NonHostIdentifier getSuccessor(@Nonnull NonHostIdentifier identifier) throws SQLException {
        @Nonnull String query = "SELECT successor FROM general_successor WHERE identifier = " + identifier;
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return new NonHostIdentifier(resultSet.getString(1));
            }
            connection.commit();
        }
        return null;
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database or retrieved by a new request.
     * Please note that the returned successor is only claimed and not yet verified.
     * 
     * @param identifier the identifier of interest.
     * @return the successor of the given identifier as stored in the database or retrieved by a new request.
     */
    public static @Nullable NonHostIdentifier getSuccessorReloaded(@Nonnull NonHostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        @Nullable NonHostIdentifier successor = getSuccessor(identifier);
        if (successor == null) {
            if (getIdentity(identifier).getCategory() == Category.EMAIL_PERSON) {
                // TODO: Load the verified successor from 'virtualid.ch' or return null otherwise.
                throw new UnsupportedOperationException("The verification of email addresses is not supported yet.");
            } else {
//                @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(NonHostIdentifier.IDENTITY_REQUEST, Block.EMPTY);
//                @Nonnull Response response = new Request(content, identifier).send();
//                @Nonnull Block[] elements = new TupleWrapper(response.getContents().getElement()).getElementsNotNull(3);
//                if (elements[2].isNotEmpty()) successor = new NonHostIdentifier(elements[2]);
            }
            
            if (successor != null) {
                try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
                    statement.executeUpdate("INSERT INTO general_successor (identifier, successor) VALUES (" + identifier + ", " + successor + ")");
                    connection.commit();
                }
            }
        }
        return successor;
    }
    
    /**
     * Sets the successor of the given identifier to the given value.
     * 
     * @param identifier the identifier whose successor is to be set.
     * @param successor the successor to be set for the given identifier.
     */
    public static void setSuccessor(@Nonnull NonHostIdentifier identifier, @Nonnull NonHostIdentifier successor, @Nonnull Reply reply) throws SQLException, InvalidDeclarationException {
        // TODO: Also store the reference to the reply in the database.
        if (getListOfSuccessors(successor).contains(identifier)) throw new InvalidDeclarationException("" + successor + " cannot be set as the successor of " + identifier + " as the latter is already a successor of the former.", identifier, reply);
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT" + Database.getConfiguration().IGNORE() + " INTO general_successor (identifier, successor) VALUES (" + identifier + ", " + successor + ")");
            connection.commit();
        }
    }
    
    /**
     * Returns the list of predecessors of (and including) the given identifier as stored in the database.
     * Please note that the returned predecessors are only claimed and not necessarily verified.
     * 
     * @param identifier the identifier of interest.
     * @return the list of predecessors of (and including) the given identifier as stored in the database.
     */
    @Deprecated // TODO: Probably better solved by the Predecessor class.
    private static @Nonnull Set<NonHostIdentifier> getSetOfPredecessors(@Nonnull NonHostIdentifier identifier) throws SQLException {
        @Nonnull ReadonlyList<NonHostIdentifier> predecessors = getPredecessors(identifier);
        @Nonnull Set<NonHostIdentifier> set = predecessors.isEmpty() ? new HashSet<NonHostIdentifier>() : getSetOfPredecessors(predecessors.getNotNull(0));
        @Nonnull Iterator<NonHostIdentifier> iterator = predecessors.iterator();
        if (iterator.hasNext()) iterator.next();
        while (iterator.hasNext()) set.addAll(getSetOfPredecessors(iterator.next()));
        set.add(identifier);
        return set;
    }
    
    /**
     * Returns the list of successors of (and including) the given identifier as stored in the database.
     * Please note that the returned successors are only claimed and not necessarily verified.
     * 
     * @param identifier the identifier of interest.
     * @return the list of successors of (and including) the given identifier as stored in the database.
     */
    private static @Nonnull LinkedList<NonHostIdentifier> getListOfSuccessors(@Nonnull NonHostIdentifier identifier) throws SQLException {
        @Nullable NonHostIdentifier successor = getSuccessor(identifier);
        @Nonnull LinkedList<NonHostIdentifier> list = successor == null ? new LinkedList<NonHostIdentifier>() : getListOfSuccessors(successor);
        list.addFirst(identifier);
        return list;
    }
    
}
