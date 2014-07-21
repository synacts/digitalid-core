package ch.virtualid.client;

import ch.virtualid.agent.IncomingRole;
import ch.virtualid.agent.Permissions;
import ch.virtualid.agent.RandomizedPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.credential.ClientCredential;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Group;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.expression.Expression;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.virtualid.module.client.Roles;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.Packet;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.Int32Wrapper;
import ch.xdf.Int8Wrapper;
import ch.xdf.IntegerWrapper;
import ch.xdf.IntvarWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A client is configured with a name and a secret and stores credentials.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.6
 */
public final class Client extends Site {
    
    /**
     * Stores the aspect of a new role being added to the observed client.
     */
    public static final @Nonnull Aspect ROLE_ADDED = new Aspect(Client.class, "role added");
    
    
    /**
     * The pattern that valid client names have to match.
     */
    private static final @Nonnull Pattern pattern = Pattern.compile("[a-z0-9]+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Returns whether the given client name is valid.
     * 
     * @param name the client name of interest.
     * @return whether the given client name is valid.
     */
    public static boolean isValid(@Nonnull String name) {
        return name.length() <= 50 && pattern.matcher(name).matches();
    }
    
    @Pure
    @Override
    public @Nonnull String getReference() {
        return "REFERENCES " + this + "_role (role) ON DELETE CASCADE";
    }
    
    
    /**
     * Stores the name of this client and is never null.
     */
    private final String name;
    
    /**
     * Stores the secret of this client and is never null.
     */
    private final BigInteger secret;
    
    /**
     * Stores the native VIDs of this client and is never null. (Mapping: Native VID => time of last request.)
     */
    private final Map<Long, Long> natives = new HashMap<Long, Long>();
    
    /**
     * Stores the credentials of this client and is never null. (Mapping: (Requester, Issuer, Authorization) => Credential.)
     */
    private final Map<Long, Map<Long, Map<RandomizedPermissions, Credential>>> credentials = new HashMap<Long, Map<Long, Map<RandomizedPermissions, Credential>>>();
    
    /**
     * Creates a new client with the given name.
     * 
     * @param name the name of the new client.
     * @require isValid(name) : "The name of the client is valid.";
     */
    public Client(@Nonnull String name) throws InvalidEncodingException, SQLException, IOException {
        assert isValid(name) : "The name of the client is valid.";
        
        this.name = name;
        
        @Nonnull File file = new File(Directory.CLIENTS.getPath() +  Directory.SEPARATOR + name + ".client.xdf");
        if (file.exists()) {
            this.secret = new IntegerWrapper(SelfcontainedWrapper.readAndClose(new FileInputStream(file)).getElement()).getValue();
        } else {
            Random random = new SecureRandom();
            this.secret = new BigInteger(Parameters.HASH, random);
            new SelfcontainedWrapper(NonHostIdentifier.CLIENT_SECRET, new IntegerWrapper(secret)).writeAndClose(new FileOutputStream(file));
        }
        
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName() + "_natives (vid BIGINT NOT NULL, time BIGINT NOT NULL, PRIMARY KEY (vid), FOREIGN KEY (vid) REFERENCES map_vid (vid))");
            
            ResultSet resultSet = statement.executeQuery("SELECT vid, time FROM " + getName() + "_natives");
            while (resultSet.next()) {
                natives.put(resultSet.getLong(1), resultSet.getLong(2));
                credentials.put(resultSet.getLong(1), new HashMap<Long, Map<RandomizedPermissions, Credential>>());
            }
            
            connection.commit();
        }
    }
    
    /**
     * Returns the name of this client.
     * 
     * @return the name of this client.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the secret of this client.
     * 
     * @return the secret of this client.
     */
    public BigInteger getSecret() {
        return secret;
    }
    
    
    /**
     * Stores the roles of this client.
     */
    private @Nullable FreezableList<Role> roles;
    
    /**
     * Stores whether the roles are loaded.
     */
    private boolean rolesLoaded = false;
    
    /**
     * Returns the roles of this client.
     * 
     * @return the roles of this client.
     */
    @Pure
    public @Nonnull ReadonlyList<Role> getRoles() throws SQLException {
        if (!rolesLoaded) {
            roles = Roles.getRoles(this);
            rolesLoaded = true;
        }
        assert roles != null;
        return roles;
    }
    
    /**
     * Adds the given role to the roles of this client.
     * 
     * @param issuer the issuer of the role to add.
     * @param authorization the incoming role with the authorization for role to add.
     */
    public void addRole(@Nonnull NonHostIdentity issuer, @Nonnull IncomingRole authorization) throws SQLException {
        getRoles();
        assert roles != null;
        roles.add(Role.add(this, issuer, null, null, authorization));
        notify(ROLE_ADDED);
    }
    
    
    /**
     * Returns whether this client is accredited at the given VID.
     * 
     * @param vid the VID of interest.
     * @return whether this client is accredited at the given VID.
     */
    public synchronized boolean isNative(long vid) {
        return natives.containsKey(vid);
    }
    
    /**
     * Returns the VIDs at which this client is accredited.
     * 
     * @return the VIDs at which this client is accredited.
     */
    public synchronized Set<Long> getNatives() {
        return natives.keySet();
    }
    
    /**
     * Returns the time of the last request to the given VID.
     * 
     * @param vid the VID of interest.
     * @return the time of the last request to the given VID.
     * @require isNative(vid) : "This client is accredited at the given VID.";
     */
    public synchronized long getTimeOfLastRequest(long vid) {
        assert isNative(vid) : "This client is accredited at the given VID.";
        
        return natives.get(vid);
    }
    
    /**
     * Sets the time of the last request to the given VID.
     * 
     * @param identity the VID of the last request.
     * @param time the time of the last request.
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     * @require time > 0 : "The time value is positive.";
     */
    synchronized void setTimeOfLastRequest(@Nonnull Identity identity, long time) throws SQLException {
        assert time > 0 : "The time value is positive.";
        
        if (!isNative(identity)) credentials.put(identity, new HashMap<Long, Map<RandomizedPermissions, Credential>>());
        
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("REPLACE INTO " + getName() + "_natives (vid, time) VALUES (" + identity + ", " + time + ")");
            connection.commit();
        }
        
        natives.put(identity, time);
    }
    
    private void audit(long auditTime, @Nullable List<Block> auditTrail) {
        // TODO
    }
    
    private Response makeInternalRequest() {
        // TODO (including auditing).
    }
    
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
    public RandomizedAuthorization getRandomizedAuthorization(long requester, long issuer, Permissions authorization) throws SQLException {
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
    
    
    
    /**
     * Opens a new account with the given identifier and category.
     * 
     * @param client the client to be authorized at the newly created VID.
     * @param identifier the identifier of the new account.
     * @param name the name of the client.
     * @param category the category of the new account.
     * @require client != null : "The client is not null.";
     * @require identifier != null : "The identifier is not null.";
     * @require Identifier.isValid(identifier) : "The identifier is valid.";
     * @require !Identifier.isHost(identifier) : "The identifier may not denote a host.";
     * @require name != null : "The name is not null.";
     * @require Category.isValid(category) : "The category is valid.";
     */
    public static void openAccount(Client client, String identifier, String name, byte category) throws Exception {
        assert client != null : "The client is not null.";
        assert identifier != null : "The identifier is not null.";
        assert Identifier.isValid(identifier) : "The identifier is valid.";
        assert !Identifier.isHost(identifier) : "The identifier may not denote a host.";
        assert name != null : "The name is not null.";
        assert Category.isValid(category) : "The category is valid.";
        
        Block[] elements = new Block[] {new Int8Wrapper(category).toBlock(), new StringWrapper(name).toBlock()};
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.open.account@virtualid.ch", new TupleWrapper(elements).toBlock());
        Packet response = new Packet(content, NonHostIdentifier.getHost(identifier), new SymmetricKey(), identifier, 0, client.getSecret()).send();
        client.setTimeOfLastRequest(Mapper.getVid(identifier), response.getSignatures().getTime());
    }
    
    /**
     * Returns the caching period of the given semantic type.
     * 
     * @param semanticType the semantic type of interest.
     * @return the caching period of the given semantic type.
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The number has to denote a semantic type.";
     */
    public static long getCachingPeriod(long semanticType) throws Exception {
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The number has to denote a semantic type.";
        
        // The first two semantic types need a special treatment in order to prevent infinite loops, the second two for performance reasons.
        if (semanticType == Vid.HOST_PUBLIC_KEY || semanticType == Vid.ATTRIBUTE_TYPE_CACHING || semanticType == Vid.INCOMING_DELEGATIONS || semanticType == Vid.OUTGOING_DELEGATIONS) {
            return YEAR;
        } else {
            return new IntvarWrapper(getAttributeNotNullUnwrapped(semanticType, Vid.ATTRIBUTE_TYPE_CACHING)).getValue() * 1000;
        }
    }
    
    /**
     * Determines whether the given VID is authorized to certify the given element.
     * 
     * @param identifier the identifier of the certifying VID.
     * @param value the certified value as a selfcontained block.
     * @return {@code true} if the given VID is authorized to certify the given element, {@code false} otherwise.
     * @require identifier != null : "The identifier is not null.";
     * @require value != null : "The value is not null.";
     */
    private static boolean isAuthorized(String identifier, Block value) throws Exception {
        assert identifier != null : "The identifier is not null.";
        assert value != null : "The value is not null.";
        
        long vid = Mapper.getVid(identifier);
        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
        
        if (vid == type) return true;
        
        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
        if (attribute == null) return false;
        
        List<Block> incoming_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
        for (Block incoming_delegation : incoming_delegations) {
            Block[] elements = new TupleWrapper(incoming_delegation).getElementsNotNull(3);
            if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type) {
                String restriction = new StringWrapper(elements[2]).getString();
                Expression expression = Expression.parse(restriction);
                if (expression.matches(value)) {
                    // Check that the delegating VID references the current VID with the same type and expression.
                    identifier = new StringWrapper(elements[1]).getString();
                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
                    if (attribute == null) continue;
                    List<Block> outgoing_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
                    for (Block outgoing_delegation : outgoing_delegations) {
                        elements = new TupleWrapper(outgoing_delegation).getElementsNotNull(3);
                        if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type && Mapper.getVid(new StringWrapper(elements[1]).getString()) == vid && new StringWrapper(elements[2]).getString().equalsIgnoreCase(restriction)) {
                            if (isAuthorized(identifier, value)) return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Retrieves the attributes of the given types from the given VID as the given client and requester.
     * 
     * @param client the client retrieving the types or null if the request is issued by a host.
     * @param requester the VID which requests the attributes or zero to indicate an unsigned request.
     * @param requestee the VID which attributes are to be retrieved.
     * @param types the types which are to be retrieved (each value needs to denote an attribute type).
     * @param time the time at which the attributes need to be fresh in order to be fetched from the cache.
     * @return the available attributes as an array of equal length as the {@code types} and also having the same indexes.
     * @require requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require types != null : "The type array is not null.";
     * @require time >= 0 : "The time value is non-negative.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block[] getAttributes(Client client, long requester, long requestee, long[] types, long time) throws Exception {
        assert requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert types != null : "The type array is not null.";
        assert time >= 0 : "The time value is non-negative.";
        
        // TODO (long-term): Verify the new public key of virtualid.ch with the stale one and remove the following line.
        if (requestee == Vid.VIRTUALID) time = 0;
        
        boolean verification = true;
        Block[] attributes = new Block[types.length];
        List<Block> typesToRetrieve = new ArrayList<Block>(types.length);
        List<Integer> indexesToStore = new ArrayList<Integer>(types.length);
        for (int i = 0; i < types.length; i++) {
            if (types[i] == Vid.HOST_PUBLIC_KEY) verification = false;
            attributes[i] = Cache.getAttribute(requester, requestee, types[i], time);
            if (attributes[i] == null) {
                typesToRetrieve.add(new StringWrapper(Mapper.getIdentifier(types[i])).toBlock());
                indexesToStore.add(i);
            }
        }
        
        if (typesToRetrieve.size() > 0) {
            // Attribute requests for hosts are not encrypted.
            SymmetricKey symmetricKey = null;
            if (!Category.isHost(requestee)) symmetricKey = new SymmetricKey();
            
            // TODO (long-term): Determine how the request is to be signed, i.e. in which context of the requester is the requestee.
            
            SelfcontainedWrapper content = new SelfcontainedWrapper("request.get.attribute@virtualid.ch", new ListWrapper(typesToRetrieve).toBlock());
            Packet response;
            if (requester == requestee) {
                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee), 0l, client.getSecret()).send(verification);
            } else if (requester != 0 && Category.isPerson(requestee)) {
                Permissions authorization = new Permissions();
                for (int i : indexesToStore) authorization.put(types[i], false);
                Credential[] credentials = new Credential[]{client.getCredential(requester, requester, client.getRandomizedAuthorization(requester, requester, authorization))};
                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee), 0l, credentials, false).send(verification);
            } else {
                response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(requestee)), symmetricKey, Mapper.getIdentifier(requestee)).send(verification);
            }
            
            List<Block> retrievedAttributes = new ListWrapper(response.getContents().getElement()).getElements();
            
            long currentTime = System.currentTimeMillis();
            long responseTime = responsegetSignaturese().getTime();
            for (int i = 0; i < retrievedAttributes.size(); i++) {
                Block declaration = retrievedAttributes.get(i);
                if (declaration.isNotEmpty()) {
                    Block[] elements = new TupleWrapper(declaration).getElementsNotNull(2);
                    Block attribute = elements[0];
                    
                    SignatureWrapper certificate = new SignatureWrapper(attribute, true);
                    long type = Mapper.getVid(new SelfcontainedWrapper(certificate.getElement()).getIdentifier());
                    if (type != types[indexesToStore.get(i)]) throw new Exception("Request: The host delivered the requested attributes in a wrong order.");
                    long caching = getCachingPeriod(type);
                    
                    // Verify that the signature is still valid or remove it otherwise.
                    long retrievalTime = responseTime;
                    if (certificate.getSigner() == null) {
                        if (certificate.isSigned()) throw new Exception("Request: Attributes may only be certified by hosts.");
                        if (type == Vid.HOST_PUBLIC_KEY && requestee != Vid.VIRTUALID) throw new Exception("Request: The public key is certified.");
                    } else {
                        long certificateTime = certificate.getTime();
                        if (Mapper.getVid(certificate.getIdentifier()) == requestee && certificateTime + caching > currentTime && isAuthorized(certificate.getSigner(), certificate.getElement())) {
                            retrievalTime = certificateTime;
                        } else {
                            if (type == Vid.HOST_PUBLIC_KEY) throw new Exception("Request: The certificate of the public key is invalid.");
                            attribute = new SignatureWrapper(certificate.getElement()).getBlock();
                        }
                    }
                    
                    attributes[indexesToStore.get(i)] = attribute;
                    Cache.setAttribute(requester, requestee, type, retrievalTime + caching, attribute);
                }
            }
            
            if (!verification) responsgetSignaturesre().verify();
        }
        
        return attributes;
    }
    
    /**
     * Retrieves the attribute of the given semantic type from the given VID as the given client and requester.
     * 
     * @param client the client retrieving the type or null if the request is issued by a host.
     * @param requester the VID which requests the attribute or zero to indicate an unsigned request.
     * @param requestee the VID which attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @param time the time at which the attributes need to be fresh in order to be fetched from the cache.
     * @return the attribute of the given type from the given VID.
     * @require requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
     * @require time >= 0 : "The time value is non-negative.";
     */
    public static Block getAttribute(Client client, long requester, long requestee, long semanticType, long time) throws Exception {
        assert requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
        assert time >= 0 : "The time value is non-negative.";
        
        return getAttributes(client, requester, requestee, new long[] {semanticType}, time)[0];
    }
    
    /**
     * Returns the attribute of the given type from the given VID as the given client and requester or throws an exception if no such attribute is available.
     * 
     * @param client the client retrieving the type or null if the request is issued by a host.
     * @param requester the VID which requests the attribute or zero to indicate an unsigned request.
     * @param requestee the VID which attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @return the attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * @require requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block getAttributeNotNull(Client client, long requester, long requestee, long semanticType) throws Exception {
        assert requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
        
        Block attribute = getAttribute(client, requester, requestee, semanticType, System.currentTimeMillis());
        if (attribute == null) throw new Exception("The attribute of type '" + Mapper.getIdentifier(semanticType) + "' of the VID with the identifier '" + Mapper.getIdentifier(requestee) + "' is not available.");
        return attribute;
    }
    
    /**
     * Return the unwrapped attribute of the given type from the given VID as the given client and requester or throws an exception if no such attribute is available.
     * 
     * @param client the client retrieving the type or null if the request is issued by a host.
     * @param requester the VID which requests the attribute or zero to indicate an unsigned request.
     * @param requestee the VID which attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @return the attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * @require requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block getAttributeNotNullUnwrapped(Client client, long requester, long requestee, long semanticType) throws Exception {
        assert requester == 0 || Mapper.isVid(requester) && Category.isPerson(requester) : "The requester is zero or denote a person.";
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The third number has to denote a semantic type.";
        
        Block attribute = getAttributeNotNull(client, requester, requestee, semanticType);
        return new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement();
    }
    
    /**
     * Retrieves the attributes of the given types from the given VID.
     * 
     * @param requestee the VID which attributes are to be retrieved.
     * @param types the types which are to be retrieved (each value needs to denote an attribute type).
     * @param time the time at which the attributes need to be fresh in order to be fetched from the cache.
     * @return the available attributes as an array of equal length as the {@code types} and also having the same indexes.
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require types != null : "The type array is not null.";
     * @require time >= 0 : "The time value is non-negative.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block[] getAttributes(long requestee, long[] types, long time) throws Exception {
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert types != null : "The type array is not null.";
        assert time >= 0 : "The time value is non-negative.";
        
        return getAttributes(null, 0l, requestee, types, time);
    }
    
    /**
     * {@link #getAttributes(long, long[], long)} with the current time.
     */
    public static Block[] getAttributes(long requestee, long[] types) throws Exception {
        return getAttributes(requestee, types, System.currentTimeMillis());
    }
    
    /**
     * Retrieves the attribute of the given semantic type from the given VID.
     * 
     * @param requestee the VID which attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @param time the time at which the attributes need to be fresh in order to be fetched from the cache.
     * @return the attribute of the given type from the given VID.
     * @require Mapper.isVid(requestee) : "The requestee has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
     * @require time >= 0 : "The time value is non-negative.";
     */
    public static Block getAttribute(Identity requestee, SemanticType semanticType, long time) throws InvalidEncodingException {
        assert Mapper.isVid(requestee) : "The requestee has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
        assert time >= 0 : "The time value is non-negative.";
        
        return getAttribute(null, 0l, requestee, semanticType, time);
    }
    
    /**
     * {@link #getAttribute(long, long, long)} unwrapped with the current time.
     */
    public static Block getAttributeUnwrapped(Identity requestee, SemanticType semanticType) throws InvalidEncodingException {
        Block attribute = getAttribute(requestee, semanticType, System.currentTimeMillis());
        return new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement();
    }
    
    /**
     * Returns the attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * 
     * @param requestee the VID whose attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @return the attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * @require Mapper.isVid(requestee) : "The first number has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block getAttributeNotNull(Identity requestee, SemanticType semanticType) throws InvalidEncodingException {
        assert Mapper.isVid(requestee) : "The first number has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
        
        return getAttributeNotNull(null, 0l, requestee, semanticType);
    }
    
    /**
     * Returns the unwrapped attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * 
     * @param requestee the VID whose attribute is to be retrieved.
     * @param semanticType the attribute type which is to be retrieved.
     * @return the unwrapped attribute of the given type from the given VID or throws an exception if no such attribute is available.
     * @require Mapper.isVid(requestee) : "The first number has to denote a VID.";
     * @require Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
     * @ensure result != null : "The result is never null.";
     */
    public static Block getAttributeNotNullUnwrapped(Identity requestee, SemanticType semanticType) throws InvalidEncodingException {
        assert Mapper.isVid(requestee) : "The first number has to denote a VID.";
        assert Mapper.isVid(semanticType) && Category.isSemanticType(semanticType) : "The second number has to denote a semantic type.";
        
        Block attribute = getAttributeNotNull(requestee, semanticType);
        return new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement();
    }
    
    /**
     * Sets the attribute of the given VID or throws an exception if the given client is not properly authorized.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID whose attribute is to be set.
     * @param attribute the attribute to set at the given VID.
     * @param visibility the visibility of the attribute to set or null for non-persons.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     * @require !Category.isHost(vid) : "The VID may not denote a host.";
     * @require attribute != null : "The attribute is not null.";
     * @require Category.isPerson(vid) == (visibility != null) : "The visibility is null for non-persons and vice versa.";
     */
    public static void setAttribute(Client client, long vid, Block attribute, String visibility) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        assert !Category.isHost(vid) : "The VID may not denote a host.";
        assert attribute != null : "The attribute is not null.";
        assert Category.isPerson(vid) == (visibility != null) : "The visibility is null for non-persons and vice versa.";
        
        Block block = visibility == null ? Block.EMPTY : new StringWrapper(visibility).toBlock();
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.set.attribute@virtualid.ch", new TupleWrapper(new Block[]{attribute, block}).toBlock());
        new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
    }
    
    /**
     * Retrieves the contacts in the given context of the given VID.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID from which the contacts are to be retrieved.
     * @param context the context whose contacts are to be retrieved.
     * @return a list of all the contacts in the given context of the given VID.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     */
    public static List<Long> getContacts(Client client, long vid, int context) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The first number has to denote a VID.";
        
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.get.contact@virtualid.ch", new Int32Wrapper(context).toBlock());
        Packet response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
        
        List<Long> contacts = new LinkedList<Long>();
        List<Block> elements = new ListWrapper(responsegetContentst().getElement()).getElements();
        for (Block element : elements) contacts.add(Mapper.getVid(new StringWrapper(element).getString()));
        return contacts;
    }
    
    /**
     * Adds the given contact to the given context of the given VID.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID to which the contact is to be added.
     * @param context the context to which the contact is to be added.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     * @require Mapper.isVid(contact) && Category.isPerson(contact) : "The second number has to denote a person.";
     */
    public static void addContact(Client client, long vid, long contact, int context) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The first number has to denote a VID.";
        assert Mapper.isVid(contact) && Category.isPerson(contact) : "The second number has to denote a person.";
        
        Block[] elements = new Block[]{new StringWrapper(Mapper.getIdentifier(contact)).toBlock(), new Int32Wrapper(context).toBlock()};
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.add.contact@virtualid.ch", new TupleWrapper(elements).toBlock());
        new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
    }
    
    /**
     * Returns the restrictions of the given client at the given VID.
     * 
     * @param client the client whose restrictions are to be returned.
     * @param vid the VID at which the given client is authorized.
     * @return the restrictions of the given client at the given VID or null if this client is not authorized.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     */
    public static Restrictions getRestrictions(Client client, long vid) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.get.restrictions.client@virtualid.ch", Block.EMPTY);
        Packet response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
        
        return new Restrictions(responsgetContentsnt().getElement());
    }
    
    /**
     * Returns the authorization of the given client at the given VID.
     * 
     * @param client the client whose authorization is to be returned.
     * @param vid the VID at which the given client is authorized.
     * @return the authorization of the given client at the given VID.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     */
    public static Permissions getAuthorization(Client client, long vid) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.get.authorization.client@virtualid.ch", Block.EMPTY);
        Packet response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
        
        return new Permissions(respongetContentsent().getElement());
    }
    
    /**
     * Retrieves all less powerful clients of the given VID.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param identity the VID from which all less powerful clients are to be retrieved.
     * @return a list of all less powerful clients, with each block being of type {@code client@virtualid.ch}.
     */
    public List<Block> getClients(@Nonnull NonHostIdentity identity) throws Exception {
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.list.client@virtualid.ch", Block.EMPTY);
        Response response = new ClientRequest(content, identity.getAddress(), -1, getSecret()).send();
        
        return new ListWrapper(response.getContent().getElement()).getElements();
    }
    
    /**
     * Removes the client given by the commitment from the given VID.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID from which the client is to be removed.
     * @param commitment the commitment of the client which is to be removed.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     * @require commitment != null : "The commitment is not null.";
     */
    public static void removeClient(Client client, long vid, BigInteger commitment) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        assert commitment != null : "The commitment is not null.";
        
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.remove.client@virtualid.ch", new IntegerWrapper(commitment).toBlock());
        new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
    }
    
    /**
     * Authorizes the client given by the commitment at the given VID with the given authorization.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID at which the client is to be authorized.
     * @param commitment the commitment of the client which is to be authorized.
     * @param restrictions the new restrictions of the client given by the commitment.
     * @param authorization the new authorization of the client given by the commitment.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     * @require commitment != null : "The commitment is not null.";
     * @require restrictions != null : "The restrictions is not null.";
     * @require authorization != null : "The authorization is not null.";
     */
    public static void authorizeClient(Client client, long vid, BigInteger commitment, Restrictions restrictions, Permissions authorization) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        assert commitment != null : "The commitment is not null.";
        assert restrictions != null : "The restrictions is not null.";
        assert authorization != null : "The authorization is not null.";
        
        Block[] elements = new Block[] {new IntegerWrapper(commitment).toBlock(), restrictions.toBlock(), authorization.toBlock()};
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.authorize.client@virtualid.ch", new TupleWrapper(elements).toBlock());
        new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
    }
    
    /**
     * Accredits the given client at the given VID with the given name.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param vid the VID at which the given client is to be accredited.
     * @param name the name with which the client is accredited.
     * @param preference the preference with which the client is accredited.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(vid) : "The value has to denote a VID.";
     * @require name != null : "The name is not null.";
     * @require preference != null : "The preference is not null.";
     */
    public static void accreditClient(Client client, long vid, String name, Permissions preference) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(vid) : "The value has to denote a VID.";
        assert name != null : "The name is not null.";
        assert preference != null : "The preference is not null.";
        
        Block[] elements = new Block[] {new StringWrapper(name).toBlock(), preference.toBlock()};
        SelfcontainedWrapper content = new SelfcontainedWrapper("request.accredit.client@virtualid.ch", new TupleWrapper(elements).toBlock());
        Packet response = new Packet(content, Mapper.getIdentifier(Mapper.getHost(vid)), new SymmetricKey(), Mapper.getIdentifier(vid), 0, client.getSecret()).send();
        client.setTimeOfLastRequest(vid, respongetSignaturesure().getTime());
    }
    
    /**
     * Obtains a credential for the given client on behalf of the given requester at the given issuer for the given authorization.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param requester the VID of a person on behalf of which the credential is to be obtained.
     * @param issuer the VID of a non-host at which the credential is to be obtained.
     * @param randomizedAuthorization the desired authorization in randomized form.
     * @return a credential for the given client on behalf of the given requester at the given issuer.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
     */
    public static ClientCredential obtainCredential(Client client, long requester, long issuer, RandomizedAuthorization randomizedAuthorization) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
        assert randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
        
        PublicKey publicKey = new PublicKey(Request.getAttributeNotNullUnwrapped(Mapper.getHost(issuer), Vid.HOST_PUBLIC_KEY));
        Group group = publicKey.getCompositeGroup();
        
        if (issuer == requester) {
            SelfcontainedWrapper content = new SelfcontainedWrapper("request.credential.client@virtualid.ch", randomizedAuthorization.toBlock());
            Packet request = new Packet(content, Mapper.getIdentifier(Mapper.getHost(issuer)), new SymmetricKey(), Mapper.getIdentifier(issuer), 0, client.getSecret());
            Packet response = request.send();
            Block[] elements = new TupleWrapper(respogetContentstent().getElement()).getElementsNotNull(3);
            
            String identifier = respogetSignaturesture().getIdentifier();
            long time = reqgetSignaturesature().getSignatureTimeRoundedDown();
            Restrictions restrictions = Request.getRestrictions(client, issuer);
            Element c = group.getElement(elements[0]);
            Exponent e = group.getExponent(elements[1]);
            Exponent b = group.getExponent(BigInteger.ZERO);
            Exponent u = group.getExponent(client.getSecret());
            Exponent i = group.getExponent(elements[2]);
            Exponent v = group.getExponent(restrictions.toBlock().getHash());
            
            return new Credential(publicKey, identifier, time, randomizedAuthorization, null, restrictions, c, e, b, u, i, v);
        } else {
            throw new UnsupportedOperationException("Credentials for attribute-based access control are not yet supported!");
        }
    }
    
}
