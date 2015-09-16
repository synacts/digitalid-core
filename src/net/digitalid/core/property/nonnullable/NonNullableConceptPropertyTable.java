package net.digitalid.core.property.nonnullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.client.Client;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.property.ConceptPropertyTable;
import net.digitalid.core.storable.AbstractFactory;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;

/**
 * Description.
 * 
 * A service consists of modules and a module consists of tables.
 * 
 * Each table has a name and some columns:
 * – the entity
 * – the time
 * – a storable object?
 * 
 * The module class could be always the same and contains a list of tables.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Immutable
public class NonNullableConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends ConceptPropertyTable<V, C, E> {
    
    private NonNullableConceptPropertyTable(@Nonnull AbstractFactory<V> factory) {
        this.factory = factory;
    }
    
    @Capturable @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property) {
        // TODO: Index all properties with their entity in order to be able to reset them. Alternatively, pass the concept index along?
        
        V v = null;
        return FreezablePair.get(new Time(), v).freeze();
    }
    
    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull V oldValue, @Nonnull V newValue) {
        // TODO!
    }
    
    
    // TODO: The following code serves just as an example and should be removed afterwards.
    
    /**
     * Returns the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be returned.
     * 
     * @return the name of the given client agent.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    @Pure
    @NonCommitting
    static @Nonnull String getName(@Nonnull ClientAgent clientAgent) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT name FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String name = resultSet.getString(1);
                if (!Client.isValidName(name)) throw new SQLException("The name of the client agent with the number " + clientAgent + " is invalid.");
                return name;
            } else throw new SQLException("The given client agent has no name.");
        }
    }
    
    /**
     * Replaces the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be replaced.
     * @param oldName the old name of the given client agent.
     * @param newName the new name of the given client agent.
     * 
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    @NonCommitting
    static void replaceName(@Nonnull ClientAgent clientAgent, @Nonnull String oldName, @Nonnull String newName) throws SQLException {
        assert Client.isValidName(oldName) : "The old name is valid.";
        assert Client.isValidName(newName) : "The new name is valid.";
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET name = ? WHERE entity = " + entity + " AND agent = " + clientAgent + " AND name = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, oldName);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The name of the client agent with the number " + clientAgent + " could not be replaced.");
        }
    }
    
}
