package ch.virtualid.synchronizer;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.ClientModule;
import ch.virtualid.packet.Packet;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyCollection;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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
    public static void load(@Nonnull Client client) throws SQLException, IOException, PacketException, ExternalException {
        // TODO: If the same client runs in several processes (on different machines), make sure the pending actions and suspended modules are loaded only once.
        final @Nonnull String SQL = "SELECT entity, recipient, action FROM " + client + "synchronization_action ORDER BY time ASC";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            while (resultSet.next()) {
                final @Nonnull Role role = Role.getNotNull(client, resultSet, 1);
                final @Nonnull HostIdentifier recipient = IdentifierClass.get(resultSet, 2).toHostIdentifier();
                final @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(Block.get(Packet.CONTENT, resultSet, 3));
                final @Nonnull SignatureWrapper signature = new SignatureWrapper(Packet.SIGNATURE, (Block) null, role.getIdentity().getAddress());
                final @Nonnull InternalAction action = Method.get(role, signature, recipient, content.getElement()).toInternalAction();
                pendingActions.add(action);
            }
        }
    }
    
    /**
     * Removes the pending actions of the given role.
     * 
     * @param role the role whose actions are to be removed.
     */
    public static void remove(@Nonnull Role role) throws SQLException {
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction action =  iterator.next();
            if (action.getRole().equals(role)) iterator.remove();
        }
    }
    
    
    static void add(@Nonnull InternalAction action) {
        // TODO
        pendingActions.add(action);
    }
    
    static void remove(@Nonnull InternalAction action) {
        // TODO
        pendingActions.remove(action);
    }
    
    static void remove(@Nonnull ReadonlyList<Method> methods) {
        // TODO
        pendingActions.removeAll((FreezableList<Method>) methods);
    }
    
    
    /**
     * Redoes all the pending actions of the given role that operate on one of the given modules until the given last action.
     * 
     * @param role
     * @param modules
     * @param lastAction 
     */
    static void redo(@Nonnull Role role, @Nonnull ReadonlyCollection<BothModule> modules, @Nullable InternalAction lastAction) {
        // TODO: Commit each action individually.
    }
    
    static @Nonnull ReadonlyList<InternalAction> reverseInterferingActions(@Nonnull Action failedAction) throws SQLException {
        final @Nonnull Role role = failedAction.getRole();
        final @Nonnull Service service = failedAction.getService();
        final @Nonnull FreezableList<InternalAction> reversedActions = new FreezableLinkedList<InternalAction>();
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.descendingIterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction pendingAction = iterator.next();
            if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service) && pendingAction.interferesWith(failedAction)) {
                pendingAction.reverseOnClient();
                reversedActions.add(pendingAction);
            }
        }
        return reversedActions.freeze();
    }
    
    static void redoReversedActions(@Nonnull ReadonlyList<InternalAction> reversedActions) throws SQLException {
        for (@Nonnull InternalAction reversedAction : reversedActions) {
            try {
                reversedAction.executeOnClient();
                Database.commit();
            } catch (@Nonnull SQLException e) {
                Database.rollback();
                // TODO: Add the action to the error module.
                remove(reversedAction);
                Database.commit();
            }
        }
    }
    
    
    /**
     * Returns the time of the last request to the given VID.
     * 
     * @param vid the VID of interest.
     * 
     * @return the time of the last request to the given VID.
     */
    static @Nonnull Time getLastTime(@Nonnull Role role, @Nonnull Service service) throws SQLException {
        // TODO
        return natives.get(vid);
    }
    
    static void setLastTime(@Nonnull Role role, @Nonnull Service service, @Nonnull Time thisTime) throws SQLException {
        // TODO
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
