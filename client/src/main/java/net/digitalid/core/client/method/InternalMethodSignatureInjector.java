package net.digitalid.core.client.method;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.client.ClientSignatureCreator;

/**
 * Injects the internal method signature function into the internal method configuration.
 * This allows the internal method class to create signed requests without knowledge of 
 * the internals of the client.
 */
@Utility
public abstract class InternalMethodSignatureInjector {
    
    /**
     * Creates a client signature based on the commitment and secret of the role agent.
     * Only native roles are supported for now.
     * An external exception can be thrown if accessing the commitment of the client fails.
     */
    @Pure
    private static @Nonnull Signature<@Nonnull Compression<@Nonnull Pack>> internalMethodSignatureFunction(@Nonnull InternalMethod internalMethod, @Nonnull Compression<Pack> compression) throws ExternalException {
        Require.that(internalMethod.getEntity() instanceof Role).orThrow("The entity must be a role for all internal methods");
    
        final @Nonnull Role role = (Role) internalMethod.getEntity();
        
        final @Nonnull Restrictions restrictions = role.getAgent().restrictions().get();
        if (!restrictions.cover((internalMethod).getRequiredRestrictionsToExecuteMethod())) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage("The restrictions of the role do not cover the required restrictions.").build(); }
        
        if (internalMethod.getService().equals(CoreService.INSTANCE)) {
            if (role instanceof NativeRole) {
                final @Nonnull NativeRole nativeRole = (NativeRole) role;
                return ClientSignatureCreator.sign(compression, CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).about(internalMethod.getSubject()).with(nativeRole.getAgent().commitment().get().addSecret(role.getUnit().secret.get()));
            } else {
                // see Method for implementation hints
                throw new UnsupportedOperationException("Non-native roles are not yet implemented");
            }
        } else {
            // see Method for implementation hints
            throw new UnsupportedOperationException("Client credential for non-core services are not yet implemented");
        }
    }
    
    /**
     * Injects the internal method signature method into the configuration of the internal method class.
     */
    @PureWithSideEffects
    @Initialize(target = InternalMethod.class)
    public static void injectInternalMethodSignatureFunction() {
        InternalMethod.configuration.set(InternalMethodSignatureInjector::internalMethodSignatureFunction);
    }
    
}
