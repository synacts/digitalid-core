package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Module;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the contacts of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Contacts extends BothModule {
    
    /**
     * Stores the semantic type {@code contacts.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("contacts.module@virtualid.ch").load(TupleWrapper.TYPE, );
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    static { Module.add(new Contacts()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_preference (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_permission (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_authentication (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addReference("contact_preference", "contact");
        Mapper.addReference("contact_permission", "contact");
        Mapper.addReference("contact_authentication", "contact");
    }
    
    
    /**
     * Returns the preferences of the given contact at the given identity.
     * 
     * @param identity the identity which has the given contact.
     * @param contact the contact whose preferences are to be returned.
     * @return the preferences of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactPreferences(@Nonnull NonHostIdentity identity, @Nonnull Person contact) throws SQLException {
        @Nonnull Set<SemanticType> preferences = getTypes(connection, identity, "contact_preference", "contact = " + contact);
        if (preferences.isEmpty() && contact.hasBeenMerged()) return getContactPreferences(connection, identity, contact);
        return preferences;
    }
    
    /**
     * Sets the preferences of the given contact at the given identity to the given types.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose preferences are to be set.
     * @param preferences the preferences to be set for the given contact.
     */
    static void setContactPreferences(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> preferences) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            int updated = statement.executeUpdate("DELETE FROM contact_preference WHERE identity = " + identity + " AND contact = " + contact);
            if (updated == 0 && contact.hasBeenMerged()) {
                setContactPreferences(connection, identity, contact, preferences);
                return;
            }
        }
        
        addTypes(connection, identity, "contact_preference", "contact", contact.getNumber(), preferences);
    }
    
    /**
     * Returns the permissions of the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are to be returned.
     * @param inherited whether the permissions of the supercontexts are inherited.
     * @return the permissions of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws SQLException {
        @Nonnull Set<SemanticType> permissions = getTypes(connection, identity, "contact_permission", "contact = " + contact);
        if (permissions.isEmpty() && contact.hasBeenMerged()) return getContactPermissions(connection, identity, contact, inherited);
        if (inherited) {
            @Nonnull Set<Context> contexts = getContexts(connection, identity, contact);
            for (@Nonnull Context context : contexts) {
                permissions.addAll(getContextPermissions(connection, identity, context, true));
            }
        }
        return permissions;
    }
    
    /**
     * Adds the given permissions to the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are extended.
     * @param permissions the permissions to be added to the given contact.
     */
    static void addContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws SQLException {
        try {
            addTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
        } catch (@Nonnull SQLException exception) {
            if (contact.hasBeenMerged()) addContactPermissions(connection, identity, contact, permissions);
            else throw exception;
        }
    }
    
    /**
     * Removes the given permissions from the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are reduced.
     * @param permissions the permissions to be removed from the given contact.
     */
    static void removeContactPermissions(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws SQLException {
        int removed = removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
        if (removed == 0 && !permissions.isEmpty() && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
    }
    
    /**
     * Returns the authentications of the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are to be returned.
     * @param inherited whether the authentications of the supercontexts are inherited.
     * @return the authentications of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws SQLException {
        @Nonnull Set<SemanticType> authentications = getTypes(connection, identity, "contact_authentication", "contact = " + contact);
        if (authentications.isEmpty() && contact.hasBeenMerged()) return getContactAuthentications(connection, identity, contact, inherited);
        if (inherited) {
            @Nonnull Set<Context> contexts = getContexts(connection, identity, contact);
            for (@Nonnull Context context : contexts) {
                authentications.addAll(getContextAuthentications(connection, identity, context, true));
            }
        }
        return authentications;
    }
    
    /**
     * Adds the given authentications to the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are extended.
     * @param authentications the authentications to be added to the given contact.
     */
    static void addContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws SQLException {
        try {
            addTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
        } catch (@Nonnull SQLException exception) {
            if (contact.hasBeenMerged()) addContactAuthentications(connection, identity, contact, authentications);
            else throw exception;
        }
    }
    
    /**
     * Removes the given authentications from the given contact at the given identity.
     * 
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are reduced.
     * @param authentications the authentications to be removed from the given contact.
     */
    static void removeContactAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws SQLException {
        int removed = removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
        if (removed == 0 && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity entity) throws SQLException {
        
    }
    
}
