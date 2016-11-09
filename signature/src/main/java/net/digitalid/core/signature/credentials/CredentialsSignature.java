package net.digitalid.core.signature.credentials;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class CredentialsSignature<T> extends Signature<T> {
    
    @Pure
    public abstract @Nonnull Exponent getT();
    
    @Pure
    public abstract @Nonnull Exponent getSU();
    
    // TODO: unclear if this must be in the credentials-signature type.
    @Pure
    public abstract @Nullable Exponent getV();
    
    @Pure
    public abstract @Nonnull Exponent getSV();
    
    @Pure
    public abstract boolean isLodged();
    
    @Pure
    public abstract @Nonnull List<@Nonnull ClientCredential> getCredentials();
    
    @Pure
    public abstract @Nonnull List<@Nonnull CertifiedAttributeValue> getCertificates();
    
}
