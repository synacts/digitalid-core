package net.digitalid.core.packet;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.RawRecipient;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.synchronizer.Audit;
import net.digitalid.core.wrappers.CompressionWrapper;
import net.digitalid.core.wrappers.HostSignatureWrapper;

/**
 * This class compresses, signs and encrypts requests by hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     */
    @NonCommitting
    public HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer) throws SQLException, IOException, PacketException, ExternalException {
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
    private HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer, int iteration) throws SQLException, IOException, PacketException, ExternalException {
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
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        return new HostRequest(methods, recipient, subject, signer).send(verified);
    }
    
}
