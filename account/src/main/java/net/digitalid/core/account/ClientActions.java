// TODO: Do. (Parts of the code make NetBeans crash.)

//package net.digitalid.core.account;
//
//import java.util.Random;
//
//import javax.annotation.Nonnull;
//
//import net.digitalid.utility.collections.list.FreezableArrayList;
//import net.digitalid.utility.collections.list.FreezableLinkedList;
//import net.digitalid.utility.collections.list.FreezableList;
//import net.digitalid.utility.collections.list.ReadOnlyList;
//import net.digitalid.utility.contracts.Require;
//import net.digitalid.utility.exceptions.InternalException;
//import net.digitalid.utility.logging.exceptions.ExternalException;
//import net.digitalid.utility.tuples.Pair;
//
//import net.digitalid.database.annotations.transaction.Committing;
//import net.digitalid.database.exceptions.DatabaseException;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.client.role.NativeRole;
//import net.digitalid.core.clientagent.ClientAgent;
//import net.digitalid.core.exceptions.request.RequestException;
//import net.digitalid.core.identification.Category;
//import net.digitalid.core.identification.identifier.ExternalIdentifier;
//import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
//import net.digitalid.core.identification.identity.InternalNonHostIdentity;
//import net.digitalid.core.packet.exceptions.NetworkException;
//import net.digitalid.core.resolution.predecessor.Predecessor;
//import net.digitalid.core.service.CoreService;
//import net.digitalid.core.synchronizer.Synchronizer;
//
//import android.content.Context;
//
///**
// * TODO: This code has just been copied from the client class.
// */
//public class ClientActions {
//    
//    /**
//     * Accredits this client at the given identity.
//     * Loop on {@link Role#reloadOrRefreshState(net.digitalid.service.core.service.Service...)} afterwards.
//     * 
//     * @param identity the identity at which this client is to be accredited.
//     * @param password the password for accreditation at the given identity.
//     * 
//     * @return the native role which was accredited at the given identity.
//     * 
//     * @require Password.isValid(password) : "The password is valid.";
//     */
//    @Committing
//    public final @Nonnull NativeRole accredit(@Nonnull InternalNonHostIdentity identity, @Nonnull String password) throws ExternalException {
//        final @Nonnull NativeRole role = addRole(identity, new Random().nextLong());
//        Database.commit();
//        try {
//            final @Nonnull ClientAgentAccredit action = new ClientAgentAccredit(role, password);
//            Context.getRoot(role).createForActions();
//            action.executeOnClient();
//            action.send();
//            Database.commit();
//        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            Database.rollback();
//            role.remove();
//            Database.commit();
//            throw exception;
//        }
//        return role;
//    }
//    
//    
//    /**
//     * Opens a new account with the given identifier and merges existing roles and identifiers into it.
//     * 
//     * TODO: Make the method more resilient to failures so that it can also be restarted mid-way through.
//     * 
//     * @param subject the identifier of the account which is to be created.
//     * @param category the category of the account which is to be created.
//     * @param roles the roles to be closed and merged into the new account.
//     * @param identifiers the identifiers to be merged into the new account.
//     * 
//     * @return the native role of this client at the newly created account.
//     * 
//     * @require !subject.exists() : "The subject does not exist.";
//     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
//     * @require !category.isType() || roles.size() <= 1 && identifiers.isEmpty() : "If the category denotes a type, at most one role and no identifier may be given.";
//     */
//    @Committing
//    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull ReadOnlyList<NativeRole> roles, @Nonnull ReadOnlyList<ExternalIdentifier> identifiers) throws InterruptedException, ExternalException {
//        Require.that(!subject.exists()).orThrow("The subject does not exist.");
//        Require.that(category.isInternalNonHostIdentity()).orThrow("The category denotes an internal non-host identity.");
//        Require.that(!category.isType() || roles.size() <= 1 && identifiers.isEmpty()).orThrow("If the category denotes a type, at most one role and no identifier may be given.");
//        
//        Database.commit(); // This commit is necessary in case the client and host share the same database.
//        final @Nonnull AccountOpen accountOpen = new AccountOpen(subject, category, this); accountOpen.send();
//        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) Mapper.mapIdentity(subject, category, null);
//        final @Nonnull NativeRole newRole = addRole(identity, accountOpen.getAgentNumber());
//        accountOpen.initialize(newRole);
//        Database.commit();
//        
//        final @Nonnull FreezableList<Pair<Predecessor, Block>> states = FreezableArrayList.withInitialCapacity(roles.size() + identifiers.size());
//        
//        for (final @Nonnull NativeRole role : roles) {
//            if (role.getIdentity().getCategory() != category) { throw InternalException.with("A role is of the wrong category."); }
//            Synchronizer.reload(role, CoreService.INSTANCE);
//            final @Nonnull ClientAgent clientAgent = role.getAgent();
//            final @Nonnull Block state = CoreService.INSTANCE.getState(role, clientAgent.permissions().get(), clientAgent.restrictions().get(), clientAgent);
//            final @Nonnull Predecessor predecessor = new Predecessor(role.getIdentity().getAddress());
//            states.add(Pair.of(predecessor, state));
//            Synchronizer.execute(new AccountClose(role, subject));
////            role.remove();
////            Database.commit();
//        }
//        
//        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
//            // TODO: Ask 'digitalid.net' to let the relocation be confirmed by the user.
//        }
//        
//        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
//            // TODO: Wait until the relocation from 'digitalid.net' can be verified.
//            states.add(Pair.of(new Predecessor(identifier), null));
//        }
//        
//        final @Nonnull AccountInitialize accountInitialize = new AccountInitialize(newRole, states.freeze());
//        accountInitialize.send();
//        accountInitialize.executeOnClient();
//        Database.commit();
//        return newRole;
//    }
//    
//    /**
//     * Opens a new account with the given identifier and category.
//     * 
//     * @param identifier the identifier of the account to be created.
//     * @param category the category of the account to be created.
//     * 
//     * @return the native role of this client at the newly created account.
//     * 
//     * @require subject.doesNotExist() : "The subject does not exist.";
//     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
//     */
//    @Committing
//    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier identifier, @Nonnull Category category) throws InterruptedException, ExternalException {
//        return openAccount(identifier, category, FreezableLinkedList.<NativeRole>withNoElements().freeze(), FreezableLinkedList.<ExternalIdentifier>withNoElements().freeze());
//    }
//    
//}
