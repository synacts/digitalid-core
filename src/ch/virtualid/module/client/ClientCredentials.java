package ch.virtualid.module.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.module.ClientModule;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the {@link ClientCredentials client credentials} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class ClientCredentials implements ClientModule {
    
    public static final ClientCredentials MODULE = new ClientCredentials();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the credentials of this client and is never null. (Mapping: (Requester, Issuer, Authorization) => Credential.)
     */
//    private final Map<Long, Map<Long, Map<RandomizedAgentPermissions, Credential>>> credentials = new HashMap<Long, Map<Long, Map<RandomizedAgentPermissions, Credential>>>();
    
    /**
     * Returns a randomized authorization for the given requester from the desired issuer for the desired authorization.
     * 
     * @param requester the VID that requests the credential.
     * @param issuer the VID that issues the credential.
     * @param authorization the desired authorization.
     * @return a randomized authorization for the given authorization.
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require authorization != null && !authorization.isEmpty() : "The authorization is not empty.";
     */
//    public RandomizedAuthorization getRandomizedAuthorization(long requester, long issuer, AgentPermissions authorization) throws SQLException {
//        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
//        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
//        assert authorization != null && !authorization.isEmpty() : "The authorization is not empty.";
//        
//        Map<Credential.RandomizedAuthorization, Credential> map = credentials.get(requester).get(issuer);
//        if (map != null) {
//            for (RandomizedAuthorization randomizedAuthorization : map.keySet()) {
//                if (randomizedAuthorization.getAuthorization().covers(authorization)) return randomizedAuthorization;
//            }
//        }
//        return new RandomizedAuthorization(authorization);
//    }
    
    /**
     * Returns the credential for the given requester from the desired issuer for the desired authorization.
     * 
     * @param requester the VID that requests the credential.
     * @param issuer the VID that issues the credential.
     * @param randomizedAuthorization the desired authorization in randomized form.
     * @return the credential for the given requester from the desired issuer.
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
     */
//    public Credential getCredential(long requester, long issuer, RandomizedAuthorization randomizedAuthorization) throws Exception {
//        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
//        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
//        assert randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
//        
//        Map<Credential.RandomizedAuthorization, Credential> map = credentials.get(requester).get(issuer);
//        if (map == null) {
//            map = new HashMap<Credential.RandomizedAuthorization, Credential>();
//            credentials.get(requester).put(issuer, map);
//        }
//        Credential credential = map.get(randomizedAuthorization);
//        if (credential == null || credential.getIssuance() < System.currentTimeMillis() - 3600000) {
//            credential = Request.obtainCredential(this, requester, issuer, randomizedAuthorization);
//            map.put(randomizedAuthorization, credential);
//        }
//        return credential;
//    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
