package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.query.internal.AgentsQuery;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableSet;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.ListWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link Agent agents} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.2
 */
public final class Agents implements BothModule {
    
    /**
     * Initializes this class.
     */
    static void initialize() {}
    
    static { Contexts.initialize(); }
    
    public static final Agents MODULE = new Agents();
    
    static { CoreService.SERVICE.add(MODULE); }
    
    /**
     * Creates the table which is referenced for the given site.
     * 
     * @param site the site for which the reference table is created.
     */
    public static void createReferenceTable(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, client BOOLEAN NOT NULL, removed BOOLEAN NOT NULL DEFAULT TRUE, PRIMARY KEY (entity, agent), FOREIGN KEY (entity) " + site.getReference() + ")");
        }
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_permission (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, writing BOOLEAN NOT NULL, PRIMARY KEY (entity, agent, type), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (type) " + site.getReference() + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_restrictions (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Restrictions.FORMAT_NOT_NULL + ", PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Restrictions.getForeignKeys(site) + ")");
            Mapper.addReference(site + "agent_restrictions", "contact");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_order (entity " + Entity.FORMAT + " NOT NULL, stronger " + Agent.FORMAT + " NOT NULL, weaker " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, stronger, weaker), FOREIGN KEY (entity, stronger) " + Agent.getReference(site) + ", FOREIGN KEY (entity, weaker) " + Agent.getReference(site) + ")");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "client (entity " + Entity.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Commitment.FORMAT + ", name VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", icon " + Image.FORMAT + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Commitment.getForeignKeys(site) + ")");
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
     * Stores the semantic type {@code entry.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_MODULE_ENTRY = SemanticType.create("entry.agent.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Agent.NUMBER, Agent.CLIENT, Agent.REMOVED);
    
    /**
     * Stores the semantic type {@code table.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_MODULE_TABLE = SemanticType.create("table.agent.agents.module@virtualid.ch").load(ListWrapper.TYPE, AGENT_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.permission.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_MODULE_ENTRY = SemanticType.create("entry.permission.agent.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Agent.NUMBER, AgentPermissions.ATTRIBUTE_TYPE, AgentPermissions.WRITING);
    
    /**
     * Stores the semantic type {@code table.permission.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_MODULE_TABLE = SemanticType.create("table.permission.agent.agents.module@virtualid.ch").load(ListWrapper.TYPE, AGENT_PERMISSION_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.restrictions.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_MODULE_ENTRY = SemanticType.create("entry.restrictions.agent.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Agent.NUMBER, Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code table.restrictions.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_MODULE_TABLE = SemanticType.create("table.restrictions.agent.agents.module@virtualid.ch").load(ListWrapper.TYPE, AGENT_RESTRICTIONS_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code stronger.order.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_ORDER_STRONGER = SemanticType.create("stronger.order.agent.agents.module@virtualid.ch").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code weaker.order.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_ORDER_WEAKER = SemanticType.create("weaker.order.agent.agents.module@virtualid.ch").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code entry.order.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_ORDER_MODULE_ENTRY = SemanticType.create("entry.order.agent.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, AGENT_ORDER_STRONGER, AGENT_ORDER_WEAKER);
    
    /**
     * Stores the semantic type {@code table.order.agent.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_ORDER_MODULE_TABLE = SemanticType.create("table.order.agent.agents.module@virtualid.ch").load(ListWrapper.TYPE, AGENT_ORDER_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.client.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT_MODULE_ENTRY = SemanticType.create("entry.client.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Agent.NUMBER, Commitment.TYPE, Client.NAME, Client.ICON);
    
    /**
     * Stores the semantic type {@code table.client.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT_MODULE_TABLE = SemanticType.create("table.client.agents.module@virtualid.ch").load(ListWrapper.TYPE, CLIENT_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.outgoing.role.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_MODULE_ENTRY = SemanticType.create("entry.outgoing.role.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Agent.NUMBER, Role.RELATION, Context.TYPE);
    
    /**
     * Stores the semantic type {@code table.outgoing.role.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_MODULE_TABLE = SemanticType.create("table.outgoing.role.agents.module@virtualid.ch").load(ListWrapper.TYPE, OUTGOING_ROLE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.incoming.role.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_MODULE_ENTRY = SemanticType.create("entry.incoming.role.agents.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Role.ISSUER, Role.RELATION, Role.AGENT);
    
    /**
     * Stores the semantic type {@code table.incoming.role.agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_MODULE_TABLE = SemanticType.create("table.incoming.role.agents.module@virtualid.ch").load(ListWrapper.TYPE, INCOMING_ROLE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code agents.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("agents.module@virtualid.ch").load(TupleWrapper.TYPE, AGENT_MODULE_TABLE, AGENT_PERMISSION_MODULE_TABLE, AGENT_RESTRICTIONS_MODULE_TABLE, AGENT_ORDER_MODULE_TABLE, CLIENT_MODULE_TABLE, INCOMING_ROLE_MODULE_TABLE, OUTGOING_ROLE_MODULE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<Block>(7);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, client, removed FROM " + host + "agent")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final boolean client = resultSet.getBoolean(3);
                    final boolean removed = resultSet.getBoolean(4);
                    entries.add(new TupleWrapper(AGENT_MODULE_ENTRY, identity, new Int64Wrapper(Agent.NUMBER, number), new BooleanWrapper(Agent.CLIENT, client), new BooleanWrapper(Agent.REMOVED, removed)).toBlock());
                }
                tables.set(0, new ListWrapper(AGENT_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, type, writing FROM " + host + "agent_permission")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 3);
                    final boolean writing = resultSet.getBoolean(4);
                    entries.add(new TupleWrapper(AGENT_PERMISSION_MODULE_ENTRY, identity, new Int64Wrapper(Agent.NUMBER, number), type.toBlockable(AgentPermissions.ATTRIBUTE_TYPE), new BooleanWrapper(AgentPermissions.WRITING, writing)).toBlock());
                }
                tables.set(1, new ListWrapper(AGENT_PERMISSION_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, " + Restrictions.COLUMNS + " FROM " + host + "agent_restrictions")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Account account = Account.get(host, resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Restrictions restrictions = Restrictions.get(account, resultSet, 3);
                    entries.add(new TupleWrapper(AGENT_RESTRICTIONS_MODULE_ENTRY, account.getIdentity(), new Int64Wrapper(Agent.NUMBER, number), restrictions).toBlock());
                }
                tables.set(2, new ListWrapper(AGENT_RESTRICTIONS_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, stronger, weaker FROM " + host + "agent_order")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final long stronger = resultSet.getLong(2);
                    final long weaker = resultSet.getLong(3);
                    entries.add(new TupleWrapper(AGENT_ORDER_MODULE_ENTRY, identity, new Int64Wrapper(AGENT_ORDER_STRONGER, stronger), new Int64Wrapper(AGENT_ORDER_WEAKER, weaker)).toBlock());
                }
                tables.set(3, new ListWrapper(AGENT_ORDER_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, " + Commitment.COLUMNS + ", name, icon FROM " + host + "client")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Commitment commitment = Commitment.get(resultSet, 3);
                    final @Nonnull String name = resultSet.getString(6);
                    final @Nonnull Image icon = Image.get(resultSet, 7);
                    entries.add(new TupleWrapper(CLIENT_MODULE_ENTRY, identity, new Int64Wrapper(Agent.NUMBER, number), commitment, new StringWrapper(Client.NAME, name), icon.toBlock().setType(Client.ICON).toBlockable()).toBlock());
                }
                tables.set(4, new ListWrapper(CLIENT_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, relation, context FROM " + host + "outgoing_role")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Account account = Account.get(host, resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Identity relation = IdentityClass.getNotNull(resultSet, 3);
                    final @Nonnull Context context = Context.getNotNull(account, resultSet, 4);
                    entries.add(new TupleWrapper(OUTGOING_ROLE_MODULE_ENTRY, account.getIdentity(), new Int64Wrapper(Agent.NUMBER, number), relation.toBlockable(Role.RELATION), context).toBlock());
                }
                tables.set(5, new ListWrapper(OUTGOING_ROLE_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, issuer, relation, agent FROM " + host + "incoming_role")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity issuer = IdentityClass.getNotNull(resultSet, 2);
                    final @Nonnull Identity relation = IdentityClass.getNotNull(resultSet, 3);
                    final long number = resultSet.getLong(4);
                    entries.add(new TupleWrapper(INCOMING_ROLE_MODULE_ENTRY, identity, issuer.toBlockable(Role.ISSUER), relation.toBlockable(Role.RELATION), new Int64Wrapper(Role.AGENT, number)).toBlock());
                }
                tables.set(6, new ListWrapper(INCOMING_ROLE_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(MODULE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyArray<Block> tables = new TupleWrapper(block).getElementsNotNull(7);
        final @Nonnull String prefix = "INSERT INTO " + host;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent (entity, agent, client, removed) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNotNull(2)).getValue());
                preparedStatement.setBoolean(4, new BooleanWrapper(elements.getNotNull(3)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent_permission (entity, agent, type, writing) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().set(preparedStatement, 3);
                preparedStatement.setBoolean(4, new BooleanWrapper(elements.getNotNull(3)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(2)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                final @Nonnull InternalNonHostIdentity identity = IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity();
                identity.set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                new Restrictions(Account.get(host, identity), elements.getNotNull(2)).set(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent_order (entity, stronger, weaker) VALUES (?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(3)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                preparedStatement.setLong(3, new Int64Wrapper(elements.getNotNull(2)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "client (entity, agent, " + Commitment.COLUMNS + ", name, icon) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(4)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(5);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                new Commitment(elements.getNotNull(2)).set(preparedStatement, 3);
                preparedStatement.setString(6, new StringWrapper(elements.getNotNull(3)).getString());
                new Image(elements.getNotNull(4)).set(preparedStatement, 7);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "outgoing_role (entity, agent, relation, context) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(5)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                final @Nonnull InternalNonHostIdentity identity = IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity();
                identity.set(preparedStatement, 1);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(1)).getValue());
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().set(preparedStatement, 3);
                Context.get(Account.get(host, identity), elements.getNotNull(3)).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "incoming_role (entity, issuer, relation, agent) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(6)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 1);
                IdentityClass.create(elements.getNotNull(1)).toInternalNonHostIdentity().set(preparedStatement, 2);
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().set(preparedStatement, 3);
                preparedStatement.setLong(4, new Int64Wrapper(elements.getNotNull(3)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_STATE_ENTRY = SemanticType.create("entry.agent.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Agent.NUMBER, Agent.CLIENT, Agent.REMOVED);
    
    /**
     * Stores the semantic type {@code table.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_STATE_TABLE = SemanticType.create("table.agent.agents.state@virtualid.ch").load(ListWrapper.TYPE, AGENT_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.permission.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_STATE_ENTRY = SemanticType.create("entry.permission.agent.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Agent.NUMBER, AgentPermissions.ATTRIBUTE_TYPE, AgentPermissions.WRITING);
    
    /**
     * Stores the semantic type {@code table.permission.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_STATE_TABLE = SemanticType.create("table.permission.agent.agents.state@virtualid.ch").load(ListWrapper.TYPE, AGENT_PERMISSION_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.restrictions.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_STATE_ENTRY = SemanticType.create("entry.restrictions.agent.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Agent.NUMBER, Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code table.restrictions.agent.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_STATE_TABLE = SemanticType.create("table.restrictions.agent.agents.state@virtualid.ch").load(ListWrapper.TYPE, AGENT_RESTRICTIONS_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.client.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT_STATE_ENTRY = SemanticType.create("entry.client.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Agent.NUMBER, Commitment.TYPE, Client.NAME, Client.ICON);
    
    /**
     * Stores the semantic type {@code table.client.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT_STATE_TABLE = SemanticType.create("table.client.agents.state@virtualid.ch").load(ListWrapper.TYPE, CLIENT_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.outgoing.role.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_STATE_ENTRY = SemanticType.create("entry.outgoing.role.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Agent.NUMBER, Role.RELATION, Context.TYPE);
    
    /**
     * Stores the semantic type {@code table.outgoing.role.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_STATE_TABLE = SemanticType.create("table.outgoing.role.agents.state@virtualid.ch").load(ListWrapper.TYPE, OUTGOING_ROLE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.incoming.role.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_STATE_ENTRY = SemanticType.create("entry.incoming.role.agents.state@virtualid.ch").load(TupleWrapper.TYPE, Role.ISSUER, Role.RELATION, Role.AGENT);
    
    /**
     * Stores the semantic type {@code table.incoming.role.agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_STATE_TABLE = SemanticType.create("table.incoming.role.agents.state@virtualid.ch").load(ListWrapper.TYPE, INCOMING_ROLE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code agents.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("agents.state@virtualid.ch").load(TupleWrapper.TYPE, AGENT_STATE_TABLE, AGENT_PERMISSION_STATE_TABLE, AGENT_RESTRICTIONS_STATE_TABLE, CLIENT_STATE_TABLE, INCOMING_ROLE_STATE_TABLE, OUTGOING_ROLE_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String from = " FROM " + site + "agent_order o, " + site;
        final @Nonnull String where = " t WHERE o.entity = " + entity + " AND o.stronger = " + agent + " AND o.entity = t.entity AND o.weaker = t.agent";
        
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<Block>(6);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, client, removed" + from + "agent" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final boolean client = resultSet.getBoolean(2);
                    final boolean removed = resultSet.getBoolean(3);
                    entries.add(new TupleWrapper(AGENT_STATE_ENTRY, new Int64Wrapper(Agent.NUMBER, number), new BooleanWrapper(Agent.CLIENT, client), new BooleanWrapper(Agent.REMOVED, removed)).toBlock());
                }
                tables.set(0, new ListWrapper(AGENT_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, type, writing" + from + "agent_permission" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final boolean writing = resultSet.getBoolean(3);
                    entries.add(new TupleWrapper(AGENT_PERMISSION_STATE_ENTRY, new Int64Wrapper(Agent.NUMBER, number), type.toBlockable(AgentPermissions.ATTRIBUTE_TYPE), new BooleanWrapper(AgentPermissions.WRITING, writing)).toBlock());
                }
                tables.set(1, new ListWrapper(AGENT_PERMISSION_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + Restrictions.COLUMNS + from + "agent_restrictions" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Restrictions restrictions = Restrictions.get(entity, resultSet, 2);
                    entries.add(new TupleWrapper(AGENT_RESTRICTIONS_STATE_ENTRY, new Int64Wrapper(Agent.NUMBER, number), restrictions).toBlock());
                }
                tables.set(2, new ListWrapper(AGENT_RESTRICTIONS_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + Commitment.COLUMNS + ", name, icon" + from + "client" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Commitment commitment = Commitment.get(resultSet, 2);
                    final @Nonnull String name = resultSet.getString(5);
                    final @Nonnull Image icon = Image.get(resultSet, 6);
                    entries.add(new TupleWrapper(CLIENT_STATE_ENTRY, new Int64Wrapper(Agent.NUMBER, number), commitment, new StringWrapper(Client.NAME, name), icon.toBlock().setType(Client.ICON).toBlockable()).toBlock());
                }
                tables.set(3, new ListWrapper(CLIENT_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, relation, context" + from + "outgoing_role" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Identity relation = IdentityClass.getNotNull(resultSet, 2);
                    final @Nonnull Context context = Context.getNotNull(entity, resultSet, 3);
                    entries.add(new TupleWrapper(OUTGOING_ROLE_STATE_ENTRY, new Int64Wrapper(Agent.NUMBER, number), relation.toBlockable(Role.RELATION), context).toBlock());
                }
                tables.set(4, new ListWrapper(OUTGOING_ROLE_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT issuer, relation, agent FROM " + site + "incoming_role WHERE entity = " + entity + " AND " + agent.getRestrictions().isRole())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity issuer = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity relation = IdentityClass.getNotNull(resultSet, 2);
                    final long number = resultSet.getLong(3);
                    entries.add(new TupleWrapper(INCOMING_ROLE_STATE_ENTRY, issuer.toBlockable(Role.ISSUER), relation.toBlockable(Role.RELATION), new Int64Wrapper(Role.AGENT, number)).toBlock());
                }
                tables.set(5, new ListWrapper(INCOMING_ROLE_STATE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(MODULE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertIgnore(statement, site + "agent", "entity", "agent");
            Database.onInsertIgnore(statement, site + "agent_permission", "entity", "agent", "type");
            Database.onInsertIgnore(statement, site + "agent_restrictions", "entity", "agent");
            Database.onInsertIgnore(statement, site + "agent_order", "entity", "stronger", "weaker");
            Database.onInsertIgnore(statement, site + "client", "entity", "agent");
            Database.onInsertIgnore(statement, site + "outgoing_role", "entity", "agent");
            Database.onInsertIgnore(statement, site + "incoming_role", "entity", "issuer", "relation");
        }
        
        final @Nonnull ReadonlyArray<Block> tables = new TupleWrapper(block).getElementsNotNull(6);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent (entity, agent, client, removed) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNotNull(1)).getValue());
                preparedStatement.setBoolean(4, new BooleanWrapper(elements.getNotNull(2)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent_permission (entity, agent, type, writing) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().set(preparedStatement, 3);
                preparedStatement.setBoolean(4, new BooleanWrapper(elements.getNotNull(2)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(2)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(2);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                new Restrictions(entity, elements.getNotNull(1)).set(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "client (entity, agent, " + Commitment.COLUMNS + ", name, icon) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(4)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                new Commitment(elements.getNotNull(1)).set(preparedStatement, 3);
                preparedStatement.setString(6, new StringWrapper(elements.getNotNull(2)).getString());
                new Image(elements.getNotNull(3)).set(preparedStatement, 7);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "outgoing_role (entity, agent, relation, context) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(5)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().set(preparedStatement, 3);
                Context.get(entity, elements.getNotNull(2)).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "incoming_role (entity, issuer, relation, agent) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(6)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 2);
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().set(preparedStatement, 3);
                preparedStatement.setLong(4, new Int64Wrapper(elements.getNotNull(2)).getValue());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "agent");
            Database.onInsertNotIgnore(statement, site + "agent_permission");
            Database.onInsertNotIgnore(statement, site + "agent_restrictions");
            Database.onInsertNotIgnore(statement, site + "agent_order");
            Database.onInsertNotIgnore(statement, site + "client");
            Database.onInsertNotIgnore(statement, site + "outgoing_role");
            Database.onInsertNotIgnore(statement, site + "incoming_role");
        }
        
        redetermineOrder(entity, null);
    }
    
    @Override
    public void removeState(@Nonnull Entity entity) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "incoming_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "outgoing_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "client WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_order WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_restrictions WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_permission WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent WHERE entity = " + entity);
        }
    }
    
    @Pure
    @Override
    public @Nonnull AgentsQuery getInternalQuery(@Nonnull Role role) {
        return new AgentsQuery(role);
    }
    
    
    /**
     * Adds the given agent.
     * 
     * @param agent the agent to be added.
     */
    private static void addAgent(@Nonnull Agent agent) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + agent.getEntity().getSite() + "agent (entity, agent, client, removed) VALUES (" + agent.getEntity() + ", " + agent + ", " + agent.isClient() + ", " + agent.isRemoved() + ")");
        }
    }
    
    /**
     * Removes the given agent.
     * 
     * @param agent the agent to be removed.
     */
    public static void removeAgent(@Nonnull Agent agent) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + agent.getEntity().getSite() + "agent SET removed = TRUE WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND removed = FALSE";
            if (statement.executeUpdate(SQL) == 0) throw new SQLException("The agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be removed.");
        }
    }
    
    /**
     * Unremoves the given agent.
     * 
     * @param agent the agent to be unremoved.
     */
    public static void unremoveAgent(@Nonnull Agent agent) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + agent.getEntity().getSite() + "agent SET removed = FALSE WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND removed = TRUE";
            if (statement.executeUpdate(SQL) == 0) throw new SQLException("The agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be unremoved.");
        }
    }
    
    
    /**
     * Returns the permissions of the given agent.
     * 
     * @param agent the agent whose permissions are to be returned.
     * 
     * @return the permissions of the given agent.
     * 
     * @ensure return.isNotFrozen() : "The permissions are not frozen.";
     */
    public static @Nonnull AgentPermissions getPermissions(@Nonnull Agent agent) throws SQLException {
        final @Nonnull String SQL = "SELECT type, writing FROM " + agent.getEntity().getSite() + "agent_permission WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull AgentPermissions permissions = new AgentPermissions();
            while (resultSet.next()) {
                final @Nonnull SemanticType type = IdentityClass.getNotNull(resultSet, 1).toSemanticType().checkIsAttributeType();
                final boolean writing = resultSet.getBoolean(2);
                permissions.put(type, writing);
            }
            return permissions;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Adds the given permissions to the given agent.
     * 
     * @param agent the agent to which the permissions are to be added.
     * @param permissions the permissions to be added to the given agent.
     */
    public static void addPermissions(@Nonnull Agent agent, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        final @Nonnull String SQL = "INSERT INTO " + agent.getEntity().getSite() + "agent_permission (entity, agent, type, writing) VALUES (" + agent.getEntity() + ", " + agent + ", ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            for (final @Nonnull SemanticType type : permissions.keySet()) {
                type.set(preparedStatement, 1);
                preparedStatement.setBoolean(2, permissions.get(type));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /**
     * Removes the given permissions from the given agent.
     * 
     * @param agent the agent from which the permissions are to be removed.
     * @param permissions the permissions to be removed from the given agent.
     */
    public static void removePermissions(@Nonnull Agent agent, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        final @Nonnull String SQL = "DELETE FROM " + agent.getEntity().getSite() + "agent_permission WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND type = ? AND writing = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            for (final @Nonnull SemanticType type : permissions.keySet()) {
                type.set(preparedStatement, 1);
                preparedStatement.setBoolean(2, permissions.get(type));
                preparedStatement.addBatch();
            }
            final int[] counts = preparedStatement.executeBatch();
            for (final int count : counts) if (count < 1) throw new SQLException("Could not find a particular permission.");
        }
    }
    
    
    /**
     * Returns the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be returned.
     * 
     * @return the restrictions of the given agent.
     */
    public static @Nonnull Restrictions getRestrictions(@Nonnull Agent agent) throws SQLException {
        final @Nonnull String SQL = "SELECT " + Restrictions.COLUMNS + " FROM " + agent.getEntity().getSite() + "agent_restrictions WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Restrictions.get(agent.getEntity(), resultSet, 1);
            else throw new SQLException("The given agent has no restrictions.");
        }
    }
    
    /**
     * Sets the restrictions of the given agent to the given restrictions.
     * 
     * @param agent the agent whose restrictions are to be set initially.
     * @param restrictions the restrictions to be set for the given agent.
     */
    public static void setRestrictions(@Nonnull Agent agent, @Nonnull Restrictions restrictions) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + agent.getEntity().getSite() + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (" + agent.getEntity() + ", " + agent + ", " + restrictions + ")");
        }
    }
    
    /**
     * Replaces the restrictions of the given agent to the given restrictions.
     * 
     * @param agent the agent whose restrictions are to be replaced.
     * @param oldRestrictions the old restrictions to be replaced with the new restrictions.
     * @param newRestrictions the new restrictions with which the old restrictions are replaced.
     * 
     * @throws SQLException if the passed restrictions not the old restrictions.
     */
    public static void replaceRestrictions(@Nonnull Agent agent, @Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("UPDATE " + agent.getEntity().getSite() + "agent_restrictions SET " + newRestrictions.toUpdateValues() + " WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND " + oldRestrictions.toUpdateCondition());
        }
    }
    
    
    /**
     * Returns the agents that are weaker than the given agent.
     * 
     * @param agent the agent whose weaker agents are to be returned.
     * 
     * @return the agents that are weaker than the given agent.
     * 
     * @ensure return.isNotFrozen() : "The set is not frozen.";
     */
    public static @Capturable @Nonnull FreezableSet<Agent> getWeakerAgents(@Nonnull Agent agent) throws SQLException {
        final @Nonnull Entity entity = agent.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT agent, client, removed FROM " + site + "agent_order o, " + site + "agent t WHERE o.entity = " + entity + " AND o.stronger = " + agent + " AND o.entity = t.entity AND o.weaker = t.agent";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableSet<Agent> agents = new FreezableLinkedHashSet<Agent>();
            while (resultSet.next()) agents.add(Agent.get(entity, resultSet, 1, 2, 3));
            return agents;
        }
    }
    
    /**
     * Redetermines which agents are stronger and weaker than the given agent or, if that one is null, all agents at the given entity.
     * Please note that it is intentionally ignored whether agents have been removed. Make sure to check this at some other places!
     * 
     * @param entity the entity whose order of agents is to be redetermined.
     * @param agent the agent whose order is to be redetermined or null.
     */
    private static void redetermineOrder(@Nonnull Entity entity, @Nullable Agent agent) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "agent_order WHERE entity = " + entity + (agent != null ? " AND (stronger = " + agent + " OR weaker = " + agent + ")": ""));
            statement.executeUpdate("INSERT INTO " + site + "agent_order (entity, stronger, weaker) SELECT " + entity + ", rs.agent, rw.agent FROM " + site + "agent_restrictions AS rs, " + site + "agent_restrictions AS rw WHERE rs.entity = " + entity + " AND rw.entity = " + entity + (agent != null ? " AND (rs.agent = " + agent + " OR rw.agent = " + agent + ")": "")
                    + " AND (NOT rw.client OR rs.client) AND (NOT rw.role OR rs.role) AND (NOT rw.writing OR rs.writing)"
                    + " AND (rw.context IS NULL OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext AS cx WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cx.subcontext = rw.context))"
                    + " AND (rw.contact IS NULL OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext AS cx, " + site + "context_contact AS cc WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cc.entity = " + entity + " AND cc.context = cx.subcontext AND cc.contact = rw.contact) OR rs.contact IS NOT NULL AND rw.contact = rs.contact)"
                    + " AND (rw.client OR rs.writing AND rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "agent_permission AS ps, " + site + "context_subcontext AS cx, " + site + "outgoing_role AS or WHERE ps.entity = " + entity + " AND ps.agent = rs.agent AND cx.entity = " + entity + " AND cx.context = rs.context AND or.entity = " + entity + " AND or.agent = rw.agent AND (ps.type = or.relation OR ps.type = " + AgentPermissions.GENERAL +") AND ps.writing AND cx.subcontext = or.context))"
                    + " AND NOT EXISTS (SELECT * FROM " + site + "agent_permission AS pw LEFT JOIN " + site + "agent_permission AS ps ON pw.entity = " + entity + " AND ps.entity = " + entity + " AND ps.agent = rs.agent AND pw.agent = rw.agent AND (ps.type = pw.type OR ps.type = " + AgentPermissions.GENERAL + ") AND (NOT pw.writing OR ps.writing) WHERE ps.agent IS NULL)");
        }
    }
    
    /**
     * Redetermines which agents are stronger and weaker than the given agent.
     * 
     * @param agent the agent whose order is to be redetermined.
     */
    private static void redetermineOrder(@Nonnull Agent agent) throws SQLException {
        redetermineOrder(agent.getEntity(), agent);
    }
    
    
    /**
     * Adds the client with the given commitment to the given identity and returns the generated agent.
     * 
     * @param identity the identity to which the client is to be added.
     * @param commitment the commitment of the client to be added.
     * @param name the name of the client to be added.
     * @return the generated agent for this client.
     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
     */
    public static @Nonnull ClientAgent addClientAgent(@Nonnull NonHostIdentity identity, @Nonnull Commitment commitment, @Nonnull String name) throws SQLException {
        assert name.length() <= 50 : "The client name may have at most 50 characters.";
        
        long number = Database.executeInsert("INSERT INTO authorization (identity) VALUES (" + identity + ")");
        @Nonnull String sql = "INSERT INTO client (agent, host, time, commitment, name) VALUES (?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, number);
            preparedStatement.setLong(2, commitment.getHost().getNumber());
            preparedStatement.setLong(3, commitment.getTime());
            preparedStatement.setBytes(4, commitment.getValue().toByteArray());
            preparedStatement.setString(5, name);
            preparedStatement.executeUpdate();
        }
        
        return new ClientAgent(identity, number, commitment, name);
    }
    
    /**
     * Returns the client with the given commitment at the given identity or null if no such client is found.
     * 
     * @param entity the identity whose client is to be returned.
     * @param commitment the commitment of the client which is to be returned.
     * 
     * @return the client with the given commitment at the given identity or null if no such client is found.
     */
    public static @Nullable ClientAgent getClientAgent(@Nonnull Entity entity, @Nonnull Commitment commitment) throws SQLException {
        @Nonnull String sql = "SELECT authorization.agent, client.name FROM authorization JOIN client ON authorization.agent = client.agent WHERE authorization.identity = ? AND NOT authorization.removed AND client.host = ? AND client.time = ? AND client.commitment = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(sql)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setLong(2, commitment.getHost().getNumber());
            preparedStatement.setLong(3, commitment.getTime());
            preparedStatement.setBytes(4, commitment.getValue().toByteArray());
            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return new ClientAgent(entity, resultSet.getLong(1), commitment, resultSet.getString(2));
                else return null;
            }
        }
        return null;
    }
    
    /**
     * Adds the outgoing role with the given relation, context and visibility to the given identity and returns the generated agent.
     * 
     * @param identity the identity to which the outgoing role is to be added.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which the outgoing role is to be assigned.
     * @return the generated agent for this outgoing role.
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nonnull OutgoingRole addOutgoingRole(@Nonnull NonHostIdentity identity, @Nonnull SemanticType relation, @Nonnull Context context) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        try (@Nonnull Statement statement = connection.createStatement()) {
            long number = Database.executeInsert(statement, "INSERT INTO authorization (identity) VALUES (" + identity + ")");
            statement.executeUpdate("INSERT INTO outgoing_role (agent, relation, context) VALUES (" + number + ", " + relation + ", " + context + ")");
            return new OutgoingRole(number, identity, relation, context);
        } catch (@Nonnull SQLException exception) {
            if (relation.hasBeenMerged()) return addOutgoingRole(identity, relation, context);
            else throw exception;
        }
    }
    
    /**
     * Returns the outgoing role with the given relation at the given identity or null if no such role is found.
     * 
     * @param entity the identity whose outgoing role is to be returned.
     * @param relation the relation between the issuing and the receiving identity.
     * 
     * @return the outgoing role with the given relation at the given identity or null if no such role is found.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nullable OutgoingRole getOutgoingRole(@Nonnull Entity entity, @Nonnull SemanticType relation, boolean restrictable) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        // TODO: Create the OutgoingRole according to the restrictable parameter.
        
        @Nonnull String sql = "SELECT authorization.agent, outgoing_role.context FROM authorization JOIN outgoing_role ON authorization.agent = outgoing_role.agent WHERE authorization.identity = " + entity + " AND NOT authorization.removed AND outgoing_role.relation = " + relation;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return new OutgoingRole(resultSet.getLong(1), entity, relation, new Context(resultSet.getLong(2)));
            } else {
                if (relation.hasBeenMerged()) return getOutgoingRole(entity, relation);
                else return null;
            }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
        return null;
    }
    
    /**
     * Adds the incoming role with the given issuer and relation to the given identity and returns the generated authorization.
     * 
     * @param identity the identity to which the incoming role is to be added.
     * @param issuer the issuer of the incoming role.
     * @param relation the relation of the incoming role.
     * @return the generated authorization for this incoming role.
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nonnull IncomingRole addIncomingRole(@Nonnull NonHostIdentity identity, @Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        try (@Nonnull Statement statement = connection.createStatement()) {
            long number = Database.executeInsert(statement, "INSERT INTO authorization (identity) VALUES (" + identity + ")");
            statement.executeUpdate("INSERT INTO incoming_role (agent, issuer, relation) VALUES (" + number + ", " + issuer + ", " + relation + ")");
            return new IncomingRole(number, identity, issuer, relation);
        } catch (@Nonnull SQLException exception) {
            if (issuer.hasBeenMerged() || relation.hasBeenMerged()) return addIncomingRole(identity, issuer, relation);
            else throw exception;
        }
    }
    
    /**
     * Returns the incoming role with the given issuer and relation at the given identity or null if no such role is found.
     * 
     * @param identity the identity whose incoming role is to be returned.
     * @param issuer the issuer of the incoming role which is to be returned.
     * @param relation the relation of the incoming role which is to be returned.
     * @return the incoming role with the given issuer and relation at the given identity or null if no such role is found.
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nullable IncomingRole getIncomingRole(@Nonnull NonHostIdentity identity, @Nonnull NonHostIdentity issuer, @Nonnull SemanticType relation) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        @Nonnull String sql = "SELECT authorization.agent FROM authorization JOIN incoming_role ON authorization.agent = incoming_role.agent WHERE authorization.identity = " + identity + " AND NOT authorization.removed AND incoming_role.issuer = " + issuer + " AND incoming_role.relation = " + relation;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return new IncomingRole(resultSet.getLong(1), identity, issuer, relation);
            } else {
                if (issuer.hasBeenMerged() || relation.hasBeenMerged()) return getIncomingRole(identity, issuer, relation);
                else return null;
            }
        }
    }
    
    /**
     * Returns the incoming roles of the given identity restricted by the given agent.
     * 
     * @param identity the identity whose incoming roles are to be returned.
     * @param agent the agent with which to restrict the incoming roles.
     * @return the incoming roles of the given identity restricted by the given agent.
     * @require agent.getRestrictions() != null : "The restrictions of the agent is not null.";
     */
    public static @Nonnull Set<IncomingRole> getIncomingRoles(@Nonnull NonHostIdentity identity, @Nonnull Agent agent) throws SQLException {
        assert agent.getRestrictions() != null : "The restrictions of the agent is not null.";
        
        @Nonnull String sql = "SELECT authorization.agent, issuer.identity, issuer.category, issuer.address, relation.identity, relation.category, relation.address FROM authorization JOIN incoming_role ON authorization.agent = incoming_role.agent JOIN general_identity AS issuer ON incoming_role.issuer = general_identity.identity JOIN general_identity AS relation ON incoming_role.relation = general_identity.identity WHERE authorization.identity = " + identity;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(sql)) {
            @Nonnull Set<IncomingRole> incomingRoles = new LinkedHashSet<IncomingRole>();
            while (resultSet.next()) {
                @Nonnull NonHostIdentity issuer = Identity.create(Category.get(resultSet.getByte(3)), resultSet.getLong(2), new NonHostIdentifier(resultSet.getString(4))).toNonHostIdentity();
                @Nonnull SemanticType relation = Identity.create(Category.get(resultSet.getByte(6)), resultSet.getLong(5), new NonHostIdentifier(resultSet.getString(7))).toSemanticType();
                @Nonnull IncomingRole incomingRole = new IncomingRole(resultSet.getLong(1), identity, issuer, relation);
                incomingRole.restrictTo(agent);
                incomingRoles.add(incomingRole);
            }
            return incomingRoles;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Removes the given incoming role.
     * 
     * @param incomingRole the incoming role to be removed.
     */
    public static void removeIncomingRole(@Nonnull IncomingRole incomingRole) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM authorization WHERE agent = " + incomingRole);
        }
    }
    
    
    /**
     * Sets the commitment of the given client agent to the given value.
     * 
     * @param clientAgent the client agent whose commitment is to be set.
     * @param commitment the commitment to set for the given client agent.
     */
    public static void setClientCommitment(@Nonnull ClientAgent clientAgent, @Nonnull Commitment commitment) throws SQLException {
        @Nonnull String sql = "UPDATE client SET host = ?, time = ?, commitment = ? WHERE agent = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, commitment.getHost().getNumber());
            preparedStatement.setLong(2, commitment.getTime());
            preparedStatement.setBytes(3, commitment.getValue().toByteArray());
            preparedStatement.setLong(4, clientAgent.getNumber());
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Sets the name of the given client agent to the given string.
     * 
     * @param clientAgent the client agent whose name is to be set.
     * @param name the name to set for the given client agent.
     * @require name.length() <= 50 : "The client name may have at most 50 characters.";
     */
    public static void setClientName(@Nonnull ClientAgent clientAgent, @Nonnull String name) throws SQLException {
        assert name.length() <= 50 : "The client name may have at most 50 characters.";
        
        @Nonnull String sql = "UPDATE client SET name = ? WHERE agent = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, clientAgent.getNumber());
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Sets the context of the given outgoing role to the given value.
     * 
     * @param outgoingRole the outgoing role whose context is to be set.
     * @param context the context to set for the given outgoing role.
     */
    public static void setOutgoingRoleContext(@Nonnull OutgoingRole outgoingRole, @Nonnull Context context) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE outgoing_role SET context = " + context + " WHERE agent = " + outgoingRole);
        }
    }
    
}
