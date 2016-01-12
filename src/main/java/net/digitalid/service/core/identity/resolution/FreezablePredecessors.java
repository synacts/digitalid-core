package net.digitalid.service.core.identity.resolution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

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
public final class FreezablePredecessors extends FreezableArrayList<Predecessor> implements ReadOnlyPredecessors {
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    public static final @Nonnull SemanticType TYPE = Predecessor.PREDECESSORS;
    
    
    /**
     * Creates an empty list of predecessors.
     */
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
    
    /**
     * Creates a new list of predecessors from the given block.
     * 
     * @param block the block containing the list of predecessors.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public FreezablePredecessors(@Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> predecessors = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block predecessor : predecessors) {
            add(new Predecessor(predecessor));
        }
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> predecessors = FreezableArrayList.getWithCapacity(size());
        for (final @Nonnull Predecessor predecessor : this) {
            predecessors.add(predecessor.toBlock());
        }
        return ListWrapper.encode(TYPE, predecessors.freeze());
    }
    
    
    @Override
    public @Nonnull @Frozen ReadOnlyPredecessors freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezablePredecessors clone() {
        return new FreezablePredecessors(this);
    }
    
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull @Frozen @NonNullableElements ReadOnlyList<NonHostIdentity> getIdentities() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull FreezableList<NonHostIdentity> identities = FreezableArrayList.getWithCapacity(size());
        for (final @Nonnull Predecessor predecessor : this) {
            final @Nullable NonHostIdentity identity = predecessor.getIdentity();
            if (identity != null) { identities.add(identity); }
        }
        return identities.freeze();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof FreezablePredecessors)) { return false; }
        final @Nonnull FreezablePredecessors other = (FreezablePredecessors) object;
        
        final int size = this.size();
        if (size != other.size()) { return false; }
        
        for (int i = 0; i < size; i++) {
            if (!this.getNonNullable(i).equals(other.getNonNullable(i))) { return false; }
        }
        
        return true;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    
    static {
        assert Threading.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_predecessors (identifier " + IdentifierImplementation.FORMAT + " NOT NULL, predecessors " + Block.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
            Database.onInsertIgnore(statement, "general_predecessors", "identifier");
        } catch (@Nonnull SQLException exception) {
            throw InitializationError.get("The database tables of the predecessors could not be created.", exception);
        }
    }
    
    /**
     * Returns whether the predecessors of the given identifier exist.
     * 
     * @param identifier the identifier whose predecessors are to be checked.
     * 
     * @return whether the predecessors of the given identifier exist.
     */
    @NonCommitting
    public static boolean exist(@Nonnull InternalNonHostIdentifier identifier) throws DatabaseException {
        final @Nonnull String SQL = "SELECT EXISTS (SELECT 1 FROM general_predecessors WHERE identifier = " + identifier + ")";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return resultSet.getBoolean(1); }
            else { throw new SQLException("This should never happen."); }
        }
    }
    
    /**
     * Returns the predecessors of the given identifier as stored in the database.
     * 
     * @param identifier the identifier whose predecessors are to be returned.
     * 
     * @return the predecessors of the given identifier as stored in the database.
     * 
     * @require exist(identifier) : "The predecessors of the given identifier exist.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull @Frozen ReadOnlyPredecessors get(@Nonnull InternalNonHostIdentifier identifier) throws DatabaseException {
        assert exist(identifier) : "The predecessors of the given identifier exist.";
        
        final @Nonnull String SQL = "SELECT predecessors FROM general_predecessors WHERE identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return new FreezablePredecessors(Block.getNotNull(TYPE, resultSet, 1)).freeze(); }
            else { throw new SQLException("The identifier " + identifier + " has no predecessors."); }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The predecessors of " + identifier + " have an invalid encoding.", exception);
        }
    }
    
    @Pure
    @Override
    @NonCommitting
    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply reply) throws DatabaseException {
        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO general_predecessors (identifier, predecessors, reply) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            identifier.set(preparedStatement, 1);
            toBlock().set(preparedStatement, 2);
            Reply.set(reply, preparedStatement, 3);
            preparedStatement.executeUpdate();
        }
    }
    
}
