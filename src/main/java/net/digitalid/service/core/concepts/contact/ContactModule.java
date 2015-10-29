package net.digitalid.service.core.concepts.contact;

import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.dataservice.Service;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class provides database access to the {@link Contact contacts} of the core service.
 */
@Stateless
public final class ContactModule implements StateModule {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Initialization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores an instance of this module.
     */
    public static final ContactModule MODULE = new ContactModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table Creation and Deletion –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws AbortException {
        // TODO: Remove the contact_preference table.
        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_preference (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (contact) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_permission (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (contact) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_authentication (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (contact) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//        }
//        
//        Mapper.addReference("contact_preference", "contact");
//        Mapper.addReference("contact_permission", "contact");
//        Mapper.addReference("contact_authentication", "contact");
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.contacts.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.contacts.module@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.service.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code contacts.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("contacts.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws AbortException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws AbortException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Getter and Setter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.contacts.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.map("entry.contacts.state@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.service.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code contacts.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("contacts.state@core.digitalid.net").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve the entries of the given entity from the database table(s).
        }
        return new ListWrapper(STATE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add the entries of the given entity to the database table(s).
        }
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    
//    /**
//     * Returns the preferences of the given contact at the given identity.
//     * 
//     * @param identity the identity which has the given contact.
//     * @param contact the contact whose preferences are to be returned.
//     * @return the preferences of the given contact at the given identity.
//     */
//    @NonCommitting
//    static @Nonnull Set<SemanticType> getContactPreferences(@Nonnull NonHostIdentity identity, @Nonnull Person contact) throws AbortException {
//        @Nonnull Set<SemanticType> preferences = getTypes(connection, identity, "contact_preference", "contact = " + contact);
//        if (preferences.isEmpty() && contact.hasBeenMerged()) return getContactPreferences(connection, identity, contact);
//        return preferences;
//    }
//    
//    /**
//     * Sets the preferences of the given contact at the given identity to the given types.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose preferences are to be set.
//     * @param preferences the preferences to be set for the given contact.
//     */
//    @NonCommitting
//    static void setContactPreferences(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> preferences) throws AbortException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            int updated = statement.executeUpdate("DELETE FROM contact_preference WHERE identity = " + identity + " AND contact = " + contact);
//            if (updated == 0 && contact.hasBeenMerged()) {
//                setContactPreferences(connection, identity, contact, preferences);
//                return;
//            }
//        }
//        
//        addTypes(connection, identity, "contact_preference", "contact", contact.getNumber(), preferences);
//    }
//    
//    /**
//     * Returns the permissions of the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose permissions are to be returned.
//     * @param inherited whether the permissions of the supercontexts are inherited.
//     * @return the permissions of the given contact at the given identity.
//     */
//    @NonCommitting
//    static @Nonnull Set<SemanticType> getContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws AbortException {
//        @Nonnull Set<SemanticType> permissions = getTypes(connection, identity, "contact_permission", "contact = " + contact);
//        if (permissions.isEmpty() && contact.hasBeenMerged()) return getContactPermissions(connection, identity, contact, inherited);
//        if (inherited) {
//            @Nonnull Set<Context> contexts = getContexts(connection, identity, contact);
//            for (@Nonnull Context context : contexts) {
//                permissions.addAll(getContextPermissions(connection, identity, context, true));
//            }
//        }
//        return permissions;
//    }
//    
//    /**
//     * Adds the given permissions to the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose permissions are extended.
//     * @param permissions the permissions to be added to the given contact.
//     */
//    @NonCommitting
//    static void addContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws AbortException {
//        try {
//            addTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
//        } catch (@Nonnull SQLException exception) {
//            if (contact.hasBeenMerged()) addContactPermissions(connection, identity, contact, permissions);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Removes the given permissions from the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose permissions are reduced.
//     * @param permissions the permissions to be removed from the given contact.
//     */
//    @NonCommitting
//    static void removeContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws AbortException {
//        int removed = removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
//        if (removed == 0 && !permissions.isEmpty() && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
//    }
//    
//    /**
//     * Returns the authentications of the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose authentications are to be returned.
//     * @param inherited whether the authentications of the supercontexts are inherited.
//     * @return the authentications of the given contact at the given identity.
//     */
//    @NonCommitting
//    static @Nonnull Set<SemanticType> getContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws AbortException {
//        @Nonnull Set<SemanticType> authentications = getTypes(connection, identity, "contact_authentication", "contact = " + contact);
//        if (authentications.isEmpty() && contact.hasBeenMerged()) return getContactAuthentications(connection, identity, contact, inherited);
//        if (inherited) {
//            @Nonnull Set<Context> contexts = getContexts(connection, identity, contact);
//            for (@Nonnull Context context : contexts) {
//                authentications.addAll(getContextAuthentications(connection, identity, context, true));
//            }
//        }
//        return authentications;
//    }
//    
//    /**
//     * Adds the given authentications to the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose authentications are extended.
//     * @param authentications the authentications to be added to the given contact.
//     */
//    @NonCommitting
//    static void addContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws AbortException {
//        try {
//            addTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
//        } catch (@Nonnull SQLException exception) {
//            if (contact.hasBeenMerged()) addContactAuthentications(connection, identity, contact, authentications);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Removes the given authentications from the given contact at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param contact the contact whose authentications are reduced.
//     * @param authentications the authentications to be removed from the given contact.
//     */
//    @NonCommitting
//    static void removeContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws AbortException {
//        int removed = removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
//        if (removed == 0 && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
//    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
