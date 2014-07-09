package ch.virtualid.module;

import ch.virtualid.authorization.Agent;
import ch.virtualid.concept.Entity;
import ch.virtualid.database.ClientEntity;
import ch.virtualid.database.Entity;
import ch.virtualid.database.Database;
import ch.virtualid.database.HostEntity;
import ch.virtualid.handler.Handler;
import ch.virtualid.handler.ServiceException;
import ch.virtualid.handler.action.external.AccessRequest;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.client.Requests;
import ch.virtualid.module.client.Roles;
import ch.virtualid.module.client.Synchronization;
import ch.virtualid.module.host.Actions;
import ch.virtualid.module.host.Credentials;
import ch.virtualid.module.host.Tokens;
import ch.virtualid.server.Service;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class initializes all modules of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class CoreService extends Service {
    
    private static final int NUMBER = 6;
    
    /**
     * Stores the modules that represent a part of an entity's state in the specified order.
     */
    private static final @Nonnull List<Module> modules = new ArrayList<Module>(NUMBER);
    
    /**
     * Initializes the core service by initializing the database and registering the handlers.
     */
    public CoreService() throws ServiceException {
        super("Core Service", "1.0");
        
        try (@Nonnull HostEntity connection = Database.getHostConnection()) {
            initialize(connection);
            connection.commit();
        } catch (@Nonnull SQLException exception) {
            throw new ServiceException("Could not create the database tables of the core service.", exception);
        }
    }
    
    /**
     * Initializes the database by creating the tables of the core service if necessary and registers the handlers.
     * Please note that the connection is not committed within this method!
     * 
     * @param connection an open client or host connection to the database.
     */
    public static void initialize(@Nonnull Entity connection) throws SQLException, ServiceException {
        Mapper.initialize();
        
        // TODO: The table initialization has to happen per host and per client, the modules however stay the same.
        
        // Do not change the order of the modules!
        modules.add(new Passwords(connection));
        modules.add(new Attributes(connection));
        modules.add(new Contexts(connection));
        modules.add(new Contacts(connection));
        modules.add(new Authorizations(connection));
        modules.add(new Certificates(connection));
        
        if (connection instanceof ClientEntity) {
            // Modules used on the client.
            @Nonnull ClientEntity clientConnection = (ClientEntity) connection;
            Requests.initialize(clientConnection);
            Roles.initialize(clientConnection);
            Synchronization.initialize(clientConnection);
        }
        
        if (connection instanceof HostEntity) {
            // Modules used on the host.
            @Nonnull HostEntity hostConnection = (HostEntity) connection;
            Actions.initialize(hostConnection);
            Credentials.initialize(hostConnection);
            Tokens.initialize(hostConnection);
        }
        
        // TODO: Add the handlers.
        Handler.add(AccessRequest.class);
    }
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param connection an open client or host connection to the database.
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    public static @Nonnull Block getAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        @Nonnull Block[] blocks = new Block[NUMBER];
        for (int i = 0; i < NUMBER; i++) blocks[i] = modules.get(i).getAll(connection, entity, agent);
        return new TupleWrapper(blocks).toBlock();
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param connection an open client or host connection to the database.
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    public static void addAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        @Nonnull Block[] blocks = new TupleWrapper(block).getElementsNotNull(NUMBER);
        for (int i = 0; i < NUMBER; i++) modules.get(i).addAll(connection, entity, blocks[i]);
    }
    
    /**
     * Removes all the entries of the given entity in all the modules.
     * On the client, you can simply remove the role from {@link Roles}.
     * 
     * @param connection an open host connection to the database.
     * @param entity the entity whose entries are to be removed.
     * @require Database.isHost() : "This method should only be called on hosts.";
     */
    public static void removeAll(@Nonnull HostEntity connection, @Nonnull Entity entity) throws SQLException {
        for (@Nonnull Module module : modules) module.removeAll(connection, entity);
    }
    
}
