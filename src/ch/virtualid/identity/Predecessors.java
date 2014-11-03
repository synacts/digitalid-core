package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a list of {@link Predecessor predecessors}.
 * <p>
 * <em>Important:</em> Make sure that you don't violate the invariants when adding!
 * 
 * @invariant doesNotContainNull() : "This list of predecessors does not contain null.";
 * @invariant doesNotContainDuplicates() : "This list of predecessors does not contain duplicates.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Predecessors extends FreezableArrayList<Predecessor> implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@virtualid.ch}.
     * (This hack was necessary to get the initialization working.)
     */
    public static final @Nonnull SemanticType TYPE = Predecessor.PREDECESSORS;
    
    
    /**
     * Creates an empty list of predecessors.
     */
    public Predecessors() {}
    
    /**
     * Creates a new list of predecessors from the given block.
     * 
     * @param block the block containing the list of predecessors.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure isFrozen() : "This list of predecessors is frozen.";
     */
    public Predecessors(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> predecessors = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block predecessor : predecessors) {
            add(new Predecessor(predecessor));
        }
        freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> predecessors = new FreezableArrayList<Block>(size());
        for (final @Nonnull Predecessor predecessor : this) {
            predecessors.add(predecessor.toBlock());
        }
        return new ListWrapper(TYPE, predecessors.freeze()).toBlock();
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull Predecessor predecessor : this) {
            if (string.length() > 1) string.append(", ");
            string.append(predecessor);
        }
        return string.append("]").toString();
    }
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Predecessors)) return false;
        final @Nonnull Predecessors other = (Predecessors) object;
        
        final int size = this.size();
        if (size != other.size()) return false;
        
        for (int i = 0; i < size; i++) {
            if (!this.getNotNull(i).equals(other.getNotNull(i))) return false;
        }
        
        return true;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_predecessors (identifier " + Identifier.FORMAT + " NOT NULL, predecessors " + Block.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database tables of the predecessors could not be created.", exception);
        }
    }
    
    /**
     * Returns the predecessors of the given identifier as stored in the database.
     * Please note that the returned predecessors are only claimed and not yet verified.
     * 
     * @param identifier the identifier whose predecessors are to be returned.
     * 
     * @return the predecessors of the given identifier as stored in the database.
     * 
     * @require Mapper.isMapped(identifier) : "The identifier is mapped.";
     * 
     * @ensure return.isFrozen() : "The returned predecessors are frozen.";
     */
    public static @Nonnull Predecessors get(@Nonnull NonHostIdentifier identifier) throws SQLException {
        assert Mapper.isMapped(identifier) : "The identifier is mapped.";
        
        final @Nonnull String SQL = "SELECT predecessors FROM general_predecessors WHERE identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return new Predecessors(Block.get(TYPE, resultSet, 1));
            else throw new SQLException("The identifier " + identifier + " has no predecessors.");
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The predecessors of " + identifier + " have an invalid encoding.", exception);
        }
    }
    
    /**
     * Sets these values as the predecessors of the given identifier.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param reply the reply stating that the given identifier has these predecessors.
     * 
     * @require !Mapper.isMapped(identifier) : "The identifier is not mapped.";
     */
    public void set(@Nonnull NonHostIdentifier identifier, @Nullable Reply reply) throws SQLException {
        assert !Mapper.isMapped(identifier) : "The identifier is not mapped.";
        
        final @Nonnull String SQL = "INSERT INTO general_predecessors (identifier, predecessors) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, identifier.getString());
            this.toBlock().set(preparedStatement, 2);
            preparedStatement.executeUpdate();
        }
    }
    
}
