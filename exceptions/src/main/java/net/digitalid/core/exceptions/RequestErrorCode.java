package net.digitalid.core.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;

import net.digitalid.service.core.block.wrappers.value.integer.Integer08Wrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.NonRequestingKeyConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.NonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.identity.SemanticType;

/**
 * This class enumerates the various request error codes.
 * 
 * @see RequestException
 */
@Immutable
public enum RequestErrorCode implements XDF<RequestErrorCode, Object>, SQL<RequestErrorCode, Object> {
    
    /* -------------------------------------------------- Error Codes -------------------------------------------------- */
    
    /**
     * The error code for a database problem.
     */
    DATABASE(0),
    
    /**
     * The error code for a network problem.
     */
    NETWORK(1),
    
    /**
     * The error code for an internal problem.
     */
    INTERNAL(2),
    
    /**
     * The error code for an external problem.
     */
    EXTERNAL(3),
    
    /**
     * The error code for a request problem.
     */
    REQUEST(4),
    
    /**
     * The error code for an invalid packet.
     */
    PACKET(5),
    
    /**
     * The error code for a replayed packet.
     */
    REPLAY(6),
    
    /**
     * The error code for an invalid encryption.
     */
    ENCRYPTION(7),
    
    /**
     * The error code for invalid elements.
     */
    ELEMENTS(8),
    
    /**
     * The error code for an invalid audit.
     */
    AUDIT(9),
    
    /**
     * The error code for an invalid signature.
     */
    SIGNATURE(10),
    
    /**
     * The error code for a required key rotation.
     */
    KEYROTATION(11),
    
    /**
     * The error code for an invalid compression.
     */
    COMPRESSION(12),
    
    /**
     * The error code for an invalid content.
     */
    CONTENT(13),
    
    /**
     * The error code for an invalid method type.
     */
    METHOD(14),
    
    /**
     * The error code for an invalid identifier as subject.
     */
    IDENTIFIER(15),
    
    /**
     * The error code for a relocated identity.
     */
    RELOCATION(16),
    
    /**
     * The error code for a relocated service provider.
     */
    SERVICE(17),
    
    /**
     * The error code for an insufficient authorization.
     */
    AUTHORIZATION(18);
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns whether the given value is a valid request error code.
     *
     * @param value the value to check.
     * 
     * @return whether the given value is a valid request error code.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 18;
    }
    
    /**
     * Stores the value of this request error code.
     */
    private final @Validated byte value;
    
    /**
     * Returns the value of this request error code.
     * 
     * @return the value of this request error code.
     */
    @Pure
    public @Validated byte getValue() {
        return value;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new request error with the given value.
     * 
     * @param value the value encoding the request error.
     */
    private RequestErrorCode(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the request error code encoded by the given value.
     * 
     * @param value the value encoding the request error code.
     * 
     * @return the request error code encoded by the given value.
     */
    @Pure
    public static @Nonnull RequestErrorCode get(@Validated byte value) {
        Require.that(isValid(value)).orThrow("The value is a valid request error.");
        
        for (final @Nonnull RequestErrorCode code : values()) {
            if (code.value == value) { return code; }
        }
        
        throw UnexpectedValueException.with("value", value);
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns the name of this request error.
     * 
     * @return the name of this request error.
     */
    @Pure
    public @Nonnull String getName() {
        final @Nonnull String string = name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    private static final @Nonnull NonRequestingKeyConverter<RequestErrorCode, Object, Byte, Object> KEY_CONVERTER = new NonRequestingKeyConverter<RequestErrorCode, Object, Byte, Object>() {
        
        @Pure
        @Override
        public boolean isValid(@Nonnull Byte value) {
            return RequestErrorCode.isValid(value);
        }
        
        @Pure
        @Override
        public @Nonnull @Validated Byte convert(@Nonnull RequestErrorCode code) {
            return code.value;
        }
        
        @Pure
        @Override
        public @Nonnull RequestErrorCode recover(@Nonnull Object none, @Nonnull @Validated Byte value) {
            return RequestErrorCode.get(value);
        }
        
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code code.error.request@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("code.error.request@core.digitalid.net").load(Integer08Wrapper.XDF_TYPE);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<RequestErrorCode, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueXDFConverter(TYPE));
    
    @Pure
    @Override
    public @Nonnull NonRequestingXDFConverter<RequestErrorCode, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("request_error_code", Integer08Wrapper.SQL_TYPE);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<RequestErrorCode, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueSQLConverter(DECLARATION));
    
    @Pure
    @Override
    public @Nonnull SQLConverter<RequestErrorCode, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<RequestErrorCode, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
