package ch.virtualid.packet;

import ch.virtualid.agent.ClientAgentCommitmentReplace;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.annotations.RawRecipient;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.entity.NativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.synchronizer.Audit;
import ch.virtualid.synchronizer.RequestAudit;
import ch.virtualid.synchronizer.Sender;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CompressionWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class compresses, signs and encrypts requests by clients.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ClientRequest extends Request {
    
    /**
     * Stores the commitment containing the client secret.
     */
    private @Nonnull SecretCommitment commitment;
    
    /**
     * Packs the given methods with the given arguments signed by the given client.
     * 
     * @param methods the methods of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param commitment the commitment containing the client secret.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require methods.getNotNull(0).isOnClient() : "The methods are on a client.";
     */
    @DoesNotCommit
    public ClientRequest(@Nonnull ReadonlyList<Method> methods, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull SecretCommitment commitment) throws SQLException, IOException, PacketException, ExternalException {
        this(methods, subject, audit, commitment, 0);
    }
    
    /**
     * Packs the given methods with the given arguments signed by the given client.
     * 
     * @param methods the methods of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param commitment the commitment containing the client secret.
     * @param iteration how many times this request was resent.
     */
    @DoesNotCommit
    private ClientRequest(@Nonnull ReadonlyList<Method> methods, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull SecretCommitment commitment, int iteration) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, subject.getHostIdentifier(), getSymmetricKey(subject.getHostIdentifier(), Time.HOUR), subject, audit, commitment, iteration);
    }
    
    
    @Override
    @RawRecipient
    void setField(@Nullable Object field) {
        assert field != null : "See the constructor above.";
        this.commitment = (SecretCommitment) field;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nonnull ClientSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        return new ClientSignatureWrapper(Packet.SIGNATURE, compression, subject, audit, commitment);
    }
    
    
    @Pure
    @Override
    public boolean isSigned() {
        return true;
    }
    
    @Override
    @DoesNotCommit
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        if (!subject.getHostIdentifier().equals(recipient)) throw new PacketException(PacketError.INTERNAL, "The host of the subject " + subject + " does not match the recipient " + recipient + ".");
        return new ClientRequest(methods, subject, getAudit(), commitment).send(verified);
    }
    
    /**
     * Recommits the client secret to the current public key of the recipient and resends this request.
     * 
     * @param methods the methods of this request.
     * @param iteration how many times this request was resent.
     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
     * 
     * @return the response to the resent request with the new commitment.
     */
    @DoesNotCommit
    @Nonnull Response recommit(@Nonnull FreezableList<Method> methods, int iteration, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull NativeRole role = methods.getNotNull(0).getRole().toNativeRole();
        final @Nonnull Client client = role.getClient();
        final @Nonnull Commitment oldCommitment = role.getAgent().getCommitment();
        final @Nonnull Commitment newCommitment = client.getCommitment(role.getIssuer().getAddress());
        final @Nonnull ClientAgentCommitmentReplace action = new ClientAgentCommitmentReplace(role.getAgent(), oldCommitment, newCommitment);
        final @Nullable RequestAudit newAudit = Sender.runAsynchronously(action, getAudit());
        return new ClientRequest(methods, getSubject(), newAudit, newCommitment.addSecret(client.getSecret()), iteration).send(verified);
    }
    
}
