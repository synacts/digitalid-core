package ch.virtualid.agent;

import ch.virtualid.client.Commitment;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import static ch.virtualid.io.Level.ERROR;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a client that acts as an agent on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class ClientAgent extends Agent implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the commitment of this client.
     */
    private @Nonnull Commitment commitment;
    
    /**
     * Stores the name of this client.
     */
    private @Nonnull String name;
    
    
    /**
     * Creates a new client agent with the given entity and number.
     * 
     * @param entity the entity to which this client agent belongs.
     * @param number the number that references this client agent.
     */
    private ClientAgent(@Nonnull Entity entity, long number) {
        super(entity, number);
    }
    
    /**
     * Creates a new client agent with the given connection, identity, number, commitment and name.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param number the internal number that represents and indexes this agent.
     * @param commitment the commitment of this client.
     * @param name the name of this client.
     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
     */
    @Deprecated
    public ClientAgent(@Nonnull NonHostIdentity identity, long number, @Nonnull Commitment commitment, @Nonnull String name) {
        super(connection, identity, number);
        
        assert name.length() <= 50 : "The client name may have at most 50 characters.";
        
        this.commitment = commitment;
        this.name = name;
    }
    
    /**
     * Creates a new client agent with the given connection from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param block the block containing the client agent.
     * @ensure getRestrictions() == null || getRestrictions().isClient() : "The restrictions are either null or for a client.";
     */
    @Deprecated
    protected ClientAgent(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(connection, identity, new TupleWrapper(block).getElementsNotNull(2)[0]);
        
        @Nonnull Block[] tuple = new TupleWrapper(block).getElementsNotNull(3);
        commitment = new Commitment(tuple[0]);
        name = new StringWrapper(tuple[1]).getString();
        preferences = new AgentPermissions(tuple[2]);
        
        if (name.length() > 50) throw new InvalidEncodingException("The client name may have at most 50 characters.");
        
        try {
            @Nullable Restrictions restrictions = getRestrictions();
            assert restrictions == null || restrictions.isClient() : "The restrictions are either null or for a client.";
        } catch (@Nonnull SQLException exception) {
            throw new ShouldNeverHappenError("There should be no need to load the restrictions from the database.", exception);
        }
    }
    
    
    /**
     * Returns the commitment of this client.
     * 
     * @return the commitment of this client.
     */
    public @Nonnull Commitment getCommitment() {
        return commitment;
    }
    
    /**
     * Sets the commitment of this client and stores it in the database.
     * 
     * @param commitment the commitment to be set.
     */
    public void setCommitment(@Nonnull Commitment commitment) throws SQLException {
        Host.setClientCommitment(getConnection(), this, commitment);
        this.commitment = commitment;
    }
    
    /**
     * Returns the name of this client.
     * 
     * @return the name of this client.
     */
    public @Nonnull String getName() {
        return name;
    }
    
    /**
     * Sets the name of this client and stores it in the database.
     * 
     * @param name the name to be set.
     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
     */
    public void setName(@Nonnull String name) throws SQLException {
        assert name.length() <= 50 : "The client name may have at most 50 characters.";
        
        Host.setClientName(getConnection(), this, name);
        this.name = name;
    }
    
    /**
     * Returns the preferences of this client.
     * 
     * @return the preferences of this client.
     */
    public @NonAgentPermissionssions getPreferences() throws SQLException {
        if (preferences == null) {
            preferences = Host.getPermissions(getConnection(), this, true);
        }
        return preferences;
    }
    
    /**
     * Sets the preferences of this client and stores them in the database.
     * 
     * @param preferences the preferences to be set.
     */
    public void setPreferencesAgentPermissionsermissions preferences) throws SQLException {
        Host.setPermissions(getConnection(), this, true, preferences);
        this.preferences = preferences;
    }
    
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    
    @Override
    public @Nonnull Block toBlock() {
        try {
            @Nonnull Block[] elements = new Block[3];
            elements[0] = commitment.toBlock();
            elements[1] = new StringWrapper(name).toBlock();
            elements[2] = getPreferences().toBlock();
            
            @Nonnull Block[] tuple = new Block[2];
            tuple[0] = super.toBlock();
            tuple[1] = new TupleWrapper(elements).toBlock();
            return new TupleWrapper(tuple).toBlock();
        } catch (@Nonnull SQLException exception) {
            Database.logger.log(ERROR, "Could not load the preferences of a client agent.", exception);
            return Block.EMPTY;
        }
    }
    
}
