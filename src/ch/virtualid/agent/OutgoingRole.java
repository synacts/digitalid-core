package ch.virtualid.agent;

import ch.virtualid.contact.Context;
import ch.virtualid.credential.Credential;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import static ch.virtualid.io.Level.ERROR;
import ch.virtualid.packet.PacketException;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an outgoing role that acts as an agent on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class OutgoingRole extends Agent implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the relation between the issuing and the receiving identity.
     */
    private final @Nonnull SemanticType role;
    
    /**
     * Stores the context to which this outgoing role is assigned.
     */
    private @Nonnull Context context;
    
    
    /**
     * Creates a new outgoing role with the given entity and number.
     * 
     * @param entity the entity to which this outgoing role belongs.
     * @param number the number that references this outgoing role.
     */
    private OutgoingRole(@Nonnull Entity entity, long number) {
        super(entity, number);
    }
    
    /**
     * Creates a new outgoing role agent with the given connection, number, identity, relation and context.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param number the internal number that represents and indexes this agent.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which this outgoing role is assigned.
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @Deprecated
    public OutgoingRole(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, long number, @Nonnull SemanticType relation, @Nonnull Context context) {
        super(connection, identity, number);
        
        assert relation.isRoleType() : "The relation is a role type.";
        
        this.role = relation;
        this.context = context;
    }
    
    /**
     * Creates a new outgoing role with the given connection from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param block the block containing the outgoing role.
     * @ensure !getRestrictions().isClient() : "The restrictions are not for a client.";
     */
    @Deprecated
    public OutgoingRole(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(connection, identity, new TupleWrapper(block).getElementsNotNull(2)[0]);
        
        @Nonnull Block[] tuple = new TupleWrapper(block).getElementsNotNull(2);
        this.role = new NonHostIdentifier(tuple[0]).getIdentity().toSemanticType();
        this.context = new Context(tuple[1]);
        
        try {
            assert !getRestrictions().isClient() : "The restrictions are not for a client.";
        } catch (@Nonnull SQLException exception) {
            throw new ShouldNeverHappenError("There should be no need to load the restrictions from the database.", exception);
        }
    }
    
    
    /**
     * @throws SQLException if the restrictions of this outgoing role are null.
     */
    @Override
    public @Nonnull Restrictions getRestrictions() throws SQLException {
        @Nullable Restrictions restrictions = super.getRestrictions();
        if (restrictions == null) throw new SQLException("The restrictions of a role should never be null.");
        return restrictions;
    }
    
    
    /**
     * Returns the relation between the issuing and the receiving identity.
     * 
     * @return the relation between the issuing and the receiving identity.
     */
    public @Nonnull SemanticType getRelation() {
        return role;
    }
    
    /**
     * Returns the context to which this outgoing role is assigned.
     * 
     * @return the context to which this outgoing role is assigned.
     */
    public @Nonnull Context getContext() {
        return context;
    }
    
    /**
     * Sets the context of this outgoing role and stores it in the database.
     * Make sure to call {@link Agent#redetermineAgents()} afterwards.
     * 
     * @param context the context to be set.
     * 
     * @require !isRestricted() : "This outgoing role may not have been restricted.";
     */
    public void setContext(@Nonnull Context context) throws SQLException {
        assert !isRestricted() : "This outgoing role may not have been restricted.";
        
        Host.setOutgoingRoleContext(getConnection(), this, context);
        this.context = context;
    }
    
    
    @Override
    public boolean isClient() {
        return false;
    }
    
    /**
     * Checks whether this outgoing role covers the given credential and throws a {@link PacketException} if not.
     * 
     * @param credential the credential that needs to be covered.
     * 
     * @require credential.getRestrictions() != null : "The restrictions of the credential is not null.";
     * @require credential.getPermissions() != null : "The permissions of the credential is not null.";
     */
    public void checkCoverage(@Nonnull Credential credential) throws SQLException, PacketException {
        @Nullable Restrictions restrictions = credential.getRestrictions();
        assert restrictions != null : "The restrictions of the credential is not null.";
        @Nullable AgentPermissions permissions = credential.getPermissions();
        assert permissions != null : "The permissions of the credential is not null.";
        
        getRestrictions().checkCoverage(restrictions);
        getPermissions().checkDoesCover(permissions);
    }
    
    /**
     * Restricts this outgoing role to the restrictions and permissions of the given credential.
     * 
     * @param credential the credential with which to restrict this outgoing role.
     * 
     * @require credential.getRestrictions() != null : "The restrictions of the credential is not null.";
     * @require credential.getPermissions() != null : "The permissions of the credential is not null.";
     */
    public void restrictTo(@Nonnull Credential credential) throws SQLException {
        @Nullable Restrictions restrictions = credential.getRestrictions();
        assert restrictions != null : "The restrictions of the credential is not null.";
        @NullAgentPermissionssions permissions = credential.getPermissions();
        assert permissions != null : "The permissions of the credential is not null.";
        
        getRestrictions().restrictTo(restrictions);
        getPermissions().restrictTo(permissions);
        setRestricted();
    }
    
    @Override
    public @Nonnull Block toBlock() {
        try {
            // TODO: The restrictions aren't needed at all!
            assert getRestrictions() != null : "The restrictions is not null.";
            
            @Nonnull Block[] elements = new Block[2];
            elements[0] = role.getAddress().toBlock();
            elements[1] = context.toBlock();
            
            @Nonnull Block[] tuple = new Block[2];
            tuple[0] = super.toBlock();
            tuple[1] = new TupleWrapper(elements).toBlock();
            return new TupleWrapper(tuple).toBlock();
        } catch (@Nonnull SQLException exception) {
            Database.logger.log(ERROR, "Could not load the restrictions of an outgoing role.", exception);
            return Block.EMPTY;
        }
    }
    
}
