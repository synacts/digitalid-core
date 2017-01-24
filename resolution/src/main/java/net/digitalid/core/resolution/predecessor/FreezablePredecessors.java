package net.digitalid.core.resolution.predecessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.freezable.annotations.Freezable;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.freezable.annotations.NonFrozenRecipient;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.NonHostIdentity;

/**
 * This class models a list of {@link Predecessor predecessors}.
 * <p>
 * <em>Important:</em> Make sure that you don't violate the invariants when adding!
 * 
 * TODO: Support the export and import of all predecessors that belong to identifiers of a certain host.
 * 
 * @invariant doesNotContainNull() : "This list of predecessors does not contain null.";
 * @invariant doesNotContainDuplicates() : "This list of predecessors does not contain duplicates.";
 */
@GenerateSubclass
// TODO: @GenerateConverter
@Freezable(ReadOnlyPredecessors.class)
public abstract class FreezablePredecessors extends FreezableArrayList<@Nonnull Predecessor> implements ReadOnlyPredecessors {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates an empty list of predecessors.
     */
    @Recover
    public FreezablePredecessors() {}
    
    /**
     * Creates an empty list of predecessors with the given capacity.
     * 
     * @param initialCapacity the initial capacity of the list.
     */
    public FreezablePredecessors(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Creates new predecessors from the given predecessors.
     * 
     * @param predecessors the predecessors to add to the new predecessors.
     */
    public FreezablePredecessors(@Nonnull ReadOnlyPredecessors predecessors) {
        for (final @Nonnull Predecessor predecessor : predecessors) {
            add(predecessor);
        }
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public @Nonnull @Frozen ReadOnlyPredecessors freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezablePredecessors clone() {
        return new FreezablePredecessorsSubclass(this);
    }
    
    /* -------------------------------------------------- Identities -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull @Frozen ReadOnlyList<@Nonnull NonHostIdentity> getIdentities() throws ExternalException {
        final @Nonnull FreezableList<NonHostIdentity> identities = FreezableArrayList.withInitialCapacity(size());
        for (final @Nonnull Predecessor predecessor : this) {
            final @Nullable NonHostIdentity identity = predecessor.getIdentity();
            if (identity != null) { identities.add(identity); }
        }
        return identities.freeze();
    }
    
    /* -------------------------------------------------- Persistence -------------------------------------------------- */
    
    // TODO: Do with the new database API.
    
//    static {
//        Require.that(Threading.isMainThread()).orThrow("This static block is called in the main thread.");
//        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_predecessors (identifier " + IdentifierImplementation.FORMAT + " NOT NULL, predecessors " + Block.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
//            Database.onInsertIgnore(statement, "general_predecessors", "identifier");
//        } catch (@Nonnull SQLException exception) {
//            throw InitializationError.get("The database tables of the predecessors could not be created.", exception);
//        }
//    }
//    
//    /**
//     * Returns whether the predecessors of the given identifier exist.
//     */
//    @NonCommitting
//    public static boolean exist(@Nonnull InternalNonHostIdentifier identifier) throws DatabaseException {
//        final @Nonnull String SQL = "SELECT EXISTS (SELECT 1 FROM general_predecessors WHERE identifier = " + identifier + ")";
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            if (resultSet.next()) { return resultSet.getBoolean(1); }
//            else { throw new SQLException("This should never happen."); }
//        }
//    }
//    
//    /**
//     * Returns the predecessors of the given identifier as stored in the database.
//     * 
//     * @require exist(identifier) : "The predecessors of the given identifier exist.";
//     */
//    @Pure
//    @NonCommitting
//    public static @Nonnull @Frozen ReadOnlyPredecessors get(@Nonnull InternalNonHostIdentifier identifier) throws DatabaseException {
//        Require.that(exist(identifier)).orThrow("The predecessors of the given identifier exist.");
//        
//        final @Nonnull String SQL = "SELECT predecessors FROM general_predecessors WHERE identifier = " + identifier;
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            if (resultSet.next()) { return new FreezablePredecessors(Block.getNotNull(TYPE, resultSet, 1)).freeze(); }
//            else { throw new SQLException("The identifier " + identifier + " has no predecessors."); }
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("The predecessors of " + identifier + " have an invalid encoding.", exception);
//        }
//    }
//    
//    @Pure
//    @Override
//    @NonCommitting
//    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply reply) throws DatabaseException {
//        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO general_predecessors (identifier, predecessors, reply) VALUES (?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            identifier.set(preparedStatement, 1);
//            toBlock().set(preparedStatement, 2);
//            Reply.set(reply, preparedStatement, 3);
//            preparedStatement.executeUpdate();
//        }
//    }
    
}
