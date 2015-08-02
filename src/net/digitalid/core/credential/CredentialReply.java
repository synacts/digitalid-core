package net.digitalid.core.credential;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.RandomizedAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.service.CoreServiceQueryReply;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Replies the parameters of a new credential.
 * 
 * @see CredentialInternalQuery
 * @see CredentialExternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class CredentialReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code c.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType C = SemanticType.map("c.credential@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code e.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType E = SemanticType.map("e.credential@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code i.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType I = SemanticType.map("i.credential@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code reply.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("reply.credential@core.digitalid.net").load(TupleWrapper.TYPE, Restrictions.TYPE, Time.TYPE, C, E, I);
    
    
    /**
     * Stores the public key of the host that issued the credential.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Stores the restrictions for which the credential is issued.
     */
    private final @Nullable Restrictions restrictions;
    
    /**
     * Stores the issuance time rounded down to the last half-hour.
     */
    private final @Nonnull Time issuance;
    
    /**
     * Stores the certifying base of the issued credential.
     */
    private final @Nonnull Element c;
    
    /**
     * Stores the certifying exponent of the issued credential.
     */
    private final @Nonnull Exponent e;
    
    /**
     * Stores the serial number of the issued credential.
     */
    private final @Nonnull Exponent i;
    
    /**
     * Creates a query reply for the parameters of a new credential.
     * 
     * @param account the account to which this query reply belongs.
     * @param publicKey the public key of the host that issued the credential.
     * @param restrictions the restrictions for which the credential is issued.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param c the certifying base of the issued credential.
     * @param e the certifying exponent of the issued credential.
     * @param i the serial number of the issued credential.
     */
    CredentialReply(@Nonnull NonHostAccount account, @Nonnull PublicKey publicKey, @Nullable Restrictions restrictions, @Nonnull Time issuance, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent i) {
        super(account);
        
        this.publicKey = publicKey;
        this.restrictions = restrictions;
        this.issuance = issuance;
        this.c = c;
        this.e = e;
        this.i = i;
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * @param block the content which is to be decoded.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    @NonCommitting
    private CredentialReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        if (!hasEntity()) throw new InvalidEncodingException("A credential reply must have an entity.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.restrictions = tuple.isElementNotNull(0) ? new Restrictions(entity, tuple.getElementNotNull(0)) : null;
        this.issuance = new Time(tuple.getElementNotNull(1));
        this.publicKey = Cache.getPublicKey(signature.getSubjectNotNull().getHostIdentifier(), issuance);
        this.c = publicKey.getCompositeGroup().getElement(tuple.getElementNotNull(2));
        this.e = new Exponent(tuple.getElementNotNull(3));
        this.i = new Exponent(tuple.getElementNotNull(4));
        
        if (issuance.isLessThan(Time.HOUR.ago())) throw new InvalidEncodingException("The returned credential is no longer active.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, Block.toBlock(restrictions), issuance.toBlock(), c.toBlock().setType(C), e.toBlock().setType(E), i.toBlock().setType(I)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the parameters of a new credential.";
    }
    
    
    /**
     * Returns an internal credential with the given parameters.
     * 
     * @param randomizedPermissions the client's randomized permissions.
     * @param role the role that is assumed or null in case no role is assumed.
     * @param b the blinding exponent of the issued credential.
     * @param u the client's secret of the issued credential.
     * 
     * @return an internal credential with the given parameters.
     * 
     * @require hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    @Nonnull ClientCredential getInternalCredential(@Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nonnull BigInteger b, @Nonnull Exponent u) throws SQLException, IOException, PacketException, ExternalException {
        assert hasSignature() : "This handler has a signature.";
        
        if (restrictions == null) throw new InvalidEncodingException("The restrictions may not be null for internal credentials.");
        final @Nonnull Exponent v = new Exponent(restrictions.toBlock().getHash());
        
        final @Nonnull InternalPerson issuer = getSignatureNotNull().getSubjectNotNull().getIdentity().toInternalPerson();
        return new ClientCredential(publicKey, issuer, issuance, randomizedPermissions, role, restrictions, c, e, new Exponent(b), u, i, v);
    }
    
    /**
     * Returns an external credential with the given parameters.
     * 
     * @param randomizedPermissions the client's randomized permissions.
     * @param attributeContent the attribute content for access control.
     * @param b the blinding exponent of the issued credential.
     * @param u the client's secret of the issued credential.
     * @param v the hash of the requester's identifier.
     * 
     * @return an external credential with the given parameters.
     * 
     * @require hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    @Nonnull ClientCredential getExternalCredential(@Nonnull RandomizedAgentPermissions randomizedPermissions, @Nonnull Block attributeContent, @Nonnull BigInteger b, @Nonnull Exponent u, @Nonnull Exponent v) throws SQLException, IOException, PacketException, ExternalException {
        assert hasSignature() : "This handler has a signature.";
        
        if (restrictions != null) throw new InvalidEncodingException("The restrictions must be null for external credentials.");
        
        final @Nonnull InternalNonHostIdentity issuer = getSignatureNotNull().getSubjectNotNull().getIdentity().toInternalNonHostIdentity();
        return new ClientCredential(publicKey, issuer, issuance, randomizedPermissions, attributeContent, c, e, new Exponent(b), u, i, v, false);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof CredentialReply) {
            final @Nonnull CredentialReply other = (CredentialReply) object;
            return this.publicKey.equals(other.publicKey) && Objects.equals(this.restrictions, other.restrictions) && this.issuance.equals(other.issuance) && this.c.equals(other.c) && this.e.equals(other.e) && this.i.equals(other.i);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + publicKey.hashCode();
        hash = 89 * hash + Objects.hashCode(restrictions);
        hash = 89 * hash + issuance.hashCode();
        hash = 89 * hash + c.hashCode();
        hash = 89 * hash + e.hashCode();
        hash = 89 * hash + i.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new CredentialReply(entity, signature, number, block);
        }
        
    }
    
}
