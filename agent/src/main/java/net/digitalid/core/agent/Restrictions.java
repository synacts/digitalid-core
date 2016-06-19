package net.digitalid.core.agent;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.declaration.CombiningDeclaration;
import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.value.CorruptParameterValueCombinationException;
import net.digitalid.database.core.exceptions.state.value.CorruptValueException;
import net.digitalid.database.core.interfaces.SelectionResult;
import net.digitalid.database.core.interfaces.ValueCollector;

import net.digitalid.core.contact.Contact;
import net.digitalid.core.context.Context;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.Converters;
import net.digitalid.core.conversion.factory.Factory;
import net.digitalid.core.conversion.factory.Tuple2Factory;
import net.digitalid.core.conversion.format.Tuple2Format;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.conversion.xdf.Encode;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;

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
     * Checks that the authorization is restricted to clients and throws a {@link RequestException} if not.
     */
    @Pure
    public void checkIsClient() throws RequestException {
        if (!isClient()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The action is restricted to clients."); }
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
     * Checks that the authorization is restricted to agents that can assume incoming roles and throws a {@link RequestException} if not.
     */
    @Pure
    public void checkIsRole() throws RequestException {
        if (!isRole()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The action is restricted to agents that can assume incoming roles."); }
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
     * Checks that the authorization is restricted to agents that can write to contexts and throws a {@link RequestException} if not.
     */
    @Pure
    public void checkIsWriting() throws RequestException {
        if (!isWriting()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The action is restricted to agents that can write to contexts."); }
    }
    
    /* -------------------------------------------------- Context -------------------------------------------------- */
    
    /**
     * Stores the context to which the authorization is restricted (or null).
     */
    @ProvideForReconstruction("entity")
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
    public boolean cover(@Nonnull Context otherContext) throws DatabaseException {
        return context != null && context.isSupercontextOf(otherContext);
    }
    
    /**
     * Checks that these restrictions cover the given context and throws a {@link RequestException} if not.
     * 
     * @param otherContext the context that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Context otherContext) throws DatabaseException, RequestException {
        if (!cover(otherContext)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary context."); }
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
    public boolean cover(@Nonnull Contact otherContact) throws DatabaseException {
        return context != null && !context.contains(otherContact) || contact != null && !contact.equals(otherContact);
    }
    
    /**
     * Checks that these restrictions cover the given contact and throws a {@link RequestException} if not.
     * 
     * @param otherContact the contact that needs to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Contact otherContact) throws DatabaseException, RequestException {
        if (!cover(otherContact)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary contact."); }
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
        Require.that(context == null || contact == null).orThrow("The context or the contact is null.");
        
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
    public boolean cover(@Nonnull Restrictions restrictions) throws DatabaseException {
        if (restrictions.client && !client) { return false; }
        if (restrictions.role && !role) { return false; }
        if (restrictions.writing && !writing) { return false; }
        final @Nullable Context context = restrictions.context;
        if (context != null && !cover(context)) { return false; }
        final @Nullable Contact contact = restrictions.contact;
        return contact == null || cover(contact);
    }
    
    /**
     * Checks whether these restrictions cover the given restrictions and throws a {@link RequestException} if not.
     * 
     * @param restrictions the restrictions that need to be covered.
     */
    @Pure
    @NonCommitting
    public void checkCover(@Nonnull Restrictions restrictions) throws DatabaseException, RequestException {
        if (!cover(restrictions)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The restrictions of the agent do not cover the necessary restrictions."); }
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
    public @Nonnull Restrictions checkMatch(@Nonnull Agent agent) throws InvalidEncodingException, InternalException {
        if (!match(agent)) { throw InvalidParameterValueCombinationException.get("The restrictions do not match the given agent."); }
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
        if (object == this) { return true; }
        if (object == null || !(object instanceof Restrictions)) { return false; }
        final @Nonnull Restrictions other = (Restrictions) object;
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
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<Restrictions, NonHostEntity> XDF_CONVERTER = new RequestingXDFConverter<Restrictions, NonHostEntity>(TYPE) {
        
        @Pure
        @Override
        public final @Nonnull Block encodeNonNullable(@Nonnull Restrictions restrictions) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(5);
            elements.set(0, BooleanWrapper.encode(CLIENT_TYPE, restrictions.client));
            elements.set(1, BooleanWrapper.encode(ROLE_TYPE, restrictions.role));
            elements.set(2, BooleanWrapper.encode(WRITING_TYPE, restrictions.writing));
            elements.set(3, Encode.nullable(CONTEXT_TYPE, restrictions.context));
            elements.set(4, Encode.nullable(CONTACT_TYPE, restrictions.contact));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public final @Nonnull Restrictions decodeNonNullable(@Nonnull NonHostEntity entity, @Nonnull Block block) throws ExternalException {
            Require.that(block.getType().isBasedOn(getType())).orThrow("The block is based on the type of this converter.");
            
            final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
            final boolean client = BooleanWrapper.decode(tuple.getNonNullableElement(0));
            final boolean role = BooleanWrapper.decode(tuple.getNonNullableElement(1));
            final boolean writing = BooleanWrapper.decode(tuple.getNonNullableElement(2));
            final @Nullable Context context = Context.XDF_CONVERTER.decodeNullable(entity, tuple.getNullableElement(3));
            final @Nullable Contact contact = Contact.XDF_CONVERTER.decodeNullable(entity, tuple.getNullableElement(4));
            
            if (context != null && contact != null) { throw InvalidParameterValueCombinationException.get("Both the context and the contact are non-null."); }
            
            return new Restrictions(client, role, writing, context, contact);
        }
        
    };
    
    @Pure
    @Override
    public final @Nonnull RequestingXDFConverter<Restrictions, NonHostEntity> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- Converter (Experimental) -------------------------------------------------- */
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull Factory<Restrictions, NonHostEntity> FACTORY = new Tuple2Factory</*O*/ Restrictions, /*E*/ NonHostEntity, /*O1*/ Boolean, /*E1*/ Object, /*O2*/ Context, /*E2*/ NonHostEntity>("restrictions", BooleanWrapper.getConverter("client"), Context.CONVERTER.nullable()) {
        
        @Override
        @NonCommitting
        public final void consumeNonNullable(@Nonnull Restrictions restrictions, @NonCaptured @Nonnull Tuple2Format<?, Boolean, Object, Context, NonHostEntity> format) throws FailedValueStoringException, InternalException {
            format.consume1(restrictions.client);
            format.consume2(restrictions.context);
            // or:
            format.consume(Pair.get(restrictions.client, restrictions.context));
        }
        
        @Override
        @NonCommitting
        public final @Nullable Restrictions produceNullable(@Nonnull NonHostEntity entity, @NonCaptured @Nonnull Tuple2Format<?, Boolean, Object, Context, NonHostEntity> format) throws FailedValueRestoringException, CorruptValueException, InternalException {
            return new Restrictions(format.produce1(None.OBJECT), format.produce2(entity));
        }
        
    };
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Declaration DECLARATION = CombiningDeclaration.get(ColumnDeclaration.get("client", BooleanWrapper.SQL_TYPE), ColumnDeclaration.get("role", BooleanWrapper.SQL_TYPE), ColumnDeclaration.get("writing", BooleanWrapper.SQL_TYPE), Context.DECLARATION.nullable(), Contact.DECLARATION.nullable()).prefixedWith("restrictions");
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Restrictions, NonHostEntity> SQL_CONVERTER = new SQLConverter<Restrictions, NonHostEntity>(DECLARATION) {
        
        @Override
        @NonCommitting
        public final void storeNonNullable(@Nonnull Restrictions restrictions, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
            BooleanWrapper.store(restrictions.client, collector);
            BooleanWrapper.store(restrictions.role, collector);
            BooleanWrapper.store(restrictions.writing, collector);
            Context.SQL_CONVERTER.storeNullable(restrictions.context, collector);
            Contact.SQL_CONVERTER.storeNullable(restrictions.contact, collector);
        }
        
        @Override
        @NonCommitting
        public final @Nullable Restrictions restoreNullable(@Nonnull NonHostEntity entity, @NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
            final boolean client = BooleanWrapper.restore(result);
            final boolean clientWasNull = result.wasNull();
            final boolean role = BooleanWrapper.restore(result);
            final boolean roleWasNull = result.wasNull();
            final boolean writing = BooleanWrapper.restore(result);
            final boolean writingWasNull = result.wasNull();
            final @Nullable Context context = Context.SQL_CONVERTER.restoreNullable(entity, result);
            final @Nullable Contact contact = Contact.SQL_CONVERTER.restoreNullable(entity, result);
            
            if (clientWasNull && roleWasNull && writingWasNull && context == null && contact == null) { return null; }
            if (clientWasNull || roleWasNull || writingWasNull) { throw CorruptParameterValueCombinationException.get("Found inconsistent restrictions ('client' = '" + client + ", 'role' = '" + role + "', 'writing' = '" + writing + "')."); }
            if (context != null && contact != null) { throw CorruptParameterValueCombinationException.get("Both the context and the contact are non-null."); }
            
            return new Restrictions(client, role, writing, context, contact);
        }
        
    };
    
    @Pure
    @Override
    public final @Nonnull SQLConverter<Restrictions, NonHostEntity> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Restrictions, NonHostEntity> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
