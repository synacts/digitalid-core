package ch.virtualid.module.client;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Client;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.Site;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;

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
     * Loads the pending actions of the given client from the database.
     * 
     * @param client the client whose pending actions are to be loaded.
     */
    public static void load(@Nonnull Client client, @Nonnull ConcurrentLinkedQueue<InternalAction> pendingActions) {
        // TODO
    }
    
    /**
     * Returns the time of the last request to the given VID.
     * 
     * @param vid the VID of interest.
     * @return the time of the last request to the given VID.
     * @require isNative(vid) : "This client is accredited at the given VID.";
     */
    public synchronized long getTimeOfLastRequest(long vid) {
        assert isNative(vid) : "This client is accredited at the given VID.";
        
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
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
