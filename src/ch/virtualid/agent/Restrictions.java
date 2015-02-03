package ch.virtualid.agent;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.Context;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the restrictions of an agent.
 * <p>
 * <em>Important:</em> Though this class promises that its objects are immutable, their hash may change!
 * 
 * @invariant getContext() == null || getContact() == null : "The context or the contact is null.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Restrictions implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code client.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT_TYPE = SemanticType.create("client.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code role.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ROLE_TYPE = SemanticType.create("role.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code writing.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType WRITING_TYPE = SemanticType.create("writing.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code context.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTEXT_TYPE = SemanticType.create("context.restrictions.agent@virtualid.ch").load(Context.TYPE);
    
    /**
     * Stores the semantic type {@code contact.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTACT_TYPE = SemanticType.create("contact.restrictions.agent@virtualid.ch").load(Contact.TYPE);
    
    /**
     * Stores the semantic type {@code restrictions.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("restrictions.agent@virtualid.ch").load(TupleWrapper.TYPE, CLIENT_TYPE, ROLE_TYPE, WRITING_TYPE, CONTEXT_TYPE, CONTACT_TYPE);
    
    
    /**
     * Stores the weakest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MIN = new Restrictions(false, false, false);
    
    /**
     * Stores the strongest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MAX = new Restrictions(true, true, true);
    
    /**
     * Stores the restrictions required to modify clients.
     */
    public static final @Nonnull Restrictions CLIENT = new Restrictions(true, false, false);
    
    /**
     * Stores the restrictions required to assume incoming roles.
     */
    public static final @Nonnull Restrictions ROLE = new Restrictions(false, true, false);
    
    /**
     * Stores the restrictions required to write to contexts.
     */
    public static final @Nonnull Restrictions WRITING = new Restrictions(false, false, true);
    
    
    /**
     * Stores whether the authorization is restricted to clients.
     */
    private final boolean client;
    
    /**
     * Stores whether the authorization is restricted to agents that can assume incoming roles.
     */
    private final boolean role;
    
    /**
     * Stores whether the authorization is restricted to agents that can write to contexts.
     */
    private final boolean writing;
    
    /**
     * Stores the context to which the authorization is restricted (or null).
     */
    private final @Nullable Context context;
    
    /**
     * Stores the contact to which the authorization is restricted (or null).
     */
    private final @Nullable Contact contact;
    
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     */
    public Restrictions(boolean client, boolean role, boolean writing) {
        this(client, role, writing, null, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * @param context the context to which the authorization is restricted (or null).
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nullable Context context) {
        this(client, role, writing, context, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * @param contact the contact to which the authorization is restricted (or null).
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nullable Contact contact) {
        this(client, role, writing, null, contact);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * @param context the context to which the authorization is restricted (or null).
     * @param contact the contact to which the authorization is restricted (or null).
     * 
     * @require context == null || contact == null : "The context or the contact is null.";
     */
    private Restrictions(boolean client, boolean role, boolean writing, @Nullable Context context, @Nullable Contact contact) {
        assert context == null || contact == null : "The context or the contact is null.";
        
        this.client = client;
        this.role = role;
        this.writing = writing;
        this.context = context;
        this.contact = contact;
    }
    
    /**
     * Creates new restrictions from the given block.
     * 
     * @param entity the entity to which the restrictions belongs.
     * @param block the block containing the restrictions.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public Restrictions(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.client = new BooleanWrapper(tuple.getElementNotNull(0)).getValue();
        this.role = new BooleanWrapper(tuple.getElementNotNull(1)).getValue();
        this.writing = new BooleanWrapper(tuple.getElementNotNull(2)).getValue();
        this.context = tuple.isElementNull(3) ? null : Context.get(entity, tuple.getElementNotNull(3));
        this.contact = tuple.isElementNull(4) ? null : Contact.get(entity, tuple.getElementNotNull(4));
        
        if (context != null && contact != null) throw new InvalidEncodingException("The context and the contact are not null.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(5);
        elements.set(0, new BooleanWrapper(CLIENT_TYPE, client).toBlock());
        elements.set(1, new BooleanWrapper(ROLE_TYPE, role).toBlock());
        elements.set(2, new BooleanWrapper(WRITING_TYPE, writing).toBlock());
        elements.set(3, Block.toBlock(CONTEXT_TYPE, context));
        elements.set(4, Block.toBlock(CONTACT_TYPE, contact));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns whether the authorization is restricted to clients.
     * 
     * @return whether the authorization is restricted to clients.
     */
    @Pure
    public boolean isClient() {
        return client;
    }
    
    /**
     * Returns whether the authorization is restricted to agents that can assume incoming roles.
     * 
     * @return whether the authorization is restricted to agents that can assume incoming roles.
     */
    @Pure
    public boolean isRole() {
        return role;
    }
    
    /**
     * Returns whether the authorization is restricted to agents that can write to contexts.
     * 
     * @return whether the authorization is restricted to agents that can write to contexts.
     */
    @Pure
    public boolean isWriting() {
        return writing;
    }
    
    /**
     * Returns the context to which the authorization is restricted (or null).
     * 
     * @return the context to which the authorization is restricted (or null).
     */
    @Pure
    public @Nullable Context getContext() {
        return context;
    }
    
    /**
     * Returns the contact to which the authorization is restricted (or null).
     * 
     * @return the contact to which the authorization is restricted (or null).
     */
    @Pure
    public @Nullable Contact getContact() {
        return contact;
    }
    
    
    /**
     * Checks that the authorization is restricted to clients and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsClient() throws PacketException {
        if (!isClient()) throw new PacketException(PacketError.AUTHORIZATION, "The action is restricted to clients.");
    }
    
    /**
     * Checks that the authorization is restricted to agents that can assume incoming roles and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsRole() throws PacketException {
        if (!isRole()) throw new PacketException(PacketError.AUTHORIZATION, "The action is restricted to agents that can assume incoming roles.");
    }
    
    /**
     * Checks that the authorization is restricted to agents that can write to contexts and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsWriting() throws PacketException {
        if (!isWriting()) throw new PacketException(PacketError.AUTHORIZATION, "The action is restricted to agents that can write to contexts.");
    }
    
    
    /**
     * Returns whether these restrictions cover the given context.
     * 
     * @param other the context that needs to be covered.
     * 
     * @return whether these restrictions cover the given context.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Context other) throws SQLException {
        return context != null && context.isSupercontextOf(other);
    }
    
    /**
     * Checks that these restrictions cover the given context and throws a {@link PacketException} if not.
     * 
     * @param other the context that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Context other) throws PacketException, SQLException {
        if (!cover(other)) throw new PacketException(PacketError.AUTHORIZATION, "The restrictions of the agent do not cover the necessary context.");
    }
    
    /**
     * Returns whether these restrictions cover the given contact.
     * 
     * @param other the contact that needs to be covered.
     * 
     * @return whether these restrictions cover the given contact.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Contact other) throws SQLException {
        return context != null && !context.contains(other) || contact != null && !contact.equals(other);
    }
    
    /**
     * Checks that these restrictions cover the given contact and throws a {@link PacketException} if not.
     * 
     * @param other the contact that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Contact other) throws PacketException, SQLException {
        if (!cover(other)) throw new PacketException(PacketError.AUTHORIZATION, "The restrictions of the agent do not cover the necessary contact.");
    }
    
    /**
     * Returns whether these restrictions cover the given restrictions.
     * 
     * @param other the restrictions that need to be covered.
     * 
     * @return whether these restrictions cover the given restrictions.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Restrictions other) throws SQLException {
        if (other.client && !client) return false;
        if (other.role && !role) return false;
        if (other.writing && !writing) return false;
        final @Nullable Context context = other.context;
        if (context != null && !cover(context)) return false;
        final @Nullable Contact contact = other.contact;
        return contact == null || cover(contact);
    }
    
    /**
     * Checks whether these restrictions cover the given restrictions and throws a {@link PacketException} if not.
     * 
     * @param restrictions the restrictions that need to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Restrictions restrictions) throws PacketException, SQLException {
        if (!cover(restrictions)) throw new PacketException(PacketError.AUTHORIZATION, "The restrictions of the agent do not cover the necessary restrictions.");
    }
    
    
    /**
     * Returns whether these restrictions match the given agent.
     * 
     * @param agent the agent which needs to be matched.
     * 
     * @return whether these restrictions match the given agent.
     */
    @Pure
    public boolean match(@Nonnull Agent agent) {
        return isClient() == agent.isClient() && (context == null || context.getEntity().equals(agent.getEntity())) && (contact == null || contact.getEntity().equals(agent.getEntity()));
    }
    
    /**
     * Checks that these restrictions match the given agent.
     * 
     * @param agent the agent which needs to be matched.
     * 
     * @return these restrictions.
     * 
     * @throws InvalidEncodingException otherwise.
     */
    @Pure
    public @Nonnull Restrictions checkMatch(@Nonnull Agent agent) throws InvalidEncodingException {
        if (!match(agent)) throw new InvalidEncodingException("The restrictions do not match the given agent.");
        return this;
    }
    
    
    /**
     * Returns these restrictions restricted by the given restrictions (except the context and contact, which are left unaffected).
     * 
     * @param restrictions the restrictions with which to restrict these restrictions.
     * 
     * @return these restrictions restricted by the given restrictions (except the context and contact, which are left unaffected).
     */
    @Pure
    public @Nonnull Restrictions restrictTo(@Nonnull Restrictions restrictions) {
        return new Restrictions(client && restrictions.client, role && restrictions.role, writing && restrictions.writing, context, contact);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Restrictions)) return false;
        @Nonnull Restrictions other = (Restrictions) object;
        return this.client == other.client && this.role == other.role && this.writing == other.writing && Objects.equals(this.context, other.context) && Objects.equals(this.contact, other.contact);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (client ? 1 : 0);
        hash = 19 * hash + (role ? 1 : 0);
        hash = 19 * hash + (writing ? 1 : 0);
        hash = 19 * hash + Objects.hashCode(context);
        hash = 19 * hash + Objects.hashCode(contact);
        return hash;
    }
    
    /**
     * Returns this context as a formatted string.
     * 
     * @return this context as a formatted string.
     */
    @Pure
    public @Nonnull String toFormattedString() {
        return "(Client: " + client + ", Role: " + role + ", Writing: " + writing + ", Context: " + context + ", Contact: " + contact + ")";
    }
    
    
    /**
     * Stores the columns used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "client BOOLEAN NOT NULL, role BOOLEAN NOT NULL, context_writing BOOLEAN NOT NULL, context " + Context.FORMAT + ", contact " + Mapper.FORMAT;
    
    /**
     * Stores the columns used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String COLUMNS = "client, role, context_writing, context, contact";
    
    /**
     * Returns the foreign key constraints used by instances of this class.
     * 
     * @param site the site at which the foreign key constraint is declared.
     * 
     * @return the foreign key constraints used by instances of this class.
     */
    @NonCommitting
    public static @Nonnull String getForeignKeys(@Nonnull Site site) throws SQLException {
        return "FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (contact) " + site.getEntityReference();
    }
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param entity the entity to which the restrictions belong.
     * @param resultSet the result set to retrieve the data from.
     * @param startIndex the start index of the columns containing the data.
     * 
     * @return the given columns of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Restrictions get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int startIndex) throws SQLException {
        final boolean client = resultSet.getBoolean(startIndex + 0);
        final boolean role = resultSet.getBoolean(startIndex + 1);
        final boolean writing = resultSet.getBoolean(startIndex + 2);
        final @Nullable Context context = Context.get(entity, resultSet, startIndex + 3);
        final @Nullable Contact contact = Contact.get(entity, resultSet, startIndex + 4);
        return new Restrictions(client, role, writing, context, contact);
     }
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws SQLException {
        preparedStatement.setBoolean(startIndex + 0, client);
        preparedStatement.setBoolean(startIndex + 1, role);
        preparedStatement.setBoolean(startIndex + 2, writing);
        Context.set(context, preparedStatement, startIndex + 3);
        Contact.set(contact, preparedStatement, startIndex + 4);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return client + ", " + role + ", " + writing + ", " + context + ", " + contact;
    }
    
    /**
     * Returns these restrictions as update values.
     * 
     * @return these restrictions as update values.
     */
    @Pure
    public @Nonnull String toUpdateValues() {
        return "client = " + client + ", role = " + role + ", context_writing = " + writing + ", context = " + context + ", contact = " + contact;
    }
    
    /**
     * Returns these restrictions as update condition.
     * 
     * @return these restrictions as update condition.
     */
    @Pure
    public @Nonnull String toUpdateCondition() {
        return "client = " + client + " AND role = " + role + " AND context_writing = " + writing + " AND context " + (context == null ? "IS NULL" : "= " + context) + " AND contact " + (contact == null ? "IS NULL" : "= " + contact);
    }
    
}
