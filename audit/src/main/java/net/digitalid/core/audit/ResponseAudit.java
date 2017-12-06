/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.audit;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.pack.Pack;

/**
 * This class models a response audit with the trail and the times of the last and this audit.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // TODO: The ReadOnlyList cannot yet be converted.
public abstract class ResponseAudit extends Audit {
    
    /* -------------------------------------------------- This Time -------------------------------------------------- */
    
    /**
     * Returns the time of this audit.
     */
    @Pure
    public abstract @Nonnull Time getThisTime();
    
    /* -------------------------------------------------- Trail -------------------------------------------------- */
    
    /**
     * Returns the trail of this audit.
     * 
     * @ensure for (Block block : return) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the returned trail is based on the packet signature type.";
     */
    @Pure
    @TODO(task = "Find a way to return the signatures without having to decode them first. (The generic type was Block before.)", date = "2016-11-09", author = Author.KASPAR_ETTER)
    public abstract @Nonnull @Frozen @NonNullableElements ReadOnlyList<Pack> getTrail();
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    // TODO: Think about where to move the following code.
    
//    /**
//     * Stores an empty set of modules.
//     */
//    static final @Nonnull ReadOnlySet<StateModule> emptyModuleSet = FreezableHashSet.<StateModule>get().freeze();
//    
//    /**
//     * Stores an empty list of methods.
//     */
//    static final @Nonnull ReadOnlyList<Method> emptyMethodList = FreezableLinkedList.<Method>get().freeze();
//    
//    /**
//     * Executes the trail of this audit.
//     * 
//     * @param role the role for which the trail is to be executed.
//     * @param service the service whose trail is to be executed.
//     * @param recipient the recipient of the actions in the trail.
//     * @param methods the methods that were sent with the audit request.
//     * @param ignoredModules the modules that are ignored when executing the trail.
//     */
//    @Committing
//    void execute(@Nonnull Role role, @Nonnull Service service, @Nonnull HostIdentifier recipient, @Nonnull ReadOnlyList<Method> methods, @Nonnull ReadOnlySet<StateModule> ignoredModules) throws ExternalException {
//        final @Nonnull FreezableSet<StateModule> suspendedModules = FreezableHashSet.get();
//        for (final @Nonnull Block block : trail) {
//            final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, role);
//            final @Nonnull Block element = SelfcontainedWrapper.decodeNonNullable(CompressionWrapper.decompressNonNullable(signature.getNonNullableElement()));
//            final @Nonnull Action action = Method.get(role, signature, recipient, element).castTo(Action.class);
//            Database.commit();
//            
//            final @Nonnull ReadOnlyList<StateModule> suspendModules = action.suspendModules();
//            if (!suspendModules.isEmpty()) {
//                suspendedModules.addAll((FreezableList<StateModule>) suspendModules);
//            }
//            
//            final @Nonnull StateModule module = action.getModule();
//            if (!suspendedModules.contains(module) && !ignoredModules.contains(module) && !methods.contains(action)) {
//                try {
//                    Log.debugging("Execute on the client the audited action " + action + ".");
//                    action.executeOnClient();
//                    ActionModule.audit(action);
//                    Database.commit();
//                } catch (@Nonnull SQLException exception) {
//                    Log.warning("Could not execute on the client the audited action " + action + ".", exception);
//                    Database.rollback();
//                    
//                    try {
//                        final @Nonnull ReadOnlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
//                        Log.debugging("Execute on the client after having reversed the interfering actions the audited action " + action + ".");
//                        action.executeOnClient();
//                        ActionModule.audit(action);
//                        Database.commit();
//                        SynchronizerModule.redoReversedActions(reversedActions);
//                    } catch (@Nonnull SQLException e) {
//                        Log.warning("Could not execute on the client after having reversed the interfering actions the audited action " + action + ".", e);
//                        suspendedModules.add(module);
//                        Database.rollback();
//                    }
//                }
//            } else {
//                try {
//                    Log.debugging("Add to the audit trail the ignored or already executed action " + action + ".");
//                    ActionModule.audit(action);
//                    Database.commit();
//                } catch (@Nonnull SQLException exception) {
//                    Log.warning("Could not add to the audit trail the ignored or already executed action " + action + ".", exception);
//                    Database.rollback();
//                }
//            }
//        }
//        
//        SynchronizerModule.setLastTime(role, service, thisTime);
//        Database.commit();
//        
//        suspendedModules.removeAll((FreezableSet<StateModule>) ignoredModules);
//        if (!suspendedModules.freeze().isEmpty()) {
//            final @Nonnull FreezableList<Method> queries = FreezableArrayList.getWithCapacity(suspendedModules.size());
//            for (final @Nonnull StateModule module : suspendedModules) { queries.add(new StateQuery(role, module)); }
//            final @Nonnull Response response = Method.send(queries.freeze(), new RequestAudit(SynchronizerModule.getLastTime(role, service)));
//            for (int i = 0; i < response.getSize(); i++) {
//                final @Nonnull StateReply reply = response.getReplyNotNull(i);
//                reply.updateState();
//            }
//            final @Nullable InternalAction lastAction = SynchronizerModule.pendingActions.peekLast();
//            Database.commit();
//            SynchronizerModule.redoPendingActions(role, suspendedModules, lastAction);
//            response.getAuditNotNull().execute(role, service, recipient, emptyMethodList, suspendedModules);
//        }
//    }
//    
//    /**
//     * The thread pool executor executes the audit asynchronously.
//     */
//    private static final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8), new NamedThreadFactory("Audit"), new ThreadPoolExecutor.CallerRunsPolicy());
//    
//    /**
//     * Shuts down the response audit executor after having finished the current audits.
//     */
//    public static void shutDown() {
//        try {
//            Log.verbose("Shutting down the response audit executor.");
//            threadPoolExecutor.shutdown();
//            threadPoolExecutor.awaitTermination(1L, TimeUnit.MINUTES);
//        } catch (@Nonnull InterruptedException exception) {
//            Log.warning("Could not shut down the response audit executor.", exception);
//        }
//    }
//    
//    /**
//     * Executes the trail of this audit asynchronously.
//     * 
//     * @param method the method that was sent.
//     */
//    public void executeAsynchronously(final @Nonnull Method method) {
//        threadPoolExecutor.execute(new Runnable() {
//            @Override
//            @Committing
//            public void run() {
//                final @Nonnull Role role = method.getRole();
//                final @Nonnull Service service = method.getService();
//                
//                try {
//                    Database.lock();
//                    Log.debugging("Execute asynchronously the audit of " + method + ".");
//                    execute(role, service, method.getRecipient(), FreezableArrayList.get(method).freeze(), emptyModuleSet);
//                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                    Log.warning("Could not execute the audit of " + method + " asynchronously.", exception);
//                    Database.rollback();
//                } finally {
//                    Database.unlock();
//                }
//                
//                Synchronizer.resume(role, service);
//            }
//        });
//    }
    
    // TODO: The following code was copied from the worker class.
    
