package net.digitalid.core.synchronizer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyCollection;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.EntityClass;
import net.digitalid.core.entity.Role;
import net.digitalid.core.entity.Site;
import net.digitalid.core.error.ErrorModule;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.InternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.module.ClientModule;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import static net.digitalid.core.synchronizer.Synchronizer.suspend;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadonlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class provides database access to the client synchronization.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class SynchronizerModule implements ClientModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final SynchronizerModule MODULE = new SynchronizerModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_action (entity " + EntityClass.FORMAT + " NOT NULL, service " + Service.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, action " + Block.FORMAT + " NOT NULL, PRIMARY KEY (entity, service, time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Service.REFERENCE + Database.getConfiguration().INDEX("time") + ")");
            Database.getConfiguration().createIndex(statement, site + "synchronization_action", "Time");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_last (entity " + EntityClass.FORMAT + " NOT NULL, service " + Service.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, PRIMARY KEY (entity, service), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Service.REFERENCE + ")");
            Database.onInsertUpdate(statement, site + "synchronization_last", 2, "entity", "service", "time");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotUpdate(statement, "synchronization_last");
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
    @NonCommitting
    public static void load(@Nonnull Client client) throws SQLException, IOException, PacketException, ExternalException {
        // TODO: If the same client runs in several processes (on different machines), make sure the pending actions and suspended modules are loaded only once.
        final @Nonnull String SQL = "SELECT entity, service, action FROM " + client + "synchronization_action ORDER BY time ASC";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            while (resultSet.next()) {
                final @Nonnull Role role = Role.getNotNull(client, resultSet, 1);
                final @Nonnull Service service = Service.get(resultSet, 2);
                final @Nonnull SelfcontainedWrapper content = new SelfcontainedWrapper(Block.getNotNull(Packet.CONTENT, resultSet, 3));
                final @Nonnull SignatureWrapper signature = new SignatureWrapper(Packet.SIGNATURE, (Block) null, role.getIdentity().getAddress());
                pendingActions.add(Method.get(role, signature, service.getRecipient(role), content.getElement()).toInternalAction());
            }
        }
    }
    
    /**
     * Removes the pending actions of the given role.
     * 
     * @param role the role whose actions are to be removed.
     */
    @NonCommitting
    public static void remove(@Nonnull Role role) throws SQLException {
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction action =  iterator.next();
            if (action.getRole().equals(role)) iterator.remove();
        }
    }
    
    /**
     * Returns whether the pending actions contain an action of the given role and service.
     * 
     * @param role the role of interest.
     * @param service the service of interest.
     * 
     * @return whether the pending actions contain an action of the given role and service.
     */
    @Pure
    private static boolean contains(@Nonnull Role role, @Nonnull Service service) {
        for (final @Nonnull InternalAction pendingAction : pendingActions) {
            if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service)) return true;
        }
        return false;
    }
    
    /**
     * Waits until all actions of the given role and service are completed.
     * 
     * @param role the role whose actions are to be completed.
     * @param service the service whose actions are to be completed.
     */
    public static void wait(@Nonnull Role role, @Nonnull Service service) throws InterruptedException {
        synchronized (pendingActions) { while (contains(role, service)) pendingActions.wait(); }
    }
    
    
    /**
     * Returns a list of similar methods from the pending actions and suspends the corresponding service.
     * 
     * @return a list of similar methods from the pending actions whose service was not suspended.
     */
    static @Nonnull @Frozen ReadonlyList<Method> getMethods() {
        final @Nonnull FreezableList<Method> methods = new FreezableLinkedList<Method>();
        final @Nonnull Set<ReadonlyPair<Role, Service>> ignored = new HashSet<ReadonlyPair<Role, Service>>();
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction reference =  iterator.next();
            final @Nonnull Role role = reference.getRole();
            final @Nonnull Service service = reference.getService();
            final @Nonnull ReadonlyPair<Role, Service> pair = new FreezablePair<Role, Service>(role, service).freeze();
            if (!ignored.contains(pair) && suspend(role, service)) {
                methods.add(reference);
                if (!reference.isSimilarTo(reference)) return methods.freeze();
                while (iterator.hasNext()) {
                    final @Nonnull InternalAction pendingAction =  iterator.next();
                    if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service)) {
                        if (pendingAction.isSimilarTo(reference) && reference.isSimilarTo(pendingAction)) methods.add(pendingAction);
                        else return methods.freeze();
                    }
                }
            } else {
                ignored.add(pair);
            }
        }
        return methods.freeze();
    }
    
    /**
     * Adds the given action to the list of pending actions.
     * 
     * @param action the action to be added to the pending actions.
     * 
     * @require action.isOnClient() : "The internal action is on a client.";
     */
    @NonCommitting
    static void add(@Nonnull InternalAction action) throws SQLException {
        final @Nonnull Role role = action.getRole();
        final @Nonnull String TIME = Database.getConfiguration().GREATEST() + "(COALESCE(MAX(time), 0) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        final @Nonnull String SQL = "INSERT INTO " + role.getSite() + "synchronization_action (entity, service, time, action) SELECT ?, ?, " + TIME + ", ? FROM " + role.getSite() + "synchronization_action";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            role.set(preparedStatement, 1);
            action.getService().set(preparedStatement, 2);
            new SelfcontainedWrapper(Packet.CONTENT, action).toBlock().set(preparedStatement, 3);
            preparedStatement.executeUpdate();
        }
        pendingActions.add(action);
    }
    
    /**
     * Removes the given action from the list of pending actions.
     * 
     * @param action the action to be removed from the pending actions.
     * 
     * @require action.isOnClient() : "The internal action is on a client.";
     */
    @NonCommitting
    static void remove(@Nonnull InternalAction action) throws SQLException {
        final @Nonnull Role role = action.getRole();
        final @Nonnull String SQL = "DELETE FROM " + role.getSite() + "synchronization_action WHERE entity = " + role + " AND service = " + action.getService() + " AND action = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            new SelfcontainedWrapper(Packet.CONTENT, action).toBlock().set(preparedStatement, 1);
            if (preparedStatement.executeUpdate() != 1) throw new SQLException("Could not find the action to be removed from the pending actions.");
        }
        pendingActions.remove(action);
        synchronized (pendingActions) { pendingActions.notifyAll(); }
    }
    
    /**
     * Removes the given methods from the list of pending actions.
     * 
     * @param methods the methods to be removed from the pending actions.
     * 
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require methods.getNotNull(0).isOnClient() : "The first method is on a client.";
     */
    @NonCommitting
    static void remove(@Nonnull ReadonlyList<Method> methods) throws SQLException {
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert methods.doesNotContainNull() : "The list of methods does not contain null.";
        assert Method.areSimilar(methods) : "The methods are similar to each other.";
        
        final @Nonnull Role role = methods.getNotNull(0).getRole();
        final @Nonnull String SQL = "DELETE FROM " + role.getSite() + "synchronization_action WHERE entity = " + role + " AND service = " + methods.getNotNull(0).getService() + " AND action = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            for (final @Nonnull Method method : methods) {
                new SelfcontainedWrapper(Packet.CONTENT, method).toBlock().set(preparedStatement, 1);
                preparedStatement.addBatch();
            }
            final int[] counts = preparedStatement.executeBatch();
            for (final int count : counts) if (count != 1) throw new SQLException("Could not find an action to be removed from the pending actions.");
        }
        for (final @Nonnull Method method : methods) pendingActions.remove(method); // The pending actions may contain duplicates which may not be removed.
        synchronized (pendingActions) { pendingActions.notifyAll(); }
    }
    
    
    /**
     * Redoes all the pending actions of the given role that operate on one of the given modules until the given last action.
     * 
     * @param role the role whose pending actions are to be redone.
     * @param modules the modules which were reloaded and need to be redone.
     * @param lastAction the last action that might have been affected by the reload.
     */
    @Committing
    static void redoPendingActions(@Nonnull Role role, @Nonnull ReadonlyCollection<BothModule> modules, @Nullable InternalAction lastAction) throws SQLException {
        for (final @Nonnull InternalAction action : pendingActions) {
            if (action.getRole().equals(role) && modules.contains(action.getModule())) {
                action.executeOnClient();
                Database.commit();
            }
            if (action == lastAction) break;
        }
    }
    
    /**
     * Reverses the pending actions that interfere with the given failed action.
     * 
     * @param failedAction the action that failed because local actions interfered with it.
     * 
     * @return a list of all the actions that interfere with the given failed action and were thus reversed.
     */
    @NonCommitting
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
    
    /**
     * Redoes each of the given actions, which were reversed.
     * 
     * @param reversedActions the actions that were reversed.
     */
    @Committing
    static void redoReversedActions(@Nonnull ReadonlyList<InternalAction> reversedActions) throws SQLException {
        for (@Nonnull InternalAction reversedAction : reversedActions) {
            try {
                reversedAction.executeOnClient();
                Database.commit();
            } catch (@Nonnull SQLException e) {
                Database.rollback();
                ErrorModule.add("Could not redo", reversedAction);
                remove(reversedAction);
                Database.commit();
            }
        }
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @param role the role of interest.
     * @param service the service of interest.
     * 
     * @return the time of the last audit.
     */
    @Pure
    @NonCommitting
    static @Nonnull Time getLastTime(@Nonnull Role role, @Nonnull Service service) throws SQLException {
        final @Nonnull String SQL = "SELECT time FROM " + role.getSite() + "synchronization_last WHERE entity = " + role + " AND service = " + service;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Time.get(resultSet, 1);
            else return Time.MIN;
        }
    }
    
    /**
     * Sets the time of the last audit.
     * 
     * @param role the role of interest.
     * @param service the desired service.
     * @param thisTime the time to be set.
     */
    @NonCommitting
    static void setLastTime(@Nonnull Role role, @Nonnull Service service, @Nonnull Time thisTime) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate(Database.getConfiguration().REPLACE() + " INTO " + role.getSite() + "synchronization_last (entity, service, time) VALUES (" + role + ", " + service + ", " + thisTime + ")");
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
