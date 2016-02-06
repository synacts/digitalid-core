package net.digitalid.core.packet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.reference.RawRecipient;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.synchronizer.Audit;
import net.digitalid.core.synchronizer.RequestAudit;
import net.digitalid.core.synchronizer.Sender;

import net.digitalid.service.core.auxiliary.Time;

import net.digitalid.core.conversion.wrappers.CompressionWrapper;

import net.digitalid.core.conversion.wrappers.signature.ClientSignatureWrapper;

import net.digitalid.core.agent.ClientAgentCommitmentReplace;

import net.digitalid.core.entity.NativeRole;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.Method;

import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;

import net.digitalid.core.client.Client;
import net.digitalid.core.client.Commitment;
import net.digitalid.core.client.SecretCommitment;

/**
 * This class compresses, signs and encrypts requests by clients.
 */
@Immutable
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
     * @require !methods.isEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require methods.getNotNull(0).isOnClient() : "The methods are on a client.";
     */
    @NonCommitting
    public ClientRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull SecretCommitment commitment) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
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
    @NonCommitting
    private ClientRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nonnull SecretCommitment commitment, int iteration) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(methods, subject.getHostIdentifier(), getSymmetricKey(subject.getHostIdentifier(), Time.HOUR), subject, audit, commitment, iteration);
    }
    
    
    @Override
    @RawRecipient
    void setField(@Nullable Object field) {
        Require.that(field != null).orThrow("See the constructor above.");
        this.commitment = (SecretCommitment) field;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nonnull ClientSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        return ClientSignatureWrapper.sign(Packet.SIGNATURE, compression, subject, audit, commitment);
    }
    
    
    @Pure
    @Override
    public boolean isSigned() {
        return true;
    }
    
    @Override
    @NonCommitting
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        if (!subject.getHostIdentifier().equals(recipient)) { throw InternalException.get("The host of the subject " + subject + " does not match the recipient " + recipient + "."); }
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
    @NonCommitting
    @Nonnull Response recommit(@Nonnull FreezableList<Method> methods, int iteration, boolean verified) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull NativeRole role = methods.getNonNullable(0).getRole().toNativeRole();
        final @Nonnull Client client = role.getClient();
        final @Nonnull Commitment oldCommitment = role.getAgent().getCommitment();
        final @Nonnull Commitment newCommitment = client.getCommitment(role.getIssuer().getAddress());
        final @Nonnull ClientAgentCommitmentReplace action = new ClientAgentCommitmentReplace(role.getAgent(), oldCommitment, newCommitment);
        final @Nullable RequestAudit newAudit = Sender.runAsynchronously(action, getAudit());
        return new ClientRequest(methods, getSubject(), newAudit, newCommitment.addSecret(client.getSecret()), iteration).send(verified);
    }
    
}
