package ch.virtualid.credential;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.ClientModule;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.xdf.Block;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 * This class provides database access to the {@link ClientCredential client credentials} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.2
 */
public final class ClientCredentialModule implements ClientModule {
    
    public static final ClientCredentialModule MODULE = new ClientCredentialModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(final @Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "credential (time " + Time.FORMAT + " NOT NULL, entity " + EntityClass.FORMAT + " NOT NULL, issuer " + Mapper.FORMAT + " NOT NULL, permissions " + Block.FORMAT + " NOT NULL, credential " + Block.FORMAT + " NOT NULL, PRIMARY KEY (time, entity, issuer, permissions), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (issuer) " + Mapper.REFERENCE + ")");
            Database.onInsertUpdate(statement, "credential", 4, "time", "entity", "issuer", "permissions", "credential");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotUpdate(statement, "credential");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "credential");
        }
    }
    
    
    /**
     * Loads the credential with the given role, issuer and permissions from the database.
     * 
     * @param role the role of the credential which is to be returned.
     * @param issuer the issuer of the credential which is to be returned.
     * @param permissions the permissions of the credential which is to be returned.
     * 
     * @return the credential with the given role, issuer and permissions or null if not found.
     */
    @Pure
    private @Nullable ClientCredential loadCredential(@Nonnull Role role, @Nonnull InternalNonHostIdentity issuer, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + role.getSite() + "credential WHERE time < " + Time.HOUR.ago());
        }
        
        final @Nonnull String SQL = "SELECT credential FROM " + role.getSite() + "credential WHERE entity = ? AND issuer = ? AND permissions = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            role.set(preparedStatement, 1);
            issuer.set(preparedStatement, 2);
            permissions.toBlock().set(preparedStatement, 3);
            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery(SQL)) {
                if (resultSet.next()) return null; // TODO
                else return null;
            }
        }
    }
    
    private void storeCredential(@Nonnull ClientCredential credential) {
        //  TODO
    }
    
    /**
     * Resets the credentials of the given role.
     * 
     * @param role the role whose credentials are to be reset.
     */
    public void resetCredentials(@Nonnull Role role) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + role.getSite() + "credential WHERE entity = " + role);
        }
        credentials.remove(role);
    }
    
    /**
     * Stores the client credentials given their role, issuer and permissions.
     */
    private final @Nonnull ConcurrentMap<Role, ConcurrentMap<Pair<InternalNonHostIdentity, ReadonlyAgentPermissions>, ClientCredential>> credentials = new ConcurrentHashMap<Role, ConcurrentMap<Pair<InternalNonHostIdentity, ReadonlyAgentPermissions>, ClientCredential>>();
    
    /**
     * Returns the 
     * 
     * @param role
     * @param permissions
     * 
     * @return 
     * 
     * @require permissions.isNotEmpty() : "The permissions are not empty.";
     */
    public @Nonnull ClientCredential getIdentityBasedCredential(@Nonnull Role role, @Nonnull ReadonlyAgentPermissions permissions) {
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        final @Nonnull Triplet<Role, InternalNonHostIdentity, ReadonlyAgentPermissions>  triplet = new Triplet<Role, InternalNonHostIdentity, ReadonlyAgentPermissions>(role, role.getIssuer(), permissions);
        @Nullable ClientCredential credential = credentials.get(triplet);
        
        if (credential != null && !credential.isActive()) credential = null;
        
        if (credential == null) {
            
            
            // Replace the stored credential.
        }
        
        return credential;
    }
    
    public @Nonnull ClientCredential getAttributeBasedCredential(@Nonnull Role role, @Nonnull SemanticType type, @Nonnull ReadonlyAgentPermissions permissions) {
        assert type.isAttributeType() : "The type is an attribute type.";
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        // TODO: Shortening with CredentialShorteningQuery.
    }
    
    
    /**
     * Returns a randomized authorization for the given requester from the desired issuer for the desired authorization.
     * 
     * @param requester the VID that requests the credential.
     * @param issuer the VID that issues the credential.
     * @param authorization the desired authorization.
     * 
     * @return a randomized authorization for the given authorization.
     * 
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require authorization != null && !authorization.isEmpty() : "The authorization is not empty.";
     */
    public RandomizedAuthorization getRandomizedAuthorization(long requester, long issuer, AgentPermissions authorization) throws SQLException {
        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
        assert authorization != null && !authorization.isEmpty() : "The authorization is not empty.";
        
        Map<Credential.RandomizedAuthorization, Credential> map = credentials.get(requester).get(issuer);
        if (map != null) {
            for (RandomizedAuthorization randomizedAuthorization : map.keySet()) {
                if (randomizedAuthorization.getAuthorization().covers(authorization)) return randomizedAuthorization;
            }
        }
        return new RandomizedAuthorization(authorization);
    }
    
    /**
     * Returns the credential for the given requester from the desired issuer for the desired authorization.
     * 
     * @param requester the VID that requests the credential.
     * @param issuer the VID that issues the credential.
     * @param randomizedAuthorization the desired authorization in randomized form.
     * 
     * @return the credential for the given requester from the desired issuer.
     * 
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
     */
    public Credential getCredential(long requester, long issuer, RandomizedAuthorization randomizedAuthorization) throws Exception {
        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
        assert randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
        
        Map<Credential.RandomizedAuthorization, Credential> map = credentials.get(requester).get(issuer);
        if (map == null) {
            map = new HashMap<Credential.RandomizedAuthorization, Credential>();
            credentials.get(requester).put(issuer, map);
        }
        Credential credential = map.get(randomizedAuthorization);
        if (credential == null || credential.getIssuance() < System.currentTimeMillis() - 3600000) {
            credential = Request.obtainCredential(this, requester, issuer, randomizedAuthorization);
            map.put(randomizedAuthorization, credential);
        }
        return credential;
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
