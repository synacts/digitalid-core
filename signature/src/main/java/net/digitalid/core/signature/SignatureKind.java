package net.digitalid.core.signature;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.CaseException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

/**
 * This class enumerates the kind of signatures that are supported.
 */
@Immutable
@GenerateConverter
public enum SignatureKind {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * The signature is null.
     */
    NULL(0),
    
    /**
     * The object is not signed at all.
     */
    NONE(1),
    
    /**
     * The object is signed by a host.
     */
    HOST(2),
    
    /**
     * The object is signed by a client.
     */
    CLIENT(3),
    
    /**
     * The object is signed with credentials.
     */
    CREDENTIAL(4);
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns whether the given value denotes a valid signature kind.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 4;
    }
    
    private final @Valid byte value;
    
    /**
     * Returns the byte representation of this signature kind.
     */
    @Pure
    public @Valid byte getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private SignatureKind(int value) {
        this.value = (byte) value;
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the signature kind denoted by the given value.
     */
    @Pure
    @Recover
    public static @Nonnull SignatureKind of(@Valid byte value) {
        Require.that(isValid(value)).orThrow("The value has to be valid but was $.", value);
        
        for (@Nonnull SignatureKind kind : values()) {
            if (kind.value == value) { return kind; }
        }
        
        throw CaseException.with("value", value);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Strings.capitalizeFirstLetters(Strings.desnake(name()));
    }
    
}
