package ch.virtualid.packet;

import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.HostSignatureWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class compresses, signs and encrypts requests with credentials.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CredentialsRequest extends Request {
    
    /**
     * Packs the given methods with the given arguments signed with the given credentials.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param credentials the credentials with which the content is signed.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param commitment the value b' or null if the credentials are not to be shortened.
     * 
     * @require methods.isFrozen() : "The methods are frozen.";
     * @require methods.isNotEmpty() : "The methods are not empty.";
     * @require methods.doesNotContainNull() : "The methods do not contain null.";
     * 
     * @require credentials.isFrozen() : "The credentials are frozen.";
     * @require CredentialsSignatureWrapper.credentialsAreValid(credentials) : "The credentials are valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
     * @require CredentialsSignatureWrapper.certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
     */
    public CredentialsRequest(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger commitment) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, recipient, new SymmetricKey(), subject, audit, null, null, credentials, certificates, lodged, commitment);
    }
    
}
