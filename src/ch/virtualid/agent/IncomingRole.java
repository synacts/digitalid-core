package ch.virtualid.agent;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.database.Database;
import ch.virtualid.exception.ShouldNeverHappenError;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import static ch.virtualid.io.Level.ERROR;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an incoming role that acts as an agent on behalf of a virtual identity.
 * 
 * TODO: Delete this class?! -> Rather just no longer inherit from Authorization and model "incoming_role (entity, issuer, relation)" instead.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
public final class IncomingRole extends Authorization {
    
    /**
     * Stores the issuer of this incoming role.
     */
    private final @Nonnull NonHostIdentity issuer;
    
    /**
     * Stores the relation of this incoming role.
     */
    private final @Nonnull SemanticType relation;
    
    
    /**
     * Creates a new incoming role agent with the given connection, number, identity, issuer and relation.
     * 
     * @param connection an open connection to the database.
     * @param identity the receiver of this incoming role.
     * @param number the internal number that represents and indexes this incoming role.
     * @param issuer the issuer of this incoming role.
     * @param relation the relation of this incoming role.
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public IncomingRole(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, long number, @Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation) {
        super(connection, identity, number);
        
        assert relation.isRoleType() : "The relation is a role type.";
        
        this.issuer = issuer;
        this.relation = relation;
    }
    
    /**
     * Creates a new incoming role with the given connection from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the receiver of this incoming role.
     * @param block the block containing the incoming role.
     * @ensure !getRestrictions().isClient() : "The restrictions are not for a client.";
     */
    public IncomingRole(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(connection, identity, new TupleWrapper(block).getElementsNotNull(2)[0]);
        
        @Nonnull Block[] tuple = new TupleWrapper(block).getElementsNotNull(2);
        this.issuer = new NonHostIdentifier(tuple[0]).getIdentity();
        this.relation = new NonHostIdentifier(tuple[1]).getIdentity().toSemanticType();
        
        try {
            assert !getRestrictions().isClient() : "The restrictions are not for a client.";
        } catch (@Nonnull SQLException exception) {
            throw new ShouldNeverHappenError("There should be no need to load the restrictions from the database.", exception);
        }
    }
    
    
    /**
     * @throws SQLException if the restrictions of this incoming role are null.
     */
    @Override
    public @Nonnull Restrictions getRestrictions() throws SQLException {
        @Nullable Restrictions restrictions = super.getRestrictions();
        if (restrictions == null) throw new SQLException("The restrictions of a role should never be null.");
        return restrictions;
    }
    
    
    /**
     * Returns the issuer of this incoming role.
     * 
     * @return the issuer of this incoming role.
     */
    public @Nonnull NonHostIdentity getIssuer() {
        return issuer;
    }
    
    /**
     * Returns the relation of this incoming role.
     * 
     * @return the relation of this incoming role.
     */
    public @Nonnull SemanticType getRelation() {
        return relation;
    }
    
    
    @Override
    public void remove() throws SQLException {
        assert !isRestricted() : "This authorization may not have been restricted.";
        
        Host.removeIncomingRole(getConnection(), this);
    }
    
    @Override
    public boolean isClient() {
        return false;
    }
    
    /**
     * Restricts this incoming role to the restrictions and permissions of the given agent.
     * 
     * @param agent the agent with which to restrict this incoming role.
     * 
     * @require agent.getRestrictions() != null : "The restrictions of the agent is not null.";
     */
    public void restrictTo(@Nonnull Agent agent) throws SQLException {
        @Nullable Restrictions restrictions = agent.getRestrictions();
        assert restrictions != null : "The restrictions of the agent is not null.";
        
        getRestrictions().restrictTo(restrictions);
        getPermissions().restrictTo(agent.getPermissions());
        setRestricted();
    }
    
    @Override
    public @Nonnull Block toBlock() {
        try {
            // TODO: The restrictions aren't needed at all!
            assert getRestrictions() != null : "The restrictions is not null.";
            
            @Nonnull Block[] elements = new Block[2];
            elements[0] = issuer.getAddress().toBlock();
            elements[1] = relation.getAddress().toBlock();
            
            @Nonnull Block[] tuple = new Block[2];
            tuple[0] = super.toBlock();
            tuple[1] = new TupleWrapper(elements).toBlock();
            return new TupleWrapper(tuple).toBlock();
        } catch (@Nonnull SQLException exception) {
            Database.logger.log(ERROR, "Could not load the restrictions of an incoming role.", exception);
            return Block.EMPTY;
        }
    }
    
}
