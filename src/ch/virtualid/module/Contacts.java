package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.concepts.Context;
import ch.virtualid.concept.Entity;
import ch.virtualid.database.Entity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
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
public final class Contacts extends Module {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client or host connection to the database.
     */
    Contacts(@Nonnull Entity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_preference (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_permission (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS contact_authentication (identity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, contact, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addTypeReference("contact_preference", "type");
        Mapper.addTypeReference("contact_permission", "type");
        Mapper.addTypeReference("contact_authentication", "type");
        Mapper.addReference("contact_preference", "contact");
        Mapper.addReference("contact_permission", "contact");
        Mapper.addReference("contact_authentication", "contact");
    }
    
    /**
     * Returns the preferences of the given contact at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity which has the given contact.
     * @param contact the contact whose preferences are to be returned.
     * @return the preferences of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactPreferences(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact) throws SQLException {
        @Nonnull Set<SemanticType> preferences = getTypes(connection, identity, "contact_preference", "contact = " + contact);
        if (preferences.isEmpty() && contact.hasBeenMerged()) return getContactPreferences(connection, identity, contact);
        return preferences;
    }
    
    /**
     * Sets the preferences of the given contact at the given identity to the given types.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose preferences are to be set.
     * @param preferences the preferences to be set for the given contact.
     */
    static void setContactPreferences(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> preferences) throws SQLException {
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
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are to be returned.
     * @param inherited whether the permissions of the supercontexts are inherited.
     * @return the permissions of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws SQLException {
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
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are extended.
     * @param permissions the permissions to be added to the given contact.
     */
    static void addContactPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws SQLException {
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
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose permissions are reduced.
     * @param permissions the permissions to be removed from the given contact.
     */
    static void removeContactPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> permissions) throws SQLException {
        int removed = removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
        if (removed == 0 && !permissions.isEmpty() && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_permission", "contact", contact.getNumber(), permissions);
    }
    
    /**
     * Returns the authentications of the given contact at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are to be returned.
     * @param inherited whether the authentications of the supercontexts are inherited.
     * @return the authentications of the given contact at the given identity.
     */
    static @Nonnull Set<SemanticType> getContactAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, boolean inherited) throws SQLException {
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
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are extended.
     * @param authentications the authentications to be added to the given contact.
     */
    static void addContactAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws SQLException {
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
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param contact the contact whose authentications are reduced.
     * @param authentications the authentications to be removed from the given contact.
     */
    static void removeContactAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Set<SemanticType> authentications) throws SQLException {
        int removed = removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
        if (removed == 0 && contact.hasBeenMerged()) removeTypes(connection, identity, "contact_authentication", "contact", contact.getNumber(), authentications);
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        
    }
    
}
