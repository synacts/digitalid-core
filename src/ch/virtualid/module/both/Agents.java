package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.query.internal.AgentsQuery;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link Agent agents} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
 */
public final class Agents implements BothModule {
    
    static { CoreService.SERVICE.add(new Agents()); }
    
    /**
     * Creates the table which is referenced for the given site.
     * 
     * @param site the site for which the reference table is created.
     */
    public static void createReferenceTable(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, client BOOLEAN NOT NULL, removed BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (entity, agent), FOREIGN KEY (entity) " + site.getReference() + ")");
        }
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_permission (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, writing BOOLEAN NOT NULL, PRIMARY KEY (entity, agent, type), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (type) " + site.getReference() + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_restrictions (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, client BOOLEAN NOT NULL, role BOOLEAN NOT NULL, writing BOOLEAN NOT NULL, context " + Context.FORMAT + ", contact " + Mapper.FORMAT + ", PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (contact) " + site.getReference() + ")");
            Mapper.addReference(site + "agent_restrictions", "contact");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_order (entity " + Entity.FORMAT + " NOT NULL, stronger " + Agent.FORMAT + " NOT NULL, weaker " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, stronger, weaker), FOREIGN KEY (entity, stronger) " + Agent.getReference(site) + ", FOREIGN KEY (entity, weaker) " + Agent.getReference(site) + ")");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "client (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, host " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + Database.getConfiguration().BLOB() + " NOT NULL, name VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", icon " + Database.getConfiguration().BLOB() + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (host) " + site.getReference() + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "outgoing_role (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (relation) " + site.getReference() + ", FOREIGN KEY (entity, context) " + Context.getReference(site) + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "incoming_role (entity " + Entity.FORMAT + " NOT NULL, issuer " + Mapper.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, issuer, relation), FOREIGN KEY (entity) " + site.getReference() + ", FOREIGN KEY (issuer) " + site.getReference() + ", FOREIGN KEY (relation) " + site.getReference() + ")");
            Mapper.addReference(site + "incoming_role", "issuer", "entity", "issuer", "relation");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Mapper.removeReference(site + "incoming_role", "issuer", "entity", "issuer", "relation");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "incoming_role");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "outgoing_role");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "client");
            
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_order");
            
            Mapper.removeReference(site + "agent_restrictions", "contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_restrictions");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_permission");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.agents.module@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("agents.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE, entries.freeze()).toBlock();
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.create("entry.agents.state@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("agents.state@virtualid.ch").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve the entries of the given entity from the database table(s).
        }
        return new ListWrapper(STATE, entries.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add the entries of the given entity to the database table(s).
        }
    }
    
    @Override
    public void removeState(@Nonnull Entity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    @Pure
    @Override
    public @Nonnull AgentsQuery getInternalQuery(@Nonnull Role role) {
        return new AgentsQuery(role);
    }
    
    
//    /**
//     * Redetermines which agents are stronger and weaker than this agent.
//     */
//    public void redetermineAgents() throws SQLException {
//        // TODO!
//    }
//    
//    /**
//     * Adds the client with the given commitment to the given identity and returns the generated agent.
//     * 
//     * @param identity the identity to which the client is to be added.
//     * @param commitment the commitment of the client to be added.
//     * @param name the name of the client to be added.
//     * @return the generated agent for this client.
//     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
//     */
//    static @Nonnull ClientAgent addClientAgent(@Nonnull NonHostIdentity identity, @Nonnull Commitment commitment, @Nonnull String name) throws SQLException {
//        assert name.length() <= 50 : "The client name may have at most 50 characters.";
//        
//        long number = Database.executeInsert(connection, "INSERT INTO authorization (identity) VALUES (" + identity + ")");
//        @Nonnull String sql = "INSERT INTO client (agent, host, time, commitment, name) VALUES (?, ?, ?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setLong(1, number);
//            preparedStatement.setLong(2, commitment.getHost().getNumber());
//            preparedStatement.setLong(3, commitment.getTime());
//            preparedStatement.setBytes(4, commitment.getValue().toByteArray());
//            preparedStatement.setString(5, name);
//            preparedStatement.executeUpdate();
//        }
//        
//        return new ClientAgent(connection, identity, number, commitment, name);
//    }
//    
//    /**
//     * Returns the client with the given commitment at the given identity or null if no such client is found.
//     * 
//     * @param entity the identity whose client is to be returned.
//     * @param commitment the commitment of the client which is to be returned.
//     * 
//     * @return the client with the given commitment at the given identity or null if no such client is found.
//     */
    public static @Nullable ClientAgent getClientAgent(@Nonnull Entity entity, @Nonnull Commitment commitment) throws SQLException {
//        @Nonnull String sql = "SELECT authorization.agent, client.name FROM authorization JOIN client ON authorization.agent = client.agent WHERE authorization.identity = ? AND NOT authorization.removed AND client.host = ? AND client.time = ? AND client.commitment = ?";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(sql)) {
//            preparedStatement.setLong(1, entity.getNumber());
//            preparedStatement.setLong(2, commitment.getHost().getNumber());
//            preparedStatement.setLong(3, commitment.getTime());
//            preparedStatement.setBytes(4, commitment.getValue().toByteArray());
//            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery()) {
//                if (resultSet.next()) return new ClientAgent(entity, resultSet.getLong(1), commitment, resultSet.getString(2));
//                else return null;
//            }
//        }
        return null;
    }
//    
//    /**
//     * Adds the outgoing role with the given relation, context and visibility to the given identity and returns the generated agent.
//     * 
//     * @param identity the identity to which the outgoing role is to be added.
//     * @param relation the relation between the issuing and the receiving identity.
//     * @param context the context to which the outgoing role is to be assigned.
//     * @return the generated agent for this outgoing role.
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    static @Nonnull OutgoingRole addOutgoingRole(@Nonnull NonHostIdentity identity, @Nonnull SemanticType relation, @Nonnull Context context) throws SQLException {
//        assert relation.isRoleType() : "The relation is a role type.";
//        
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            long number = Database.executeInsert(statement, "INSERT INTO authorization (identity) VALUES (" + identity + ")");
//            statement.executeUpdate("INSERT INTO outgoing_role (agent, relation, context) VALUES (" + number + ", " + relation + ", " + context + ")");
//            return new OutgoingRole(connection, number, identity, relation, context);
//        } catch (@Nonnull SQLException exception) {
//            if (relation.hasBeenMerged()) return addOutgoingRole(connection, identity, relation, context);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Returns the outgoing role with the given relation at the given identity or null if no such role is found.
//     * 
//     * @param entity the identity whose outgoing role is to be returned.
//     * @param relation the relation between the issuing and the receiving identity.
//     * 
//     * @return the outgoing role with the given relation at the given identity or null if no such role is found.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
    public static @Nullable OutgoingRole getOutgoingRole(@Nonnull Entity entity, @Nonnull SemanticType relation, boolean restrictable) throws SQLException {
//        assert relation.isRoleType() : "The relation is a role type.";
//        
//        // TODO: Create the OutgoingRole according to the restrictable parameter.
//        
//        @Nonnull String sql = "SELECT authorization.agent, outgoing_role.context FROM authorization JOIN outgoing_role ON authorization.agent = outgoing_role.agent WHERE authorization.identity = " + entity + " AND NOT authorization.removed AND outgoing_role.relation = " + relation;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
//            if (resultSet.next()) {
//                return new OutgoingRole(connection, resultSet.getLong(1), entity, relation, new Context(resultSet.getLong(2)));
//            } else {
//                if (relation.hasBeenMerged()) return getOutgoingRole(connection, entity, relation);
//                else return null;
//            }
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
        return null;
    }
//    
//    /**
//     * Removes the given agent.
//     * 
//     * @param agent the agent to be removed.
//     */
//    public static void removeAgent(@Nonnull Agent agent) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("UPDATE authorization SET removed = TRUE WHERE agent = " + agent);
//        }
//    }
//    
//    /**
//     * Adds the incoming role with the given issuer and relation to the given identity and returns the generated authorization.
//     * 
//     * @param identity the identity to which the incoming role is to be added.
//     * @param issuer the issuer of the incoming role.
//     * @param relation the relation of the incoming role.
//     * @return the generated authorization for this incoming role.
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    static @Nonnull IncomingRole addIncomingRole(@Nonnull NonHostIdentity identity, @Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation) throws SQLException {
//        assert relation.isRoleType() : "The relation is a role type.";
//        
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            long number = Database.executeInsert(statement, "INSERT INTO authorization (identity) VALUES (" + identity + ")");
//            statement.executeUpdate("INSERT INTO incoming_role (agent, issuer, relation) VALUES (" + number + ", " + issuer + ", " + relation + ")");
//            return new IncomingRole(connection, number, identity, issuer, relation);
//        } catch (@Nonnull SQLException exception) {
//            if (issuer.hasBeenMerged() || relation.hasBeenMerged()) return addIncomingRole(connection, identity, issuer, relation);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Returns the incoming role with the given issuer and relation at the given identity or null if no such role is found.
//     * 
//     * @param identity the identity whose incoming role is to be returned.
//     * @param issuer the issuer of the incoming role which is to be returned.
//     * @param relation the relation of the incoming role which is to be returned.
//     * @return the incoming role with the given issuer and relation at the given identity or null if no such role is found.
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    static @Nullable IncomingRole getIncomingRole(@Nonnull NonHostIdentity identity, @Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation) throws SQLException {
//        assert relation.isRoleType() : "The relation is a role type.";
//        
//        @Nonnull String sql = "SELECT authorization.agent FROM authorization JOIN incoming_role ON authorization.agent = incoming_role.agent WHERE authorization.identity = " + identity + " AND NOT authorization.removed AND incoming_role.issuer = " + issuer + " AND incoming_role.relation = " + relation;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
//            if (resultSet.next()) {
//                return new IncomingRole(connection, resultSet.getLong(1), identity, issuer, relation);
//            } else {
//                if (issuer.hasBeenMerged() || relation.hasBeenMerged()) return getIncomingRole(connection, identity, issuer, relation);
//                else return null;
//            }
//        }
//    }
//    
//    /**
//     * Returns the incoming roles of the given identity restricted by the given agent.
//     * 
//     * @param identity the identity whose incoming roles are to be returned.
//     * @param agent the agent with which to restrict the incoming roles.
//     * @return the incoming roles of the given identity restricted by the given agent.
//     * @require agent.getRestrictions() != null : "The restrictions of the agent is not null.";
//     */
//    static @Nonnull Set<IncomingRole> getIncomingRoles(@Nonnull NonHostIdentity identity, @Nonnull Agent agent) throws SQLException {
//        assert agent.getRestrictions() != null : "The restrictions of the agent is not null.";
//        
//        @Nonnull String sql = "SELECT authorization.agent, issuer.identity, issuer.category, issuer.address, relation.identity, relation.category, relation.address FROM authorization JOIN incoming_role ON authorization.agent = incoming_role.agent JOIN general_identity AS issuer ON incoming_role.issuer = general_identity.identity JOIN general_identity AS relation ON incoming_role.relation = general_identity.identity WHERE authorization.identity = " + identity;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
//            @Nonnull Set<IncomingRole> incomingRoles = new LinkedHashSet<IncomingRole>();
//            while (resultSet.next()) {
//                @Nonnull NonHostIdentity issuer = Identity.create(Category.get(resultSet.getByte(3)), resultSet.getLong(2), new NonHostIdentifier(resultSet.getString(4))).toNonHostIdentity();
//                @Nonnull SemanticType relation = Identity.create(Category.get(resultSet.getByte(6)), resultSet.getLong(5), new NonHostIdentifier(resultSet.getString(7))).toSemanticType();
//                @Nonnull IncomingRole incomingRole = new IncomingRole(connection, resultSet.getLong(1), identity, issuer, relation);
//                incomingRole.restrictTo(agent);
//                incomingRoles.add(incomingRole);
//            }
//            return incomingRoles;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Removes the given incoming role.
//     * 
//     * @param incomingRole the incoming role to be removed.
//     */
//    public static void removeIncomingRole(@Nonnull IncomingRole incomingRole) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("DELETE FROM authorization WHERE agent = " + incomingRole);
//        }
//    }
//    
//    
//    /**
//     * Returns the restrictions of the given authorization or null if not yet set.
//     * 
//     * @param authorization the authorization whose restrictions are to be returned.
//     * @return the restrictions of the given authorization or null if not yet set.
//     */
//    public static @Nullable Restrictions getRestrictions(@Nonnull Authorization authorization) throws SQLException {
//        @Nonnull String query = "SELECT client, context, writing, history, role FROM authorization_restrictions WHERE agent = " + authorization;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) return new Restrictions(resultSet.getBoolean(1), new Context(resultSet.getLong(2)), resultSet.getBoolean(3), resultSet.getLong(4), resultSet.getBoolean(5));
//            else return null;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Sets (respectively replaces) the restrictions of the given authorization.
//     * 
//     * @param authorization the authorization whose restrictions are to be set.
//     * @param restrictions the restrictions to be set for the given authorization.
//     */
//    public static void setRestrictions(@Nonnull Authorization authorization, @Nonnull Restrictions restrictions) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("REPLACE INTO authorization_restrictions (agent, client, context, writing, history, role) VALUES (" + authorization + ", " + restrictions.isClient() + ", " + restrictions.getContext() + ", " + restrictions.isWriting() + ", " + restrictions.getHistory() + ", " + restrictions.isRole() + ")");
//        }
//    }
//    
//    /**
//     * Returns the permissions (or preferences) of the given authorization.
//     * 
//     * @param agent the authorization whose permissions (or preferences) are to be returned.
//     * @param preference whether the preferences or the actual permissions are to be returned.
//     * @return the permissions (or preferences) of the given authorization.
//     */
//    public static @Nonnull AgentPermissions getPermissions(@Nonnull Agent agent) throws SQLException {
//        @Nonnull String query = "SELECT general_identity.identity, general_identity.category, general_identity.address, authorization_permission.writing FROM authorization_permission JOIN general_identity ON authorization_permission.type = general_identity.identity WHERE agent = " + agent + " AND preference = " + preference;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//           AgentPermissionsermissions perAgentPermissionsnew Permissions();
//            while (resultSet.next()) {
//                long number = resultSet.getLong(1);
//                @Nonnull Category category = Category.get(resultSet.getByte(2));
//                @Nonnull NonHostIdentifier address = new NonHostIdentifier(resultSet.getString(3));
//                boolean writing = resultSet.getBoolean(4);
//                permissions.put(Identity.create(category, number, address).toSemanticType(), writing);
//            }
//            return permissions;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Adds the given permissions (or preferences) to the given authorization.
//     * Please note that a different writing property for a permission (or a preference) replaces the existing entry.
//     * 
//     * @param authorization the authorization to which the permissions (or preferences) are to be added.
//     * @param preference whether the preferences or the actual permissions are to be extended.
//     * @param permissions the permissions (or preferences) to be added to the given authorization.
//     */
//    public static void addPermissions(@Nonnull Authorization authorization, boolean pAgentPermissions@Nonnull Permissions permissions) throws SQLException {
//        @Nonnull String sql = "REPLACE INTO authorization_permission (agent, preference, type, writing) VALUES (" + authorization + ", " + preference + ", ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            for (@Nonnull SemanticType type : permissions.keySet()) {
//                preparedStatement.setLong(1, type.getNumber());
//                preparedStatement.setBoolean(2, permissions.get(type));
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        } catch (@Nonnull SQLException exception) {
//            boolean merged = false;
//            for (@Nonnull SemanticType type : permissions.keySet()) {
//                if (type.hasBeenMerged()) merged = true;
//            }
//            if (merged) addPermissions(connection, authorization, preference, permissions);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Sets the given permissions (or preferences) in the given authorization.
//     * 
//     * @param authorization the authorization whose permissions (or preferences) are to be set.
//     * @param preference whether the preferences or the actual permissions are to be set.
//     * @param permissions the permissions (or preferences) to be set in the given authorization.
//     */
//    public static void setPermissions(@Nonnull Authorization authorization, boolAgentPermissionsnce, @Nonnull Permissions permissions) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("DELETE FROM authorization_permission WHERE agent = " + authorization + " AND preference = " + preference);
//        }
//        
//        addPermissions(connection, authorization, preference, permissions);
//    }
//    
//    /**
//     * Removes the given permissions (or preferences) from the given authorization.
//     * Please note that the writing property of the permissions (or preferences) is ignored for this operation.
//     * 
//     * @param authorization the authorization whose permissions (or preferences) are to be removed.
//     * @param preference whether the preferences or the actual permissions are to be removed.
//     * @param permissions the permissions (or preferences) to be removed from the given authorization.
//     * @return the number of rows deleted from the database.
//     */
//    public static int removePermissions(@Nonnull Authorization authorization,AgentPermissionseference, @Nonnull Permissions permissions) throws SQLException {
//        @Nonnull String sql = "DELETE FROM authorization_permission WHERE agent = " + authorization + " AND preference = " + preference + " AND type = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            for (@Nonnull SemanticType type : permissions.keySet()) {
//                preparedStatement.setLong(1, type.getNumber());
//                preparedStatement.addBatch();
//            }
//            int[] updated = preparedStatement.executeBatch();
//            
//           AgentPermissions0;
//      AgentPermissionsull Permissions merged = new Permissions();
//            for (int i = 0; i < updated.length; i++) {
//                sum += updated[i];
//                if (updated[i] < 1) {
//                    @Nonnull SemanticType type = getElement(permissions.keySet(), i);
//                    if (type.hasBeenMerged()) merged.put(type, permissions.get(type));
//                }
//            }
//            if (!merged.isEmpty()) return sum + removePermissions(connection, authorization, preference, merged);
//            else return sum;
//        }
//    }
//    
//    
//    /**
//     * Returns the agents that are weaker than the given agent.
//     * 
//     * @param agent the agent whose weaker agents are to be returned.
//     * @return the agents that are weaker than the given agent.
//     * @require agent.getRestrictions() != null : "The restrictions of the agent is not null.";
//     */
//    public static @Nonnull Set<Agent> getAgents(@Nonnull Agent agent) throws SQLException {
//        assert agent.getRestrictions() != null : "The restrictions of the agent is not null.";
//        
//        @Nonnull String sql = "SELECT agent_order.weaker, identity.identity, identity.category, identity.address, host.identity, host.category, host.address, client.time, client.commitment, client.name, relation.identity, relation.category, relation.address, outgoing_role.context FROM agent_order JOIN authorization ON agent_order.weaker = authorization.agent JOIN general_identity AS identity ON authorization.identity = general_identity.identity LEFT JOIN (client JOIN general_identity AS host ON client.host = general_identity.identity) ON agent_order.weaker = client.agent LEFT JOIN (outgoing_role JOIN general_identity AS relation ON outgoing_role.relation = general_identity.identity) ON agent_order.weaker = outgoing_role.agent WHERE agent_order.stronger = " + agent + " AND NOT authorization.removed";
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
//            @Nonnull Set<Agent> agents = new LinkedHashSet<Agent>();
//            while (resultSet.next()) {
//                @Nonnull NonHostIdentity identity = Identity.create(Category.get(resultSet.getByte(3)), resultSet.getLong(2), new NonHostIdentifier(resultSet.getString(4))).toNonHostIdentity();
//                resultSet.getLong(5);
//                if (!resultSet.wasNull()) {
//                    @Nonnull HostIdentity host = Identity.create(Category.get(resultSet.getByte(6)), resultSet.getLong(5), new HostIdentifier(resultSet.getString(7))).toHostIdentity();
//                    @Nonnull Commitment commitment = new Commitment(host, resultSet.getLong(8), new BigInteger(resultSet.getBytes(9)));
//                    agents.add(new ClientAgent(connection, identity, resultSet.getLong(1), commitment, resultSet.getString(10)));
//                } else {
//                    @Nonnull SemanticType relation = Identity.create(Category.get(resultSet.getByte(12)), resultSet.getLong(11), new HostIdentifier(resultSet.getString(13))).toSemanticType();
//                    agents.add(new OutgoingRole(connection, resultSet.getLong(1), identity, relation, new Context(resultSet.getLong(14))));
//                }
//            }
//            return agents;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Redetermines which agents are stronger and weaker than the given agent.
//     * 
//     * @param agent the agent which is to be redetermined.
//     */
//    public static void redetermineAgents(@Nonnull Agent agent) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("DELETE FROM agent_order WHERE stronger = " + agent + " OR weaker = " + agent);
//            
//            // Determine the weaker agents.
//            @Nullable Restrictions restrictions = agent.getRestrictions();
//            if (restrictions != null) {
//                statement.executeUpdate("INSERT INTO agent_order (stronger, weaker) SELECT " + agent + ", authorization.agent FROM authorization LEFT JOIN authorization_restrictions AS restrictions USING (agent) WHERE authorization.identity = " + agent.getIdentity()
//                        + " AND (restrictions.agent IS NULL OR ("
//                            + "(NOT restrictions.client OR " + restrictions.isClient() + ") AND "
//                            + "(restrictions.context & " + restrictions.getContext().getMask() + " = " + restrictions.getContext() + ") AND "
//                            + "(NOT restrictions.writing OR " + restrictions.isWriting() + ") AND "
//                            + "(restrictions.history >= " + restrictions.getHistory() + ") AND "
//                            + "(NOT restrictions.role OR " + restrictions.isRole() + ")))"
//                        + " AND (restrictions.client" + (restrictions.isWriting() ? ""
//                            + " OR EXISTS (SELECT * FROM outgoing_role JOIN authorization_permission as permission ON outgoing_role.relation = permission.type OR permission.type = " + SemanticType.CLIENT_GENERAL_PERMISSION
//                                + " WHERE outgoing_role.agent = authorization.agent AND outgoing_role.context & " + restrictions.getContext().getMask() + " = " + restrictions.getContext()
//                                + " AND permission.agent = " + agent + " AND NOT permission.preference AND permission.writing)" : "")
//                        + ") AND NOT EXISTS (SELECT * FROM authorization_permission AS weaker LEFT JOIN authorization_permission AS stronger ON stronger.agent = " + agent + " AND NOT stronger.preference AND (weaker.type = stronger.type OR stronger.type = " + SemanticType.CLIENT_GENERAL_PERMISSION + ") AND (NOT weaker.writing OR stronger.writing) WHERE weaker.agent = authorization.agent AND NOT weaker.preference AND stronger.agent IS NULL)");
//            }
//            
//            // Determine the stronger agents.
//            int updated = statement.executeUpdate("INSERT INTO agent_order (stronger, weaker) SELECT authorization.agent, " + agent + " FROM authorization JOIN authorization_restrictions AS restrictions USING (agent) WHERE authorization.identity = " + agent.getIdentity()
//                    + " AND ("
//                        + "(NOT " + agent.isClient() + " OR restrictions.client)" + (restrictions == null ? "" : " AND "
//                        + "(restrictions.context IN (" + restrictions.getContext().getSupercontextsAsString() + ")) AND "
//                        + "(NOT " + restrictions.isWriting() + " OR restrictions.writing) AND "
//                        + "(" + restrictions.getHistory() + " >= restrictions.history) AND "
//                        + "(NOT " + restrictions.isRole() + " OR restrictions.role)")
//                     + ") AND (restrictions.client OR EXISTS (SELECT * FROM outgoing_role WHERE outgoing_role.agent = authorization.agent))" 
//                    + (agent instanceof OutgoingRole ? " AND (restrictions.writing AND restrictions.context IN (" + ((OutgoingRole) agent).getContext().getSupercontextsAsString() + ")"
//                        + " AND EXISTS (SELECT * FROM authorization_permission as permission WHERE permission.agent = authorization.agent AND NOT permission.preference AND permission.writing AND (permission.type = " + ((OutgoingRole) agent).getRelation() + " OR permission.type = " + SemanticType.CLIENT_GENERAL_PERMISSION + ")))" : "")
//                    + " AND NOT EXISTS (SELECT * FROM authorization_permission AS weaker LEFT JOIN authorization_permission AS stronger ON stronger.agent = authorization.agent AND NOT stronger.preference AND (weaker.type = stronger.type OR stronger.type = " + SemanticType.CLIENT_GENERAL_PERMISSION + ") AND (NOT weaker.writing OR stronger.writing) WHERE weaker.agent = " + agent + " AND NOT weaker.preference AND stronger.agent IS NULL)");
//            
//            if (updated == 0 && agent instanceof OutgoingRole && ((OutgoingRole) agent).getRelation().hasBeenMerged()) redetermineAgents(connection, agent);
//        }
//    }
//    
//    
//    /**
//     * Sets the commitment of the given client agent to the given value.
//     * 
//     * @param clientAgent the client agent whose commitment is to be set.
//     * @param commitment the commitment to set for the given client agent.
//     */
//    public static void setClientCommitment(@Nonnull ClientAgent clientAgent, @Nonnull Commitment commitment) throws SQLException {
//        @Nonnull String sql = "UPDATE client SET host = ?, time = ?, commitment = ? WHERE agent = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setLong(1, commitment.getHost().getNumber());
//            preparedStatement.setLong(2, commitment.getTime());
//            preparedStatement.setBytes(3, commitment.getValue().toByteArray());
//            preparedStatement.setLong(4, clientAgent.getNumber());
//            preparedStatement.executeUpdate();
//        }
//    }
//    
//    /**
//     * Sets the name of the given client agent to the given string.
//     * 
//     * @param clientAgent the client agent whose name is to be set.
//     * @param name the name to set for the given client agent.
//     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
//     */
//    public static void setClientName(@Nonnull ClientAgent clientAgent, @Nonnull String name) throws SQLException {
//        assert name.length() <= 50 : "The client name may have at most 50 characters.";
//        
//        @Nonnull String sql = "UPDATE client SET name = ? WHERE agent = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setString(1, name);
//            preparedStatement.setLong(2, clientAgent.getNumber());
//            preparedStatement.executeUpdate();
//        }
//    }
//    
//    /**
//     * Sets the context of the given outgoing role to the given value.
//     * 
//     * @param outgoingRole the outgoing role whose context is to be set.
//     * @param context the context to set for the given outgoing role.
//     */
//    public static void setOutgoingRoleContext(@Nonnull OutgoingRole outgoingRole, @Nonnull Context context) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("UPDATE outgoing_role SET context = " + context + " WHERE agent = " + outgoingRole);
//        }
//    }
    
}
