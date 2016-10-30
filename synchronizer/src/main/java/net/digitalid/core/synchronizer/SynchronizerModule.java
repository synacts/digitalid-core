package net.digitalid.core.synchronizer;

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

import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.interfaces.Database;
import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.Locked;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.service.CoreService;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concepts.error.ErrorModule;
import net.digitalid.service.core.entity.EntityImplementation;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.packet.Packet;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.storage.ClientModule;
import net.digitalid.service.core.storage.Service;

/**
 * This class provides database access to the client synchronization.
 * 
 * @see Synchronizer
 */
@Stateless
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
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_action (entity " + EntityImplementation.FORMAT + " NOT NULL, service " + Service.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, action " + Block.FORMAT + " NOT NULL, PRIMARY KEY (entity, service, time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Service.REFERENCE + Database.getConfiguration().INDEX("time") + ")");
            Database.getConfiguration().createIndex(statement, site + "synchronization_action", "time");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "synchronization_last (entity " + EntityImplementation.FORMAT + " NOT NULL, service " + Service.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, PRIMARY KEY (entity, service), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Service.REFERENCE + ")");
            Database.onInsertUpdate(statement, site + "synchronization_last", 2, "entity", "service", "time");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotUpdate(statement, "synchronization_last");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "synchronization_action");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "synchronization_last");
        }
    }
    
    
    /**
     * Stores the pending actions of the synchronizer.
     */
    static final @Nonnull BlockingDeque<InternalAction> pendingActions = new LinkedBlockingDeque<>();
    
    /**
     * Loads the pending actions of the given client from the database.
     * 
     * @param client the client whose pending actions are to be loaded.
     */
    @NonCommitting
    public static void load(@Nonnull Client client) throws ExternalException {
        // TODO: If the same client runs in several processes (on different machines), make sure the pending actions and suspended modules are loaded only once.
        final @Nonnull String SQL = "SELECT entity, service, action FROM " + client + "synchronization_action ORDER BY time ASC";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            while (resultSet.next()) {
                final @Nonnull Role role = Role.getNotNull(client, resultSet, 1);
                final @Nonnull Service service = Service.get(resultSet, 2);
                final @Nonnull Block content = SelfcontainedWrapper.decodeNonNullable(Block.getNotNull(Packet.CONTENT, resultSet, 3));
                final @Nonnull SignatureWrapper signature = SignatureWrapper.encodeWithoutSigning(Packet.SIGNATURE, (Block) null, role.getIdentity().getAddress());
                pendingActions.add(Method.get(role, signature, service.getRecipient(role), content).castTo(InternalAction.class));
            }
        }
    }
    
    /**
     * Removes the pending actions of the given role.
     * 
     * @param role the role whose actions are to be removed.
     */
    @NonCommitting
    public static void remove(@Nonnull Role role) throws DatabaseException {
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction action = iterator.next();
            if (action.getRole().equals(role)) { iterator.remove(); }
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
            if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service)) { return true; }
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
    static @Nonnull @Frozen ReadOnlyList<Method> getMethods() {
        final @Nonnull FreezableList<Method> methods = new FreezableLinkedList<>();
        final @Nonnull Set<ReadOnlyPair<Role, Service>> ignored = new HashSet<>();
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction reference = iterator.next();
            final @Nonnull Role role = reference.getRole();
            final @Nonnull Service service = reference.getService();
            final @Nonnull ReadOnlyPair<Role, Service> pair = new FreezablePair<>(role, service).freeze();
            if (!ignored.contains(pair) && Synchronizer.suspend(role, service)) {
                methods.add(reference);
                if (!reference.isSimilarTo(reference)) { return methods.freeze(); }
                while (iterator.hasNext()) {
                    final @Nonnull InternalAction pendingAction = iterator.next();
                    if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service)) {
                        if (pendingAction.isSimilarTo(reference) && reference.isSimilarTo(pendingAction)) { methods.add(pendingAction); }
                        else { return methods.freeze(); }
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
    static void add(@Nonnull InternalAction action) throws DatabaseException {
        final @Nonnull Role role = action.getRole();
        final @Nonnull String TIME = Database.getConfiguration().GREATEST() + "(COALESCE(MAX(time), 0) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        final @Nonnull String SQL = "INSERT INTO " + role.getSite() + "synchronization_action (entity, service, time, action) SELECT ?, ?, " + TIME + ", ? FROM " + role.getSite() + "synchronization_action";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            role.set(preparedStatement, 1);
            action.getService().set(preparedStatement, 2);
            SelfcontainedWrapper.encodeNonNullable(Packet.CONTENT, action).set(preparedStatement, 3);
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
    static void remove(@Nonnull InternalAction action) throws DatabaseException {
        final @Nonnull Role role = action.getRole();
        final @Nonnull String SQL = "DELETE FROM " + role.getSite() + "synchronization_action WHERE time IN (SELECT time FROM " + role.getSite() + "synchronization_action WHERE entity = " + role + " AND service = " + action.getService() + " AND action = ? ORDER BY time LIMIT 1)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            SelfcontainedWrapper.encodeNonNullable(Packet.CONTENT, action).set(preparedStatement, 1);
            final int count = preparedStatement.executeUpdate();
            if (count != 1) { throw new SQLException("Could not find the action to be removed from the pending actions. (The count is " + count + " instead of 1.)"); }
        }
        pendingActions.remove(action);
        synchronized (pendingActions) { pendingActions.notifyAll(); }
    }
    
    /**
     * Removes the given methods from the list of pending actions.
     * 
     * @param methods the methods to be removed from the pending actions.
     * 
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require methods.getNotNull(0).isOnClient() : "The first method is on a client.";
     */
    @NonCommitting
    static void remove(@Nonnull @NonEmpty @NonNullableElements ReadOnlyList<Method> methods) throws DatabaseException {
        Require.that(!methods.isEmpty()).orThrow("The list of methods is not empty.");
        Require.that(!methods.containsNull()).orThrow("The list of methods does not contain null.");
        Require.that(Method.areSimilar(methods)).orThrow("The methods are similar to each other.");
        
        final @Nonnull Role role = methods.getNonNullable(0).getRole();
        final @Nonnull String SQL = "DELETE FROM " + role.getSite() + "synchronization_action WHERE time IN (SELECT time FROM " + role.getSite() + "synchronization_action WHERE entity = " + role + " AND service = " + methods.getNonNullable(0).getService() + " AND action = ? ORDER BY time LIMIT 1)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            for (final @Nonnull Method method : methods) {
                SelfcontainedWrapper.encodeNonNullable(Packet.CONTENT, method).set(preparedStatement, 1);
                preparedStatement.addBatch();
            }
            final int[] counts = preparedStatement.executeBatch();
            for (final int count : counts) if (count != 1) { throw new SQLException("Could not find an action to be removed from the pending actions. (The count is " + count + " instead of 1.)"); }
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
    static void redoPendingActions(@Nonnull Role role, @Nonnull ReadOnlyCollection<StateModule> modules, @Nullable InternalAction lastAction) throws DatabaseException {
        for (final @Nonnull InternalAction pendingAction : pendingActions) {
            if (pendingAction.getRole().equals(role) && modules.contains(pendingAction.getModule())) {
                try {
                    Log.debugging("Reexecute after reloading a module the pending action " + pendingAction + ".");
                    pendingAction.executeOnClient();
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Log.warning("Could not reexecute after reloading a module the pending action " + pendingAction + ".", exception);
                    Database.rollback();
                    ErrorModule.add("Could not reexecute after reloading a module", pendingAction);
                    remove(pendingAction);
                    Database.commit();
                }
            }
            if (pendingAction == lastAction) { break; }
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
    static @Nonnull ReadOnlyList<InternalAction> reverseInterferingActions(@Nonnull Action failedAction) throws DatabaseException {
        final @Nonnull Role role = failedAction.getRole();
        final @Nonnull Service service = failedAction.getService();
        final @Nonnull FreezableList<InternalAction> reversedActions = new FreezableLinkedList<>();
        final @Nonnull Iterator<InternalAction> iterator = pendingActions.descendingIterator();
        while (iterator.hasNext()) {
            final @Nonnull InternalAction pendingAction = iterator.next();
            if (pendingAction.getRole().equals(role) && pendingAction.getService().equals(service) && pendingAction.interferesWith(failedAction)) {
                Log.debugging("Reverse the potentially interfering action " + pendingAction + ".");
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
    static void redoReversedActions(@Nonnull ReadOnlyList<InternalAction> reversedActions) throws DatabaseException {
        for (final @Nonnull InternalAction reversedAction : reversedActions) {
            try {
                Log.debugging("Reexecute the reversed action " + reversedAction + ".");
                reversedAction.executeOnClient();
                Database.commit();
            } catch (@Nonnull SQLException exception) {
                Log.warning("Could not reexecute the reversed action " + reversedAction + ".", exception);
                Database.rollback();
                ErrorModule.add("Could not reexecute after reversion", reversedAction);
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
    @Locked
    @NonCommitting
    public static @Nonnull Time getLastTime(@Nonnull Role role, @Nonnull Service service) throws DatabaseException {
        final @Nonnull String SQL = "SELECT time FROM " + role.getSite() + "synchronization_last WHERE entity = " + role + " AND service = " + service;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return Time.get(resultSet, 1); }
            else { return Time.MIN; }
        }
    }
    
    /**
     * Sets the time of the last audit.
     * 
     * @param role the role of interest.
     * @param service the desired service.
     * @param thisTime the time to be set.
     */
    @Locked
    @NonCommitting
    static void setLastTime(@Nonnull Role role, @Nonnull Service service, @Nonnull Time thisTime) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate(Database.getConfiguration().REPLACE() + " INTO " + role.getSite() + "synchronization_last (entity, service, time) VALUES (" + role + ", " + service + ", " + thisTime + ")");
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
