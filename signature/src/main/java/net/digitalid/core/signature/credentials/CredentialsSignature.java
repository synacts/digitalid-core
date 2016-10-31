package net.digitalid.core.signature.credentials;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.signature.Signature;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class CredentialsSignature<T> extends Signature<T> {
    
    public abstract @Nonnull Exponent getT();
    
    public abstract @Nonnull Exponent getSU();
    
    // TODO: unclear if this must be in the credentials-signature type.
    public abstract @Nullable Exponent getV();
    
    public abstract @Nonnull Exponent getSV();
    
    public abstract @Nonnull List<@Nonnull ClientCredential> getCredentials();
    
    public abstract @Nonnull List<@Nonnull CertificateAttributeValue> getCertificates();
    
}
