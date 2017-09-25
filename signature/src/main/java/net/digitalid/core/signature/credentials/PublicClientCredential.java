package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.SaltedAgentPermissions;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PublicClientCredential {
    
    @Pure
    public abstract @Nonnull ExposedExponent getExposedExponent();
    
    @Pure
    public abstract @Nonnull Element getC();
    
    @Pure
    public abstract @Nonnull Exponent getSe();
    
    @Pure
    public abstract @Nonnull Exponent getSb();
    
    @Pure
    public abstract @Nullable SaltedAgentPermissions getSaltedAgentPermissions();
    
    @Pure
    public abstract @Nullable Exponent getI();
    
    @Pure
    public abstract @Nullable Exponent getSi();
    
    @Pure
    public abstract @Nullable VerifiableEncryption getVerifiableEncryption();
    
}
