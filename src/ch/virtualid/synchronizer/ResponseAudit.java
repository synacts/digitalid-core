package ch.virtualid.synchronizer;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.InternalMethod;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Service;
import ch.virtualid.module.both.Actions;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.Response;
import ch.virtualid.util.ConcurrentHashSet;
import ch.virtualid.util.ConcurrentSet;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.CompressionWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a response audit with the trail and the times of the last and this audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ResponseAudit extends Audit implements Immutable, Blockable {
    
    /**
     * Stores the time of this audit.
     */
    private final @Nonnull Time thisTime;
    
    /**
     * Stores the trail of this audit.
     * 
     * @invariant trail.isFrozen() : "The trail is frozen.";
     * @invariant trail.doesNotContainNull() : "The trail does not contain null.";
     * @invariant for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    private final @Nonnull ReadonlyList<Block> trail;
    
    /**
     * Creates a new audit with the given times and trail.
     * 
     * @param lastTime the time of the last audit.
     * @param thisTime the time of this audit.
     * @param trail the trail of this audit.
     * 
     * @require trail.isFrozen() : "The trail is frozen.";
     * @require trail.doesNotContainNull() : "The trail does not contain null.";
     * @require for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    public ResponseAudit(@Nonnull Time lastTime, @Nonnull Time thisTime, @Nonnull ReadonlyList<Block> trail) {
        super(lastTime);
        
        assert trail.isFrozen() : "The trail is frozen.";
        assert trail.doesNotContainNull() : "The trail does not contain null.";
        for (final @Nonnull Block block : trail) assert block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
        
        this.thisTime = thisTime;
        this.trail = trail;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, getLastTime().toBlock().setType(Audit.LAST_TIME));
        elements.set(1, thisTime.toBlock().setType(Audit.THIS_TIME));
        elements.set(2, new ListWrapper(Audit.TRAIL, trail).toBlock());
        return new TupleWrapper(Audit.TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the time of this audit.
     * 
     * @return the time of this audit.
     */
    @Pure
    public @Nonnull Time getThisTime() {
        return thisTime;
    }
    
    /**
     * Returns the trail of this audit.
     * 
     * @return the trail of this audit.
     * 
     * @ensure trail.isFrozen() : "The trail is frozen.";
     * @ensure trail.doesNotContainNull() : "The trail does not contain null.";
     * @ensure for (Block block : return) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the returned trail is based on the packet signature type.";
     */
    @Pure
    public @Nonnull ReadonlyList<Block> getTrail() {
        return trail;
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Response audit from " + getLastTime().asDate() + " to " + thisTime.asDate() + " with " + trail.size() + " actions";
    }
    
    
    /**
     * Executes the trail of this audit.
     * 
     * @param method the method that was sent.
     */
    public void execute(@Nonnull Method method) throws SQLException, IOException, PacketException, ExternalException {
        if (method.isOnClient() && method instanceof InternalMethod && method.isSimilarTo(method)) {
            final @Nonnull Role role = method.getRole();
            final @Nonnull Agent agent = role.getAgent();
            final @Nonnull Service service = method.getService();
            final @Nonnull HostIdentifier recipient = method.getRecipient();
            @Nullable ConcurrentSet<BothModule> suspendedModules = Synchronization.suspendedModules.get(role);
            if (suspendedModules == null) suspendedModules = Synchronization.suspendedModules.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<BothModule>());
            
            for (@Nonnull Block block : trail) {
                final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, role);
                final @Nonnull CompressionWrapper compression = new CompressionWrapper(signature.getElementNotNull());
                final @Nonnull Block element = new SelfcontainedWrapper(compression.getElementNotNull()).getElement();
                final @Nonnull Action action = Method.get(role, signature, recipient, element).toAction();
                final @Nonnull BothModule module = action.getModule();
                
                if (!suspendedModules.contains(module)) {
                    final @Nonnull ReadonlyList<BothModule> suspendModules = action.suspendModules();
                    if (suspendModules.isNotEmpty()) {
                        Synchronization.suspend(role, suspendModules);
                    } else {
                        if (!agent.equals(signature.getAgent(role))) {
                            try {
                                action.executeOnClient();
                                Actions.audit(action);
                                Database.commit();
                            } catch (@Nonnull SQLException exception) {
                                Database.rollback();
                                if (action.isSimilarTo(action)) {
                                    final @Nonnull List<InternalAction> pendingActions = new LinkedList<InternalAction>();
                                    final @Nonnull Iterator<InternalAction> iterator = Synchronization.pendingActions.descendingIterator();
                                    while (iterator.hasNext()) {
                                        final @Nonnull InternalAction pendingAction = iterator.next();
                                        if (pendingAction.getModule().equals(module)) {
                                            pendingAction.reverseOnClient();
                                            pendingActions.add(pendingAction);
                                        }
                                    }
                                    
                                    try {
                                        action.executeOnClient();
                                        Actions.audit(action);
                                        Database.commit();
                                    } catch (@Nonnull SQLException e) {
                                        Database.rollback();
                                        Synchronization.suspend(role, new FreezableArrayList<BothModule>(module).freeze());
                                        break;
                                    }
                                    
                                    for (@Nonnull InternalAction pendingAction : pendingActions) {
                                        try {
                                            pendingAction.executeOnClient();
                                            Database.commit();
                                        } catch (@Nonnull SQLException e) {
                                            Database.rollback();
                                            // TODO: Add the action to the error module.
                                            Synchronization.remove(pendingAction);
                                            Database.commit();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Synchronization.setLastTime(role, service, thisTime);
            
            if (suspendedModules.isNotEmpty()) {
                final @Nonnull FreezableList<Method> methods = new FreezableArrayList<Method>(suspendedModules.size());
                for (@Nonnull BothModule module : suspendedModules) methods.add(module.getInternalQuery(role));
                final @Nonnull Response response = Method.send(methods.freeze(), RequestAudit.get(methods.getNotNull(0)));
                // TODO: Be able to handle the replies automatically.
                // TODO: Resume the suspended modules.
            }
        } else {
            throw new InvalidEncodingException("No audit trail should be appended.");
        }
    }
    
    /**
     * Executes the trail of this audit asynchronously.
     * 
     * @param method the method that was sent.
     */
    public void executeAsynchronously(final @Nonnull Method method) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    execute(method);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    Synchronizer.LOGGER.log(Level.WARNING, exception);
                }
            }
        }).start();
    }
    
}
