package net.digitalid.core.signature;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class signs the wrapped object for encoding.
 * 
 * @see HostSignature
 * @see ClientSignature
 * TODO: CredentialsSignature
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class Signature<@Unspecifiable OBJECT> extends RootClass {
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    /**
     * Returns the wrapped object that has been or will be signed.
     */
    @Pure
    public abstract @Nonnull OBJECT getObject();
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * Returns the time at which the object has been or will be signed.
     */
    @Pure
    @PrimaryKey // TODO: The current time might not be unique enough.
    @Default("net.digitalid.utility.time.TimeBuilder.build()")
    public abstract @Nonnull @Positive Time getTime();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the subject about which a statement is made.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Returns whether this signature has been verified.
     */
    @Pure
    public boolean isVerified() {
        // TODO.
        return false;
    }
    
}
