package net.digitalid.service.core.concepts.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.converter.Converters;
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
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.declaration.CombiningDeclaration;
import net.digitalid.utility.database.declaration.Declaration;

/**
 * This class models the restrictions of an agent.
 * <p>
 * <em>Important:</em> Though this class promises that its objects are immutable, their hash may change!
 * 
 * @invariant getContext() == null || getContact() == null : "The context or the contact is null.";
 */
@Immutable
public final class Restrictions implements XDF<Restrictions, NonHostEntity>, SQL<Restrictions, NonHostEntity> {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores the weakest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MIN = Restrictions.get(false, false, false);
    
    /**
     * Stores the strongest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions MAX = Restrictions.get(true, true, true);
    
    /**
     * Stores the restrictions required to modify clients.
     */
    public static final @Nonnull Restrictions CLIENT = Restrictions.get(true, false, false);
    
    /**
     * Stores the restrictions required to assume incoming roles.
     */
    public static final @Nonnull Restrictions ROLE = Restrictions.get(false, true, false);
    
    /**
     * Stores the restrictions required to write to contexts.
     */
    public static final @Nonnull Restrictions WRITING = Restrictions.get(false, false, true);
    
    /* -------------------------------------------------- Client -------------------------------------------------- */
    
    /**
     * Stores whether the authorization is restricted to clients.
     */
    private final boolean client;
    
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
     * Checks that the authorization is restricted to clients and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsClient() throws PacketException {
        if (!isClient()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to clients.");
    }
    
    /* -------------------------------------------------- Role -------------------------------------------------- */
    
    /**
     * Stores whether the authorization is restricted to agents that can assume incoming roles.
     */
    private final boolean role;
    
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
     * Checks that the authorization is restricted to agents that can assume incoming roles and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsRole() throws PacketException {
        if (!isRole()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to agents that can assume incoming roles.");
    }
    
    /* -------------------------------------------------- Writing -------------------------------------------------- */
    
    /**
     * Stores whether the authorization is restricted to agents that can write to contexts.
     */
    private final boolean writing;
    
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
     * Checks that the authorization is restricted to agents that can write to contexts and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkIsWriting() throws PacketException {
        if (!isWriting()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The action is restricted to agents that can write to contexts.");
    }
    
    /* -------------------------------------------------- Context -------------------------------------------------- */
    
    /**
     * Stores the context to which the authorization is restricted (or null).
     */
    private final @Nullable Context context;
    
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
     * Returns whether these restrictions cover the given context.
     * 
     * @param otherContext the context that needs to be covered.
     * 
     * @return whether these restrictions cover the given context.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Context otherContext) throws AbortException {
        return context != null && context.isSupercontextOf(otherContext);
    }
    
    /**
     * Checks that these restrictions cover the given context and throws a {@link PacketException} if not.
     * 
     * @param otherContext the context that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Context otherContext) throws AbortException, PacketException {
        if (!cover(otherContext)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary context.");
    }
    
    /* -------------------------------------------------- Contact -------------------------------------------------- */
    
    /**
     * Stores the contact to which the authorization is restricted (or null).
     */
    private final @Nullable Contact contact;
    
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
     * Returns whether these restrictions cover the given contact.
     * 
     * @param otherContact the contact that needs to be covered.
     * 
     * @return whether these restrictions cover the given contact.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Contact otherContact) throws AbortException {
        return context != null && !context.contains(otherContact) || contact != null && !contact.equals(otherContact);
    }
    
    /**
     * Checks that these restrictions cover the given contact and throws a {@link PacketException} if not.
     * 
     * @param otherContact the contact that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Contact otherContact) throws AbortException, PacketException {
        if (!cover(otherContact)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary contact.");
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
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
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * 
     * @return new restrictions with the given arguments.
     */
    @Pure
    public static @Nonnull Restrictions get(boolean client, boolean role, boolean writing) {
        return new Restrictions(client, role, writing, null, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * @param context the context to which the authorization is restricted (or null).
     * 
     * @return new restrictions with the given arguments.
     */
    @Pure
    public static @Nonnull Restrictions get(boolean client, boolean role, boolean writing, @Nullable Context context) {
        return new Restrictions(client, role, writing, context, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write to contexts.
     * @param contact the contact to which the authorization is restricted (or null).
     * 
     * @return new restrictions with the given arguments.
     */
    @Pure
    public static @Nonnull Restrictions get(boolean client, boolean role, boolean writing, @Nullable Contact contact) {
        return new Restrictions(client, role, writing, null, contact);
    }
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether these restrictions cover the given restrictions.
     * 
     * @param restrictions the restrictions that need to be covered.
     * 
     * @return whether these restrictions cover the given restrictions.
     */
    @Pure
    @NonCommitting
    public boolean cover(@Nonnull Restrictions restrictions) throws AbortException {
        if (restrictions.client && !client) return false;
        if (restrictions.role && !role) return false;
        if (restrictions.writing && !writing) return false;
        final @Nullable Context context = restrictions.context;
        if (context != null && !cover(context)) return false;
        final @Nullable Contact contact = restrictions.contact;
        return contact == null || cover(contact);
    }
    
    /**
     * Checks whether these restrictions cover the given restrictions and throws a {@link PacketException} if not.
     * 
     * @param restrictions the restrictions that need to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Restrictions restrictions) throws AbortException, PacketException {
        if (!cover(restrictions)) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary restrictions.");
    }
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
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
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Restrictions (Client: " + client + ", Role: " + role + ", Writing: " + writing + ", Context: " + context + ", Contact: " + contact + ")";
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code client.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_TYPE = SemanticType.map("client.restrictions.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code role.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ROLE_TYPE = SemanticType.map("role.restrictions.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code writing.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType WRITING_TYPE = SemanticType.map("writing.restrictions.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
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
    public static final @Nonnull SemanticType TYPE = SemanticType.map("restrictions.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, CLIENT_TYPE, ROLE_TYPE, WRITING_TYPE, CONTEXT_TYPE, CONTACT_TYPE);
    
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
            elements.set(0, BooleanWrapper.encode(restrictions.client, CLIENT_TYPE));
            elements.set(1, BooleanWrapper.encode(restrictions.role, ROLE_TYPE));
            elements.set(2, BooleanWrapper.encode(restrictions.writing, WRITING_TYPE));
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
            
            if (context != null && contact != null) { throw new InvalidEncodingException("The context and the contact are not null."); }
            
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
     * Stores the declaration of this class.
     */
    public static final @Nonnull Declaration DECLARATION = CombiningDeclaration.get(ColumnDeclaration.get("client", BooleanWrapper.SQL_TYPE), ColumnDeclaration.get("role", BooleanWrapper.SQL_TYPE), ColumnDeclaration.get("writing", BooleanWrapper.SQL_TYPE), Context.DECLARATION.nullable(), Contact.DECLARATION.nullable()).prefixedWith("restrictions");
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static final class SQLConverter extends AbstractSQLConverter<Restrictions, NonHostEntity> {
        
        /**
         * Creates a new SQL converter.
         */
        private SQLConverter() {
            super(DECLARATION);
        }
        
        @Pure
        @Override
        public void storeNonNullable(@Nonnull Restrictions restrictions, @NonCapturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
            BooleanWrapper.store(restrictions.client, values, index);
            BooleanWrapper.store(restrictions.role, values, index);
            BooleanWrapper.store(restrictions.writing, values, index);
            Context.SQL_CONVERTER.storeNullable(restrictions.context, values, index);
            Contact.SQL_CONVERTER.storeNullable(restrictions.contact, values, index);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Restrictions restrictions, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws SQLException {
            BooleanWrapper.store(restrictions.client, preparedStatement, parameterIndex);
            BooleanWrapper.store(restrictions.role, preparedStatement, parameterIndex);
            BooleanWrapper.store(restrictions.writing, preparedStatement, parameterIndex);
            Context.SQL_CONVERTER.storeNullable(restrictions.context, preparedStatement, parameterIndex);
            Contact.SQL_CONVERTER.storeNullable(restrictions.contact, preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Restrictions restoreNullable(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws SQLException {
            final boolean client = BooleanWrapper.restore(resultSet, columnIndex);
            final boolean clientWasNull = resultSet.wasNull();
            final boolean role = BooleanWrapper.restore(resultSet, columnIndex);
            final boolean roleWasNull = resultSet.wasNull();
            final boolean writing = BooleanWrapper.restore(resultSet, columnIndex);
            final boolean writingWasNull = resultSet.wasNull();
            final @Nullable Context context = Context.SQL_CONVERTER.restoreNullable(entity, resultSet, columnIndex);
            final @Nullable Contact contact = Contact.SQL_CONVERTER.restoreNullable(entity, resultSet, columnIndex);
            
            if (clientWasNull && roleWasNull && writingWasNull && context == null && contact == null) { return null; }
            if (clientWasNull || roleWasNull || writingWasNull) { throw new SQLException("Found inconsistent restrictions ('client' = '" + client + ", 'role' = '" + role + "', 'writing' = '" + writing + "')."); }
            
            return new Restrictions(client, role, writing, context, contact);
        }
        
    }
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter SQL_CONVERTER = new SQLConverter();
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Restrictions, NonHostEntity> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
