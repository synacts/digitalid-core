package net.digitalid.core.exceptions.request;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

/**
 * This class enumerates the various request error codes.
 * 
 * @see RequestException
 */
@Immutable
@GenerateConverter
public enum RequestErrorCode {
    
    /* -------------------------------------------------- Error Codes -------------------------------------------------- */
    
    /**
     * The error code for a database problem.
     */
    @Deprecated // TODO: This is an internal problem.
    DATABASE(0),
    
    /**
     * The error code for a network problem.
     */
    @Deprecated // TODO: This is an external problem.
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
     * The error code for a wrong recipient.
     */
    RECIPIENT(15),
    
    /**
     * The error code for a non-existent identity.
     */
    IDENTITY(16),
    
    /**
     * The error code for a relocated identity.
     */
    RELOCATION(17),
    
    /**
     * The error code for a relocated service provider.
     */
    SERVICE(18),
    
    /**
     * The error code for an insufficient authorization.
     */
    AUTHORIZATION(19);
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns whether the given value is a valid request error code.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 19;
    }
    
    private final @Valid byte value;
    
    /**
     * Returns the value of this request error code.
     */
    @Pure
    public @Valid byte getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private RequestErrorCode(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the request error code denoted by the given value.
     */
    @Pure
    @Recover
    public static @Nonnull RequestErrorCode of(@Valid byte value) {
        Require.that(isValid(value)).orThrow("The value has to be a valid request error code but was $.", value);
        
        for (@Nonnull RequestErrorCode code : values()) {
            if (code.value == value) { return code; }
        }
        
        throw CaseExceptionBuilder.withVariable("value").withValue(value).build();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Strings.capitalizeFirstLetters(Strings.desnake(name()));
    }
    
}