//                    final @Nullable ResponseAudit responseAudit;
//                    if (requestAudit != null) {
//                        final @Nonnull Time auditStart = Time.getCurrent();
//                        if (!(reference instanceof InternalMethod)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "An audit may only be requested by internal methods."); }
//                        final @Nullable ReadOnlyAgentPermissions permissions;
//                        @Nullable Restrictions restrictions;
//                        if (service.equals(CoreService.SERVICE)) {
//                            Require.that(agent != null).orThrow("See above.");
//                            permissions = agent.getPermissions();
//                            try {
//                                restrictions = agent.getRestrictions();
//                            } catch (@Nonnull SQLException exception) {
//                                restrictions = Restrictions.MIN;
//                            }
//                        } else {
//                            final @Nonnull Credential credential = signature.toCredentialsSignatureWrapper().getCredentials().getNonNullable(0);
//                            permissions = credential.getPermissions();
//                            restrictions = credential.getRestrictions();
//                            if (permissions == null || restrictions == null) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "If an audit is requested, neither the permissions nor the restrictions may be null."); }
//                        }
//                        responseAudit = ActionModule.getAudit(reference.getNonHostAccount(), service, requestAudit.getLastTime(), permissions, restrictions, agent);
//                        Database.commit();
//                        final @Nonnull Time auditEnd = Time.getCurrent();
//                        Log.debugging("Audit retrieved in " + auditEnd.subtract(auditStart).getValue() + " ms.");
//                    } else {
//                        responseAudit = null;
//                    }
}
