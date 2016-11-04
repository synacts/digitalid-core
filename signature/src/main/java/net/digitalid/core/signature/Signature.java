package net.digitalid.core.signature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.host.HostSignature;

/**
 * @see HostSignature
 * @see ClientSignature
 * @see CredentialsSignature
 */
@Immutable
//@GenerateConverter // TODO: Support the generation of converters for types with generic parameters.
public abstract class Signature<T> extends RootClass {
    
    @Pure
    public abstract @Nullable InternalIdentifier getSubject();
    
    @Pure
    public abstract @Nullable @Positive Time getTime();
    
    @Pure
    public abstract @Nullable T getElement();
    
    @Pure
    public boolean isVerified() {
        // TODO.
        return false;
    }
    
    @Pure
//    @Recover
    public static <T> @Nonnull Signature<T> with() {
        // TODO
        return null;
    }
    
    // TODO: Do we need to add the Audit?!
    
}
