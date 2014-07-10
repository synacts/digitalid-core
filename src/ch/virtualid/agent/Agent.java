package ch.virtualid.agent;

import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models an agent that acts on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public abstract class Agent extends Authorization {
    
    /**
     * Creates a new agent with the given connection, identity and number.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param number the internal number that represents and indexes this agent.
     */
    protected Agent(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, long number) {
        super(connection, identity, number);
    }
    
    /**
     * Creates a new agent with the given connection and identity from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param block the block containing the agent.
     */
    protected Agent(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(connection, identity, new TupleWrapper(block).getElementsNotNull(2)[0]);
    }
    
    /**
     * Creates a new agent with the given connection and identity from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param block the block containing the agent.
     * @return the new agent with the given connection and identity from the given block.
     */
    public final @Nonnull Agent create(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        @Nonnull Block[] tuple = new TupleWrapper(block).getElementsNotNull(2);
        if (new BooleanWrapper(tuple[1]).getValue()) return new ClientAgent(connection, identity, block);
        else return new OutgoingRole(connection, identity, block);
    }
    
    
    /**
     * Redetermines which agents are stronger and weaker than this agent.
     */
    public final void redetermineAgents() throws SQLException {
        Host.redetermineAgents(getConnection(), this);
    }
    
    /**
     * Removes this agent from the database.
     * Make sure to call {@link #redetermineAgents()} afterwards.
     */
    @Override
    public final void remove() throws SQLException {
        Host.removeAgent(getConnection(), this);
    }
    
    @Override
    public @Nonnull Block toBlock() {
        @Nonnull Block[] tuple = new Block[2];
        tuple[0] = super.toBlock();
        tuple[1] = new BooleanWrapper(this instanceof ClientAgent).toBlock();
        return new TupleWrapper(tuple).toBlock();
    }
    
}
