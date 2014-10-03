package ch.virtualid.module.client;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.credential.Credential;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.ClientModule;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Credentials extends ClientModule {
    
    // TODO: This class name already exists as a host module!
    
    /**
     * Stores the credentials of this client and is never null. (Mapping: (Requester, Issuer, Authorization) => Credential.)
     */
    private final Map<Long, Map<Long, Map<RandomizedAgentPermissions, Credential>>> credentials = new HashMap<Long, Map<Long, Map<RandomizedAgentPermissions, Credential>>>();
    
    
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
     * @return the credential for the given requester from the desired issuer.
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
    
}
