package net.digitalid.service.core.packet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.wrappers.CompressionWrapper;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.utility.annotations.reference.RawRecipient;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class compresses, signs and encrypts requests by hosts.
 */
@Immutable
public final class HostRequest extends Request {
    
    /**
     * Stores the identifier of the signing host.
     */
    private @Nonnull InternalIdentifier signer;
    
    /**
     * Packs the given methods with the given arguments signed by the given host.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param signer the identifier of the signing host.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require !methods.isEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     */
    @NonCommitting
    public HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer) throws DatabaseException, PacketException, ExternalException, NetworkException {
        this(methods, recipient, subject, signer, 0);
    }
    
    /**
     * Packs the given methods with the given arguments signed by the given host.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param signer the identifier of the signing host.
     * @param iteration how many times this request was resent.
     */
    @NonCommitting
    private HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer, int iteration) throws DatabaseException, PacketException, ExternalException, NetworkException {
        super(methods, recipient, getSymmetricKey(recipient, Time.TROPICAL_YEAR), subject, null, signer, iteration);
    }
    
    
    @Override
    @RawRecipient
    void setField(@Nullable Object field) {
        assert field != null : "See the constructor above.";
        this.signer = (InternalIdentifier) field;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nonnull HostSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        return new HostSignatureWrapper(Packet.SIGNATURE, compression, subject, audit, signer);
    }
    
    
    @Pure
    @Override
    public boolean isSigned() {
        return true;
    }
    
    @Override
    @NonCommitting
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws DatabaseException, PacketException, ExternalException, NetworkException {
        return new HostRequest(methods, recipient, subject, signer).send(verified);
    }
    
}
