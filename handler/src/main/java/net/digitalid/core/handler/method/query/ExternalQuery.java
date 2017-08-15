package net.digitalid.core.handler.method.query;

import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.functional.failable.FailableBinaryFunction;
import net.digitalid.utility.functional.failable.FailableUnaryFunction;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureBuilder;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;

/**
 * External queries can be sent by both hosts and clients.
 */
@Immutable
public abstract class ExternalQuery<ENTITY extends Entity> extends Query<ENTITY> {
    
    @Pure
    public @Nonnull ReadOnlyAuthentications getAuthentications() throws ExternalException {
        final @Nonnull Identity identity = getSubject().resolve();
//        if (getEntity() != null && getEntity() instanceof Role && identity instanceof Person) {
//            return Contact.get((Role) entity, (Person) identity).getAuthentications();
//        } else {
            return FreezableAuthentications.NONE;
//        }
    }
    
    /* -------------------------------------------------- Request Signature -------------------------------------------------- */
    
    public static final @Nonnull Configuration<FailableBinaryFunction<ExternalQuery, ReadOnlyAgentPermissions, FreezableList<ClientCredential>, ExternalException>> clientCredentialsInjection = Configuration.withUnknownProvider();
    
    public static final @Nonnull Configuration<FailableUnaryFunction<ExternalQuery, FreezableList<CertifiedAttributeValue>, ExternalException>> certificatesInjection = Configuration.withUnknownProvider();
    
    @Pure
    @Override
    public @Nonnull Signature<@Nonnull Compression<@Nonnull Pack>> getSignature(@Nonnull Compression<@Nonnull Pack> compression) throws ExternalException {
        
            if (getAuthentications().isEmpty()) {
                return SignatureBuilder.withObject(compression).withSubject(getSubject()).build();
            } else {
                Require.that(getEntity() != null).orThrow("Entity must not be null");
                final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissionsToExecuteMethod();
                final @Nonnull FreezableList<@Nonnull ClientCredential> credentials = clientCredentialsInjection.get().evaluate(this, permissions);
                final @Nullable FreezableList<@Nonnull CertifiedAttributeValue> certificates = certificatesInjection.get().evaluate(this);

                final @Nonnull SecureRandom random = new SecureRandom();
                for (@Nonnull ClientCredential clientCredential : credentials) {
                    
//                    clientCredential.getC().pow(res)
                }
                // content = tuple(subject, time, element, audit)
                // ts: list of the same size as the credentials, ts entry: tuple(clientCredential.getC())
                // t = hash(content) ^ hash(ts) ^ tf
                // su = ru - t * u
//                return CredentialsSignatureBuilder.withObject(compression).withSubject(getSubject()).withT(t).withSU(su).withSV(sv).withLodged(isLodged()).withCredentials(credentials).withCertificates(certificates).build();
                throw new UnsupportedOperationException("The credential signature is not yet implemented");
            }
    }
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof ExternalQuery;
    }
    
}
