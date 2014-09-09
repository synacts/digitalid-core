package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Commitment;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Agents;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models a client that acts as an agent on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientAgent extends Agent implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the aspect of the commitment being changed at the observed client agent.
     */
    public static final @Nonnull Aspect COMMITMENT = new Aspect(ClientAgent.class, "commitment changed");
    
    /**
     * Stores the aspect of the name being changed at the observed client agent.
     */
    public static final @Nonnull Aspect NAME = new Aspect(ClientAgent.class, "name changed");
    
    /**
     * Stores the aspect of the icon being changed at the observed client agent.
     */
    public static final @Nonnull Aspect ICON = new Aspect(ClientAgent.class, "icon changed");
    
    
    /**
     * Stores the maximal length of the name.
     */
    public static final int NAME_LENGTH = 50;
    
    /**
     * Stores the size of the square icon as number of pixels horizontally and vertically.
     */
    public static final int ICON_SIZE = 256;
    
    
    /**
     * Stores the commitment of this client agent.
     */
    private @Nullable Commitment commitment;
    
    /**
     * Stores the name of this client agent.
     * 
     * @invariant name.length() <= NAME_LENGTH : "The name has at most the indicated amount of characters.";
     */
    private @Nullable String name;
    
    /**
     * Stores the icon of this client agent.
     * 
     * @invariant icon.isSquare(ICON_SIZE) : "The icon has the specified size.";
     */
    private @Nullable Image icon;
    
    /**
     * Creates a new client agent with the given entity and number.
     * 
     * @param entity the entity to which this client agent belongs.
     * @param number the number that references this client agent.
     * @param removed whether this client agent has been removed.
     */
    private ClientAgent(@Nonnull Entity entity, long number, boolean removed) {
        super(entity, number, removed);
    }
    
    
    /**
     * Returns the commitment of this client agent.
     * 
     * @return the commitment of this client agent.
     */
    public @Nonnull Commitment getCommitment() throws SQLException {
        if (commitment == null) {
            commitment = Agents.getCommitment(this);
        }
        return commitment;
    }
    
    /**
     * Sets the commitment of this client agent.
     * 
     * @param newCommitment the new commitment of this client agent.
     */
    public void setCommitment(@Nonnull Commitment newCommitment) throws SQLException {
        final @Nonnull Commitment oldCommitment = getCommitment();
        if (!newCommitment.equals(oldCommitment)) {
            Synchronizer.execute(new ClientAgentCommitmentReplace(this, oldCommitment, newCommitment));
        }
    }
    
    /**
     * Replaces the commitment of this client agent.
     * 
     * @param oldCommitment the old commitment of this client agent.
     * @param newCommitment the new commitment of this client agent.
     */
    @OnlyForActions
    public void replaceCommitment(@Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) throws SQLException {
        Agents.replaceCommitment(this, oldCommitment, newCommitment);
        commitment = newCommitment;
        notify(COMMITMENT);
    }
    
    
    /**
     * Returns the name of this client agent.
     * 
     * @return the name of this client agent.
     */
    public @Nonnull String getName() throws SQLException {
        if (name == null) {
            name = Agents.getName(this);
        }
        return name;
    }
    
    /**
     * Sets the name of this client agent.
     * 
     * @param newName the new name of this client agent.
     * 
     * @require name.length() <= NAME_LENGTH : "The name has at most the indicated amount of characters.";
     */
    public void setName(@Nonnull String newName) throws SQLException {
        assert newName.length() <= NAME_LENGTH : "The new name has at most the indicated amount of characters.";
        
        final @Nonnull String oldName = getName();
        if (!newName.equals(oldName)) {
            Synchronizer.execute(new ClientAgentNameReplace(this, oldName, newName));
        }
    }
    
    /**
     * Replaces the name of this client agent.
     * 
     * @param oldName the old name of this client agent.
     * @param newName the new name of this client agent.
     * 
     * @require oldName.length() <= NAME_LENGTH : "The old name has at most the indicated amount of characters.";
     * @require newName.length() <= NAME_LENGTH : "The new name has at most the indicated amount of characters.";
     */
    @OnlyForActions
    public void replaceName(@Nonnull String oldName, @Nonnull String newName) throws SQLException {
        assert oldName.length() <= NAME_LENGTH : "The old name has at most the indicated amount of characters.";
        assert newName.length() <= NAME_LENGTH : "The new name has at most the indicated amount of characters.";
        
        Agents.replaceName(this, oldName, newName);
        name = newName;
        notify(NAME);
    }
    
    
    /**
     * Returns the icon of this client agent.
     * 
     * @return the icon of this client agent.
     */
    public @Nonnull Image getIcon() throws SQLException {
        if (icon == null) {
            icon = Agents.getIcon(this);
        }
        return icon;
    }
    
    /**
     * Sets the icon of this client agent.
     * 
     * @param newIcon the new icon of this client agent.
     * 
     * @require newIcon.isSquare(ICON_SIZE) : "The new icon has the specified size.";
     */
    public void setIcon(@Nonnull Image newIcon) throws SQLException {
        assert newIcon.isSquare(ICON_SIZE) : "The new icon has the specified size.";
        
        final @Nonnull Image oldIcon = getIcon();
        if (!newIcon.equals(oldIcon)) {
            Synchronizer.execute(new ClientAgentIconReplace(this, oldIcon, newIcon));
        }
    }
    
    /**
     * Replaces the icon of this client agent.
     * 
     * @param oldIcon the old icon of this client agent.
     * @param newIcon the new icon of this client agent.
     * 
     * @require oldIcon.isSquare(ICON_SIZE) : "The old icon has the specified size.";
     * @require newIcon.isSquare(ICON_SIZE) : "The new icon has the specified size.";
     */
    @OnlyForActions
    public void replaceIcon(@Nonnull Image oldIcon, @Nonnull Image newIcon) throws SQLException {
        assert oldIcon.isSquare(ICON_SIZE) : "The old icon has the specified size.";
        assert newIcon.isSquare(ICON_SIZE) : "The new icon has the specified size.";
        
        Agents.replaceIcon(this, oldIcon, newIcon);
        icon = newIcon;
        notify(ICON);
    }
    
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    
    /**
     * Caches client agents given their entity and number.
     */
    private static final @Nonnull Map<Pair<Entity, Long>, ClientAgent> index = new HashMap<Pair<Entity, Long>, ClientAgent>();
    
    /**
     * Returns a (locally cached) client agent that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the client agent belongs.
     * @param number the number that denotes the client agent.
     * @param removed whether the client agent has been removed.
     * 
     * @return a new or existing client agent with the given entity and number.
     */
    @Pure
    public static @Nonnull ClientAgent get(@Nonnull Entity entity, long number, boolean removed) {
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Entity, Long> pair = new Pair<Entity, Long>(entity, number);
                @Nullable ClientAgent clientAgent = index.get(pair);
                if (clientAgent == null) {
                    clientAgent = new ClientAgent(entity, number, removed);
                    index.put(pair, clientAgent);
                }
                return clientAgent;
            }
        } else {
            return new ClientAgent(entity, number, removed);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the client agent belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * @param removed whether the client agent has been removed.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull ClientAgent get(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex, boolean removed) throws SQLException {
        return get(entity, resultSet.getLong(columnIndex), removed);
    }
    
}
