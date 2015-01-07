package ch.virtualid.synchronizer;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.Service;
import ch.virtualid.util.ConcurrentHashSet;
import ch.virtualid.util.ConcurrentSet;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the client synchronization.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Synchronization implements ClientModule {
    
    public static final Synchronization MODULE = new Synchronization();
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_action (entity " + EntityClass.FORMAT + " NOT NULL, service " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, recipient " + IdentifierClass.FORMAT + " NOT NULL, action " + Block.FORMAT + " NOT NULL, PRIMARY KEY (entity, service, time), INDEX(time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_last (entity " + EntityClass.FORMAT + " NOT NULL, service " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, PRIMARY KEY (entity, service), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Mapper.REFERENCE + ")");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "synchronization_action");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "synchronization_last");
        }
    }
    
    
    /**
     * Stores the pending actions of the synchronizer.
     */
    static final @Nonnull BlockingDeque<InternalAction> pendingActions = new LinkedBlockingDeque<InternalAction>();
    
    /**
     * Loads the pending actions of the given client from the database.
     * 
     * @param client the client whose pending actions are to be loaded.
     */
    public static void load(@Nonnull Client client) {
        // TODO: If the same client runs in several processes (on different machines), make sure the pending actions and suspended modules are loaded only once.
        Synchronization.load(client);
        // TODO: Also load the suspended modules from the database.
    }
    
    
    static void suspend(Role role, ReadonlyList<BothModule> modules) {
        throw new UnsupportedOperationException("suspend in Synchronization is not supported yet.");
        @Nullable ConcurrentSet<BothModule> set = suspendedModules.get(role);
        if (set == null) set = Synchronization.suspendedModules.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<BothModule>());
        for (@Nonnull BothModule module : modules) set.add(module);
    }
    
    static void setLastTime(Role role, Service service, Time thisTime) {
        throw new UnsupportedOperationException("setLastTime in Synchronization is not supported yet.");
    }
    
    static void add(InternalAction action) {
        throw new UnsupportedOperationException("queue in Synchronization is not supported yet.");
        pendingActions.add(action);
    }
    
    static void remove(InternalAction action) {
        throw new UnsupportedOperationException("remove in Synchronization is not supported yet.");
        pendingActions.remove(action);
    }
    
    
    
    /**
     * Returns the time of the last request to the given VID.
     * 
     * @param vid the VID of interest.
     * 
     * @return the time of the last request to the given VID.
     */
    public static @Nonnull Time getLastTime(@Nonnull Role role, @Nonnull Service service) throws SQLException {
        // TODO
        return natives.get(vid);
    }
    
    /**
     * Sets the time of the last request to the given VID.
     * 
     * @param identity the VID of the last request.
     * @param time the time of the last request.
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     * @require time > 0 : "The time value is positive.";
     */
    synchronized void setTimeOfLastRequest(@Nonnull Identity identity, long time) throws SQLException {
        assert time > 0 : "The time value is positive.";
        
        if (!isNative(identity)) credentials.put(identity, new HashMap<Long, Map<RandomizedAgentPermissions, Credential>>());
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("REPLACE INTO " + getName() + "_natives (vid, time) VALUES (" + identity + ", " + time + ")");
            Database.commit();
        }
        
        natives.put(identity, time);
    }
    
    public static void store(Role role, Service service, HostIdentifier recipient, Action action) {
        throw new UnsupportedOperationException("store in Synchronization is not supported yet.");
        // TODO: also update the last synchronization time?
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
