package net.digitalid.core.handler.method.action;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingle;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.compression.Compression;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.host.HostSignatureBuilder;

/**
 * External actions can be sent by both hosts and clients.
 * Depending on whether the reply is needed immediately, external actions can be either sent directly or passed to the pusher!
 * 
 * @invariant hasEntity() : "This external action has an entity.";
 */
@Immutable
public abstract class ExternalAction extends Action {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    @Pure
    @TODO(task = "Do we have to provide the value here rather than in the signature?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    public @Nullable BigInteger getCommitmentValue() {
        return null;
    }
 
    /* -------------------------------------------------- Request Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Signature<Compression<Pack>> getSignature(@Nonnull Compression<Pack> compression) {
    
        Require.that(getEntity() != null).orThrow("The entity must not be null in case of external actions");
//        if (getEntity() instanceof Role) {
    
//            final @Nonnull ClientCredential credential = null;//ClientCredential.getIdentityBased((Role) entity, getRequiredPermissions(methods));
//    
//            throw new UnsupportedOperationException("Client credentials are not yet implemented");
//            return CredentialsSignatureBuilder.withObject(compression).withSubject(getSubject()).withT(t).withSU(su).withSV(sv).withLodged(isLodged()).withCredentials(FreezableArrayList.withElement(credential)).withCertificates(FreezableArrayList.withElement(certificate)).build();
//        } else {
            return HostSignatureBuilder.withObject(compression).withSubject(getSubject()).withSigner(getEntity().getIdentity().getAddress()).build();
//        }
    }
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof ExternalAction;
    }
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean canBeSentByClients() {
        return true;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * This method is executed after successful transmission.
     */
    @NonCommitting
    @PureWithSideEffects
    public void executeOnSuccess() throws DatabaseException {}
    
    /**
     * This method is executed if an error occurred during pushing.
     */
    @NonCommitting
    @PureWithSideEffects
    public abstract void executeOnFailure() throws DatabaseException;
    
    /* -------------------------------------------------- Audit on Failure -------------------------------------------------- */
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull @EmptyOrSingle ReadOnlyAgentPermissions getFailedAuditPermissions() {
        return ReadOnlyAgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull Restrictions getFailedAuditRestrictions() {
        return Restrictions.MIN;
    }
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    @NonCommitting
    public @Nullable Agent getFailedAuditAgent() throws DatabaseException {
        return null;
    }
    
}
