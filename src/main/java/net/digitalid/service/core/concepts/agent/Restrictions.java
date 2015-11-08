package net.digitalid.service.core.concepts.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.ColumnIndex;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.ComposingSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.site.Site;

/**
 * This class models the restrictions of an agent.
 * <p>
 * <em>Important:</em> Though this class promises that its objects are immutable, their hash may change!
 * 
 * @invariant getContext() == null || getContact() == null : "The context or the contact is null.";
 */
@Immutable
public final class Restrictions implements XDF<Restrictions, NonHostEntity>, SQL<Restrictions, NonHostEntity> {
    
    
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
        if (!isClient()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to clients.");
    }
    
    /**
     * Checks that the authorization is restricted to agents that can assume incoming roles and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsRole() throws PacketException {
        if (!isRole()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to agents that can assume incoming roles.");
    }
    
    /**
     * Checks that the authorization is restricted to agents that can write to contexts and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsWriting() throws PacketException {
        if (!isWriting()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to agents that can write to contexts.");
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
    public boolean cover(@Nonnull Context other) throws AbortException {
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
        if (!cover(other)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary context.");
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
    public boolean cover(@Nonnull Contact other) throws AbortException {
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
        if (!cover(other)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary contact.");
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
    public boolean cover(@Nonnull Restrictions other) throws AbortException {
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
        if (!cover(restrictions)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary restrictions.");
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
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code client.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_TYPE = SemanticType.map("client.restrictions.agent@core.digitalid.net").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code role.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ROLE_TYPE = SemanticType.map("role.restrictions.agent@core.digitalid.net").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code writing.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType WRITING_TYPE = SemanticType.map("writing.restrictions.agent@core.digitalid.net").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code context.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CONTEXT_TYPE = SemanticType.map("context.restrictions.agent@core.digitalid.net").load(Context.TYPE);
    
    /**
     * Stores the semantic type {@code contact.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CONTACT_TYPE = SemanticType.map("contact.restrictions.agent@core.digitalid.net").load(Contact.TYPE);
    
    /**
     * Stores the semantic type {@code restrictions.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("restrictions.agent@core.digitalid.net").load(TupleWrapper.TYPE, CLIENT_TYPE, ROLE_TYPE, WRITING_TYPE, CONTEXT_TYPE, CONTACT_TYPE);
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractXDFConverter<Restrictions, NonHostEntity> {
        
        /**
         * Creates a new XDF converter.
         */
        private XDFConverter() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Restrictions restrictions) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(5);
            elements.set(0, BooleanWrapper.encode(CLIENT_TYPE, restrictions.client));
            elements.set(1, BooleanWrapper.encode(ROLE_TYPE, restrictions.role));
            elements.set(2, BooleanWrapper.encode(WRITING_TYPE, restrictions.writing));
            elements.set(3, ConvertToXDF.nullable(restrictions.context, CONTEXT_TYPE));
            elements.set(4, ConvertToXDF.nullable(restrictions.contact, CONTACT_TYPE));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull Restrictions decodeNonNullable(@Nonnull NonHostEntity entity, @Nonnull @BasedOn("restrictions.agent@core.digitalid.net") Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
            
            final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
            final boolean client = BooleanWrapper.decode(tuple.getNonNullableElement(0));
            final boolean role = BooleanWrapper.decode(tuple.getNonNullableElement(1));
            final boolean writing = BooleanWrapper.decode(tuple.getNonNullableElement(2));
            final @Nullable Context context = Context.XDF_CONVERTER.decodeNullable(entity, tuple.getNullableElement(3));
            final @Nullable Contact contact = Contact.XDF_CONVERTER.decodeNullable(entity, tuple.getNullableElement(4));
            
            if (context != null && contact != null) throw new InvalidEncodingException("The context and the contact are not null.");
            
            return new Restrictions(client, role, writing, context, contact);
        }
        
    }
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter();
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static final class SQLConverter extends ComposingSQLConverter<Restrictions, NonHostEntity> {
        
        /**
         * Stores the SQL converter for the client field.
         */
        private final @Nonnull AbstractSQLConverter<Boolean, Object> clientSQLConverter;
        
        /**
         * Stores the SQL converter for the role field.
         */
        private final @Nonnull AbstractSQLConverter<Boolean, Object> roleSQLConverter;
        
        /**
         * Stores the SQL converter for the writing field.
         */
        private final @Nonnull AbstractSQLConverter<Boolean, Object> contextWritingSQLConverter;
        
        /**
         * Creates a new SQL converter.
         */
        private SQLConverter(@Nonnull AbstractSQLConverter<Boolean, Object> clientSQLConverter, @Nonnull AbstractSQLConverter<Boolean, Object> roleSQLConverter, @Nonnull AbstractSQLConverter<Boolean, Object> contextWritingSQLConverter) {
            super(clientSQLConverter, roleSQLConverter, contextWritingSQLConverter, Context.SQL_CONVERTER, Contact.SQL_CONVERTER);
            this.clientSQLConverter = clientSQLConverter;
            this.roleSQLConverter = roleSQLConverter;
            this.contextWritingSQLConverter = contextWritingSQLConverter;
        }
        
        @Pure
        @Override
        public void getValues(@Nonnull final Restrictions restrictions, @NonCapturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> values, @Nonnull ColumnIndex columnIndex) {
            clientSQLConverter.getValues(restrictions.client, values, columnIndex);
            roleSQLConverter.getValues(restrictions.role, values, columnIndex);
            contextWritingSQLConverter.getValues(restrictions.writing, values, columnIndex);
            Context.SQL_CONVERTER.getValuesOrNulls(restrictions.context, values, columnIndex);
            Contact.SQL_CONVERTER.getValuesOrNulls(restrictions.contact, values, columnIndex);
        }
        
        @Override
        @NonCommitting
        @SuppressWarnings("AssignmentToMethodParameter")
        public void storeNonNullable(@Nonnull Restrictions restrictions, @Nonnull PreparedStatement preparedStatement, ColumnIndex parameterIndex) throws SQLException {
            clientSQLConverter.storeNonNullable(restrictions.client, preparedStatement, parameterIndex);
            roleSQLConverter.storeNonNullable(restrictions.role, preparedStatement, parameterIndex);
            contextWritingSQLConverter.storeNonNullable(restrictions.writing, preparedStatement, parameterIndex);
            
            Context.SQL_CONVERTER.storeNullable(restrictions.context, preparedStatement, parameterIndex);
            parameterIndex += Context.SQL_CONVERTER.getNumberOfColumns();
            Contact.SQL_CONVERTER.storeNullable(restrictions.contact, preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        @SuppressWarnings("AssignmentToMethodParameter")
        public @Nullable Restrictions restoreNullable(final @Nonnull NonHostEntity entity, final @Nonnull ResultSet resultSet, ColumnIndex columnIndex) throws SQLException {
            final @Nullable Boolean client = clientSQLConverter.restoreNullable(None.OBJECT, resultSet, columnIndex);
            // TODO: replace with self-advancing column index.
            final @Nullable Boolean role = roleSQLConverter.restoreNullable(None.OBJECT, resultSet, columnIndex);
            final @Nullable Boolean writing = contextWritingSQLConverter.restoreNullable(None.OBJECT, resultSet, columnIndex);
            
            final @Nullable Context context = Context.SQL_CONVERTER.restoreNullable(entity, resultSet, columnIndex);
            columnIndex += Context.SQL_CONVERTER.getNumberOfColumns();
            
            final @Nullable Contact contact = Contact.SQL_CONVERTER.restoreNullable(entity, resultSet, columnIndex);
            
            if (client == null && role == null && writing == null && context == null && contact == null) { return null; }
            if (client == null || role == null || writing == null) { throw new SQLException("Found inconsistency in restrictions ('client' = '" + client + ", 'role' = '" + role + "', 'writing' = '" + writing + "')."); }
            
            return new Restrictions(client, role, writing, context, contact);
        }
        
    }
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter SQL_CONVERTER = new SQLConverter(BooleanWrapper.getValueSQLConverter("client"), BooleanWrapper.getValueSQLConverter("role"), BooleanWrapper.getValueSQLConverter("context_writing"));
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return SQL_CONVERTER;
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
    public static @Nonnull String getForeignKeys(@Nonnull Site site) throws AbortException {
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
    public static @Nonnull Restrictions get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int startIndex) throws AbortException {
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
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws AbortException {
        preparedStatement.setBoolean(startIndex + 0, client);
        preparedStatement.setBoolean(startIndex + 1, role);
        preparedStatement.setBoolean(startIndex + 2, writing);
        Context.set(context, preparedStatement, startIndex + 3);
        Contact.set(contact, preparedStatement, startIndex + 4);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Database.toBoolean(client) + ", " + Database.toBoolean(role) + ", " + Database.toBoolean(writing) + ", " + context + ", " + contact;
    }
    
    /**
     * Returns these restrictions as update values.
     * 
     * @return these restrictions as update values.
     */
    @Pure
    public @Nonnull String toUpdateValues() {
        return "client = " + Database.toBoolean(client) + ", role = " + Database.toBoolean(role) + ", context_writing = " + Database.toBoolean(writing) + ", context = " + context + ", contact = " + contact;
    }
    
    /**
     * Returns these restrictions as update condition.
     * 
     * @return these restrictions as update condition.
     */
    @Pure
    public @Nonnull String toUpdateCondition() {
        return "client = " + Database.toBoolean(client) + " AND role = " + Database.toBoolean(role) + " AND context_writing = " + Database.toBoolean(writing) + " AND context " + (context == null ? "IS NULL" : "= " + context) + " AND contact " + (contact == null ? "IS NULL" : "= " + contact);
    }
    
}
