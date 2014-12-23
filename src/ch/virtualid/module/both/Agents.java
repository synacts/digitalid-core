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
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.query.internal.AgentsQuery;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
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
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link Agent agents} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public final class Agents implements BothModule {
    
    /**
     * Initializes this class.
     */
    static void initialize() {}
    
    static { Contexts.initialize(); }
    
    public static final Agents MODULE = new Agents();
    
    /**
     * Creates the table which is referenced for the given site.
     * 
     * @param site the site for which the reference table is created.
     */
    public static void createReferenceTable(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent (entity " + EntityClass.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, client BOOLEAN NOT NULL, removed BOOLEAN NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
        }
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_permission (entity " + EntityClass.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, writing BOOLEAN NOT NULL, PRIMARY KEY (entity, agent, type), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_restrictions (entity " + EntityClass.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Restrictions.FORMAT_NOT_NULL + ", PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Restrictions.getForeignKeys(site) + ")");
            Mapper.addReference(site + "agent_restrictions", "contact");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_order (entity " + EntityClass.FORMAT + " NOT NULL, stronger " + Agent.FORMAT + " NOT NULL, weaker " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, stronger, weaker), FOREIGN KEY (entity, stronger) " + Agent.getReference(site) + ", FOREIGN KEY (entity, weaker) " + Agent.getReference(site) + ")");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "client_agent (entity " + EntityClass.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Commitment.FORMAT + ", name VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", icon " + Image.FORMAT + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Commitment.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "outgoing_role (entity " + EntityClass.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (relation) " + Mapper.REFERENCE + ", FOREIGN KEY (entity, context) " + Context.getReference(site) + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "incoming_role (entity " + EntityClass.FORMAT + " NOT NULL, issuer " + Mapper.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, issuer, relation), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (issuer) " + Mapper.REFERENCE + ", FOREIGN KEY (relation) " + Mapper.REFERENCE + ")");
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
                    final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
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
                    final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
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
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().checkIsAttributeType().set(preparedStatement, 3);
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
                new Restrictions(NonHostAccount.get(host, identity), elements.getNotNull(2)).set(preparedStatement, 3);
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
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name, icon) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
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
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().checkIsRoleType().set(preparedStatement, 3);
                Context.get(NonHostAccount.get(host, identity), elements.getNotNull(3)).set(preparedStatement, 4);
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
                IdentityClass.create(elements.getNotNull(2)).toSemanticType().checkIsRoleType().set(preparedStatement, 3);
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
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("agents.state@virtualid.ch").load(TupleWrapper.TYPE, AGENT_STATE_TABLE, AGENT_PERMISSION_STATE_TABLE, AGENT_RESTRICTIONS_STATE_TABLE, CLIENT_STATE_TABLE, OUTGOING_ROLE_STATE_TABLE, INCOMING_ROLE_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull Agent agent) throws SQLException {
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
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + Commitment.COLUMNS + ", name, icon" + from + "client_agent" + where)) {
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
        return new TupleWrapper(STATE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
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
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsAttributeFor(entity).set(preparedStatement, 3);
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
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name, icon) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(3)).getElementsNotNull();
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
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(4)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                preparedStatement.setLong(2, new Int64Wrapper(elements.getNotNull(0)).getValue());
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsRoleType().set(preparedStatement, 3);
                Context.get(entity, elements.getNotNull(2)).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "incoming_role (entity, issuer, relation, agent) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(5)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                IdentityClass.create(elements.getNotNull(0)).toInternalNonHostIdentity().set(preparedStatement, 2);
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsRoleType().set(preparedStatement, 3);
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
        
        ClientAgent.reset(entity);
        OutgoingRole.reset(entity);
        
        if (entity instanceof Role) resetIncomingRoles((Role) entity);
    }
    
    @Override
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "incoming_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "outgoing_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "client_agent WHERE entity = " + entity);
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
                final @Nonnull SemanticType type = IdentityClass.getNotNull(resultSet, 1).toSemanticType().checkIsAttributeFor(agent.getEntity());
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
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    public static void addPermissions(@Nonnull Agent agent, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        assert permissions.isFrozen() : "The permissions are frozen.";
        
        final @Nonnull String SQL = "INSERT INTO " + agent.getEntity().getSite() + "agent_permission (entity, agent, type, writing) VALUES (" + agent.getEntity() + ", " + agent + ", ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            for (final @Nonnull SemanticType type : permissions.keySet()) {
                type.set(preparedStatement, 1);
                preparedStatement.setBoolean(2, permissions.get(type));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        redetermineOrder(agent);
    }
    
    /**
     * Removes the given permissions from the given agent.
     * 
     * @param agent the agent from which the permissions are to be removed.
     * @param permissions the permissions to be removed from the given agent.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    public static void removePermissions(@Nonnull Agent agent, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        assert permissions.isFrozen() : "The permissions are frozen.";
        
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
        redetermineOrder(agent);
    }
    
    
    /**
     * Returns the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be returned.
     * 
     * @return the restrictions of the given agent.
     * 
     * @ensure return.match(agent) : "The returned restrictions match the given agent.";
     */
    public static @Nonnull Restrictions getRestrictions(@Nonnull Agent agent) throws SQLException {
        final @Nonnull String SQL = "SELECT " + Restrictions.COLUMNS + " FROM " + agent.getEntity().getSite() + "agent_restrictions WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Restrictions.get(agent.getEntity(), resultSet, 1).checkMatch(agent);
            else throw new SQLException("The given agent has no restrictions.");
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Sets the restrictions of the given agent to the given restrictions.
     * 
     * @param agent the agent whose restrictions are to be set initially.
     * @param restrictions the restrictions to be set for the given agent.
     */
    private static void setRestrictions(@Nonnull Agent agent, @Nonnull Restrictions restrictions) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + agent.getEntity().getSite() + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (" + agent.getEntity() + ", " + agent + ", " + restrictions + ")");
        } catch (@Nonnull SQLException exception) {
            final @Nullable Contact contact = restrictions.getContact();
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) setRestrictions(agent, restrictions);
            else throw exception;
        }
    }
    
    /**
     * Replaces the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be replaced.
     * @param oldRestrictions the old restrictions to be replaced with the new restrictions.
     * @param newRestrictions the new restrictions with which the old restrictions are replaced.
     * 
     * @throws SQLException if the passed restrictions are not the old restrictions.
     * 
     * @require oldRestrictions.match(agent) : "The old restrictions match the given agent.";
     * @require newRestrictions.match(agent) : "The new restrictions match the given agent.";
     */
    public static void replaceRestrictions(@Nonnull Agent agent, @Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws SQLException {
        assert oldRestrictions.match(agent) : "The old restrictions match the given agent.";
        assert newRestrictions.match(agent) : "The new restrictions match the given agent.";
        
        final int count;
        try (@Nonnull Statement statement = Database.createStatement()) {
            count = statement.executeUpdate("UPDATE " + agent.getEntity().getSite() + "agent_restrictions SET " + newRestrictions.toUpdateValues() + " WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND " + oldRestrictions.toUpdateCondition());
        } catch (@Nonnull SQLException exception) {
            final @Nullable Contact contact = newRestrictions.getContact();
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) replaceRestrictions(agent, oldRestrictions, newRestrictions);
            return;
        }
        if (count == 0) {
            final @Nullable Contact contact = oldRestrictions.getContact();
            final @Nonnull SQLException exception = new SQLException("The restrictions of the agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be replaced.");
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) replaceRestrictions(agent, oldRestrictions, newRestrictions);
            return;
        }
        redetermineOrder(agent);
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
        final @Nonnull NonHostEntity entity = agent.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT agent, client, removed FROM " + site + "agent_order o, " + site + "agent t WHERE o.entity = " + entity + " AND o.stronger = " + agent + " AND o.entity = t.entity AND o.weaker = t.agent";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableSet<Agent> agents = new FreezableLinkedHashSet<Agent>();
            while (resultSet.next()) agents.add(Agent.get(entity, resultSet, 1, 2, 3));
            return agents;
        }
    }
    
    /**
     * Returns the agent weaker than the given agent with the given agent number.
     * 
     * @param agent the agent whose weaker agent is to be returned.
     * @param agentNumber the number of the agent which is to be returned.
     * 
     * @return the agent weaker than the given agent with the given agent number.
     * 
     * @throws SQLException if no weaker agent with the given number is found.
     */
    public static @Nonnull Agent getWeakerAgent(@Nonnull Agent agent, long agentNumber) throws SQLException {
        final @Nonnull NonHostEntity entity = agent.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT agent, client, removed FROM " + site + "agent_order o, " + site + "agent t WHERE o.entity = " + entity + " AND o.stronger = " + agent + " AND o.weaker = " + agentNumber + " AND t.entity = " + entity + " AND t.agent = " + agentNumber;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Agent.getNotNull(entity, resultSet, 1, 2, 3);
            else throw new SQLException("No weaker agent with the given number was found.");
        }
    }
    
    /**
     * Returns whether the first agent is stronger than the second agent.
     * 
     * @param agent1 the first agent.
     * @param agent2 the second agent.
     * 
     * @return whether the first agent is stronger than the second agent.
     * 
     * @require agent1.getEntity().equals(agent2.getEntity()) : "Both agents belong to the same entity.";
     */
    public static boolean isStronger(@Nonnull Agent agent1, @Nonnull Agent agent2) throws SQLException {
        assert agent1.getEntity().equals(agent2.getEntity()) : "Both agents belong to the same entity.";
        
        final @Nonnull NonHostEntity entity = agent1.getEntity();
        final @Nonnull String SQL = "SELECT EXISTS (SELECT * FROM " + entity.getSite() + "agent_order WHERE entity = " + entity + " AND stronger = " + agent1 + " AND weaker = " + agent2 + ")";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return resultSet.next() && resultSet.getBoolean(1);
        }
    }
    
    /**
     * Redetermines which agents are stronger and weaker than the given agent or, if that one is null, all agents at the given entity.
     * Please note that it is intentionally ignored whether agents have been removed. Make sure to check this at some other places!
     * 
     * @param entity the entity whose order of agents is to be redetermined.
     * @param agent the agent whose order is to be redetermined or null.
     */
    private static void redetermineOrder(@Nonnull NonHostEntity entity, @Nullable Agent agent) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "agent_order WHERE entity = " + entity + (agent != null ? " AND (stronger = " + agent + " OR weaker = " + agent + ")": ""));
            statement.executeUpdate("INSERT INTO " + site + "agent_order (entity, stronger, weaker) SELECT " + entity + ", rs.agent, rw.agent FROM " + site + "agent_restrictions AS rs, " + site + "agent_restrictions AS rw WHERE rs.entity = " + entity + " AND rw.entity = " + entity + (agent != null ? " AND (rs.agent = " + agent + " OR rw.agent = " + agent + ")": "")
                    + " AND (NOT rw.client OR rs.client) AND (NOT rw.role OR rs.role) AND (NOT rw.writing OR rs.writing)"
                    + " AND (rw.context IS NULL OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext AS cx WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cx.subcontext = rw.context))"
                    + " AND (rw.contact IS NULL OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext AS cx, " + site + "context_contact AS cc WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cc.entity = " + entity + " AND cc.context = cx.subcontext AND cc.contact = rw.contact) OR rs.contact IS NOT NULL AND rw.contact = rs.contact)"
                    + " AND (rw.client OR rs.writing AND rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "agent_permission AS ps, " + site + "context_subcontext AS cx, " + site + "outgoing_role AS og WHERE ps.entity = " + entity + " AND ps.agent = rs.agent AND cx.entity = " + entity + " AND cx.context = rs.context AND og.entity = " + entity + " AND og.agent = rw.agent AND (ps.type = og.relation OR ps.type = " + AgentPermissions.GENERAL +") AND ps.writing AND cx.subcontext = og.context))"
                    + " AND NOT EXISTS (SELECT * FROM " + site + "agent_permission AS pw LEFT JOIN " + site + "agent_permission AS ps ON pw.entity = " + entity + " AND ps.entity = " + entity + " AND (ps.type = pw.type OR ps.type = " + AgentPermissions.GENERAL + ") AND (NOT pw.writing OR ps.writing) WHERE ps.agent = rs.agent AND pw.agent = rw.agent AND ps.agent IS NULL)");
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
     * Adds the client agent with the given parameters to the database.
     * 
     * @param clientAgent the client agent which is to be added.
     * @param permissions the permissions of the client agent.
     * @param restrictions the restrictions of the client agent.
     * @param commitment the commitment of the client agent.
     * @param name the name of the given client agent.
     * @param icon the icon of the given client agent.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require Client.isValid(name) : "The name is valid.";
     * @require Client.isValid(icon) : "The icon is valid.";
     */
    public static void addClientAgent(@Nonnull ClientAgent clientAgent, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nonnull Commitment commitment, @Nonnull String name, @Nonnull Image icon) throws SQLException {
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert Client.isValid(name) : "The name is valid.";
        assert Client.isValid(icon) : "The icon is valid.";
        
        addAgent(clientAgent);
        setRestrictions(clientAgent, restrictions);
        addPermissions(clientAgent, permissions);
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "INSERT INTO " + entity.getSite() + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name, icon) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            entity.set(preparedStatement, 1);
            clientAgent.set(preparedStatement, 2);
            commitment.set(preparedStatement, 3);
            preparedStatement.setString(6, name);
            icon.set(preparedStatement, 7);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Returns the client agent with the given commitment at the given entity or null if no such client agent is found.
     * 
     * @param entity the entity whose client agent is to be returned.
     * @param commitment the commitment of the client agent to be returned.
     * 
     * @return the client agent with the given commitment at the given entity or null if no such client agent is found.
     */
    public static @Nullable ClientAgent getClientAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT a.agent, a.removed FROM " + site + "client_agent AS c, " + site + "agent AS a WHERE c.entity = " + entity + " AND a.entity = " + entity + " AND c.agent = a.agent AND " + Commitment.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            commitment.set(preparedStatement, 1);
            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return ClientAgent.get(entity, resultSet.getLong(1), resultSet.getBoolean(2));
                else return null;
            }
        }
    }
    
    /**
     * Returns the commitment of the given client agent.
     * 
     * @param clientAgent the client agent whose commitment is to be returned.
     */
    public static @Nonnull Commitment getCommitment(@Nonnull ClientAgent clientAgent) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT " + Commitment.COLUMNS + " FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Commitment.get(resultSet, 1);
            else throw new SQLException("The given client agent has no commitment.");
        }
    }
    
    /**
     * Replaces the commitment of the given client agent.
     * 
     * @param clientAgent the client agent whose commitment is to be replaced.
     * @param oldCommitment the old commitment of the given client agent.
     * @param newCommitment the new commitment of the given client agent.
     */
    public static void replaceCommitment(@Nonnull ClientAgent clientAgent, @Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET " + Commitment.UPDATE + " WHERE entity = " + entity + " AND agent = " + clientAgent + " AND " + Commitment.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newCommitment.set(preparedStatement, 1);
            oldCommitment.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The commitment of the client agent with the number " + clientAgent + " could not be replaced.");
        }
    }
    
    /**
     * Returns the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be returned.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    public static @Nonnull String getName(@Nonnull ClientAgent clientAgent) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT name FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String name = resultSet.getString(1);
                if (!Client.isValid(name)) throw new SQLException("The name of the client agent with the number " + clientAgent + " is invalid.");
                return name;
            } else throw new SQLException("The given client agent has no name.");
        }
    }
    
    /**
     * Replaces the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be replaced.
     * @param oldName the old name of the given client agent.
     * @param newName the new name of the given client agent.
     * 
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    public static void replaceName(@Nonnull ClientAgent clientAgent, @Nonnull String oldName, @Nonnull String newName) throws SQLException {
        assert Client.isValid(oldName) : "The old name is valid.";
        assert Client.isValid(newName) : "The new name is valid.";
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET name = ? WHERE entity = " + entity + " AND agent = " + clientAgent + " AND name = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, oldName);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The name of the client agent with the number " + clientAgent + " could not be replaced.");
        }
    }
    
    /**
     * Returns the icon of the given client agent.
     * 
     * @param clientAgent the client agent whose icon is to be returned.
     * 
     * @ensure Client.isValid(return) : "The returned icon is valid.";
     */
    public static @Nonnull Image getIcon(@Nonnull ClientAgent clientAgent) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT icon FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull Image icon = Image.get(resultSet, 1);
                if (!Client.isValid(icon)) throw new SQLException("The icon of the client agent with the number " + clientAgent + " is invalid.");
                return icon;
            } else throw new SQLException("The given client agent has no icon.");
        }
    }
    
    /**
     * Replaces the icon of the given client agent.
     * 
     * @param clientAgent the client agent whose icon is to be replaced.
     * @param oldIcon the old icon of the given client agent.
     * @param newIcon the new icon of the given client agent.
     * 
     * @require Client.isValid(oldIcon) : "The old icon is valid.";
     * @require Client.isValid(newIcon) : "The new icon is valid.";
     */
    public static void replaceIcon(@Nonnull ClientAgent clientAgent, @Nonnull Image oldIcon, @Nonnull Image newIcon) throws SQLException {
        assert Client.isValid(oldIcon) : "The old icon is valid.";
        assert Client.isValid(newIcon) : "The new icon is valid.";
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET icon = ? WHERE entity = " + entity + " AND agent = " + clientAgent + " AND icon = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newIcon.set(preparedStatement, 1);
            oldIcon.set(preparedStatement, 2);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The icon of the client agent with the number " + clientAgent + " could not be replaced.");
        }
    }
    
    
    /**
     * Adds the outgoing role with the given parameters to the database.
     * 
     * @param outgoingRole the outgoing role which is to be added to the database.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which the outgoing role is to be assigned.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     * @require context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
     */
    public static void addOutgoingRole(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType relation, @Nonnull Context context) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        assert context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
        
        addAgent(outgoingRole);
        setRestrictions(outgoingRole, Restrictions.NONE);
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + entity.getSite() + "outgoing_role (entity, agent, relation, context) VALUES (" + entity + ", " + outgoingRole + ", " + relation + ", " + context + ")");
        }
        
        redetermineOrder(outgoingRole);
    }
    
    /**
     * Returns the outgoing role with the given relation at the given entity or null if no such role is found.
     * 
     * @param entity the entity whose outgoing role is to be returned.
     * @param relation the relation between the issuing and the receiving identity.
     * @param restrictable whether the outgoing role can be restricted.
     * 
     * @return the outgoing role with the given relation at the given entity or null if no such role is found.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static @Nullable OutgoingRole getOutgoingRole(@Nonnull NonHostEntity entity, @Nonnull SemanticType relation, boolean restrictable) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT a.agent, a.removed FROM " + site + "outgoing_role AS o, " + site + "agent AS a WHERE o.entity = " + entity + " AND a.entity = " + entity + " AND o.agent = a.agent AND o.relation = " + relation;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return OutgoingRole.get(entity, resultSet.getLong(1), resultSet.getBoolean(2), restrictable);
            else return null;
        }
    }
    
    /**
     * Returns the relation of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose relation is to be returned.
     * 
     * @ensure return.isRoleType() : "The returned relation is a role type.";
     */
    public static @Nonnull SemanticType getRelation(@Nonnull OutgoingRole outgoingRole) throws SQLException {
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        final @Nonnull String SQL = "SELECT relation FROM " + entity.getSite() + "outgoing_role WHERE entity = " + entity + " AND agent = " + outgoingRole;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return IdentityClass.getNotNull(resultSet, 1).toSemanticType().checkIsRoleType();
            else throw new SQLException("The given outgoing role has no relation.");
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Replaces the relation of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose relation is to be replaced.
     * @param oldRelation the old relation of the given outgoing role.
     * @param newRelation the new relation of the given outgoing role.
     * 
     * @require oldRelation.isRoleType() : "The old relation is a role type.";
     * @require newRelation.isRoleType() : "The new relation is a role type.";
     */
    public static void replaceRelation(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) throws SQLException {
        assert oldRelation.isRoleType() : "The old relation is a role type.";
        assert newRelation.isRoleType() : "The new relation is a role type.";
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + entity.getSite() + "outgoing_role SET relation = " + newRelation + " WHERE entity = " + entity + " AND agent = " + outgoingRole + " AND relation = " + oldRelation;
            if (statement.executeUpdate(SQL) == 0) throw new SQLException("The relation of the client agent with the number " + outgoingRole + " could not be replaced.");
        }
        redetermineOrder(outgoingRole);
    }
    
    /**
     * Returns the context of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose context is to be returned.
     * 
     * @ensure return.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the same entity as the outgoing role.";
     */
    public static @Nonnull Context getContext(@Nonnull OutgoingRole outgoingRole) throws SQLException {
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        final @Nonnull String SQL = "SELECT context FROM " + entity.getSite() + "outgoing_role WHERE entity = " + entity + " AND agent = " + outgoingRole;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return Context.getNotNull(entity, resultSet, 1);
            else throw new SQLException("The given outgoing role has no context.");
        }
    }
    
    /**
     * Replaces the context of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose context is to be replaced.
     * @param oldContext the old context of the given outgoing role.
     * @param newContext the new context of the given outgoing role.
     * 
     * @require oldContext.isRoleType() : "The old context is a role type.";
     * @require newContext.isRoleType() : "The new context is a role type.";
     */
    public static void replaceContext(@Nonnull OutgoingRole outgoingRole, @Nonnull Context oldContext, @Nonnull Context newContext) throws SQLException {
        assert oldContext.getEntity().equals(outgoingRole.getEntity()) : "The old context belongs to the same entity as the outgoing role.";
        assert newContext.getEntity().equals(outgoingRole.getEntity()) : "The new context belongs to the same entity as the outgoing role.";
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + entity.getSite() + "outgoing_role SET context = " + newContext + " WHERE entity = " + entity + " AND agent = " + outgoingRole + " AND context = " + oldContext;
            if (statement.executeUpdate(SQL) == 0) throw new SQLException("The context of the client agent with the number " + outgoingRole + " could not be replaced.");
        }
        redetermineOrder(outgoingRole);
    }
    
    
    /**
     * Adds the incoming role with the given issuer, relation and agent number to the given entity.
     * 
     * @param entity the entity to which the incoming role is to be added.
     * @param issuer the issuer of the incoming role.
     * @param relation the relation of the incoming role.
     * @param agentNumber the agent number of the incoming role.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static void addIncomingRole(@Nonnull NonHostEntity entity, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + entity.getSite() + "incoming_role (entity, issuer, relation, agent) VALUES (" + entity + ", " + issuer + ", " + relation + ", " + agentNumber + ")");
        } catch (@Nonnull SQLException exception) {
            if (issuer.hasBeenMerged(exception)) addIncomingRole(entity, issuer, relation, agentNumber);
        }
    }
    
    /**
     * Removes the incoming role with the given issuer and relation at the given entity.
     * 
     * @param entity the entity from which the incoming role is to be removed.
     * @param issuer the issuer of the incoming role which is to be removed.
     * @param relation the relation of the incoming role which is to be removed.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    public static void removeIncomingRole(@Nonnull NonHostEntity entity, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "DELETE FROM " + entity.getSite() + "incoming_role WHERE entity = " + entity + " AND issuer = " + issuer + " AND relation = " + relation;
            if (statement.executeUpdate(SQL) == 0) {
                final @Nonnull SQLException exception = new SQLException("The incoming role with the issuer " + issuer.getAddress() + " and relation " + relation.getAddress() + " could not be removed.");
                if (issuer.hasBeenMerged(exception)) removeIncomingRole(entity, issuer, relation);
            }
        }
    }
    
    /**
     * Resets the incoming roles of the given role.
     * 
     * @param role the role whose incoming roles are to be reset.
     */
    public static void resetIncomingRoles(@Nonnull Role role) throws SQLException {
        final @Nonnull ReadonlyList<Role> roles = role.getRoles();
        final @Nonnull HashSet<Role> foundRoles = new HashSet<Role>();
        final @Nonnull String SQL = "SELECT issuer, relation, agent FROM " + role.getSite() + "incoming_role WHERE entity = " + role;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            while (resultSet.next()) {
                final @Nonnull InternalNonHostIdentity issuer = IdentityClass.getNotNull(resultSet, 1).toInternalNonHostIdentity();
                final @Nonnull SemanticType relation = IdentityClass.getNotNull(resultSet, 2).toSemanticType().checkIsRoleType();
                final long agentNumber = resultSet.getLong(3);
                
                boolean found = false;
                for (final @Nonnull Role subrole : roles) {
                    if (subrole.getIssuer().equals(issuer) && relation.equals(subrole.getRelation())) {
                        foundRoles.add(role);
                        found = true;
                        break;
                    }
                }
                if (!found) role.addRole(issuer, relation, agentNumber);
            }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
        for (final @Nonnull Role subrole : roles) {
            if (!foundRoles.contains(subrole)) subrole.remove();
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
