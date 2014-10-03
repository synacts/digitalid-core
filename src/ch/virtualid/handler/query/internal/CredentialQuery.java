package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.client.Client;
import ch.virtualid.credential.ClientCredential;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Group;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.packet.Packet;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;

/**
 * Description.
 * 
 * TODO: Isn't this rather an internal or even external action?
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class CredentialQuery extends CoreServiceInternalQuery {
    
    public CredentialQuery() {
        
    }
    
    /**
     * Obtains a credential for the given client on behalf of the given requester at the given issuer for the given authorization.
     * 
     * @param client the client whose secret is used to sign the request.
     * @param requester the VID of a person on behalf of which the credential is to be obtained.
     * @param issuer the VID of a non-host at which the credential is to be obtained.
     * @param randomizedAuthorization the desired authorization in randomized form.
     * @return a credential for the given client on behalf of the given requester at the given issuer.
     * @require client != null : "The client is not null.";
     * @require Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
     * @require Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
     * @require randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
     */
    public static ClientCredential obtainCredential(Client client, long requester, long issuer, RandomizedAuthorization randomizedAuthorization) throws Exception {
        assert client != null : "The client is not null.";
        assert Mapper.isVid(requester) && Category.isPerson(requester) : "The requester has to denote a person.";
        assert Mapper.isVid(issuer) && (Category.isSemanticType(issuer) || issuer == requester) : "The issuer is either a semantic type or the requester itself (roles are not yet supported).";
        assert randomizedAuthorization != null && randomizedAuthorization.getAuthorization() != null && !randomizedAuthorization.getAuthorization().isEmpty() : "The randomized authorization is not empty.";
        
        PublicKey publicKey = new PublicKey(Request.getAttributeNotNullUnwrapped(Mapper.getHost(issuer), Vid.HOST_PUBLIC_KEY));
        Group group = publicKey.getCompositeGroup();
        
        if (issuer == requester) {
            SelfcontainedWrapper content = new SelfcontainedWrapper("request.credential.client@virtualid.ch", randomizedAuthorization.toBlock());
            Packet request = new Packet(content, Mapper.getIdentifier(Mapper.getHost(issuer)), new SymmetricKey(), Mapper.getIdentifier(issuer), 0, client.getSecret());
            Packet response = request.send();
            Block[] elements = new TupleWrapper(respogetContentstent().getElement()).getElementsNotNull(3);
            
            String identifier = respogetSignaturesture().getIdentifier();
            long time = reqgetSignaturesature().getSignatureTimeRoundedDown();
            Restrictions restrictions = Request.getRestrictions(client, issuer);
            Element c = group.getElement(elements[0]);
            Exponent e = group.getExponent(elements[1]);
            Exponent b = group.getExponent(BigInteger.ZERO);
            Exponent u = group.getExponent(client.getSecret());
            Exponent i = group.getExponent(elements[2]);
            Exponent v = group.getExponent(restrictions.toBlock().getHash());
            
            return new Credential(publicKey, identifier, time, randomizedAuthorization, null, restrictions, c, e, b, u, i, v);
        } else {
            throw new UnsupportedOperationException("Credentials for attribute-based access control are not yet supported!");
        }
    }
    
    /**
     * The handler for requests of type {@code request.credential.client@virtualid.ch}.
     */
    private static class ObtainCredential extends Handler {

        private ObtainCredential() throws Exception { super("request.credential.client@virtualid.ch", "response.credential.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            if (!Category.isPerson(vid)) throw new PacketException(PacketException.IDENTIFIER);
            
            BigInteger commitment = signature.getClient();
            Restrictions restrictions = host.getRestrictions(connection, vid, commitment);
            AgentPermissions authorization = host.getAuthorization(connection, vid, commitment);
            if (restrictions == null) throw new PacketException(PacketException.AUTHORIZATION);
            RandomizedAuthorization randomizedAuthorization = new RandomizedAuthorization(element);
            authorization.checkCover(randomizedAuthorization.getAuthorization());
            
            PublicKey publicKey = host.getPublicKey();
            PrivateKey privateKey = host.getPrivateKey();
            Group group = privateKey.getCompositeGroup();
            
            Element f = group.getElement(commitment);
            Exponent i = group.getExponent(new BigInteger(Parameters.HASH, new SecureRandom()));
            Exponent v = group.getExponent(restrictions.toBlock().getHash());
            Exponent o = group.getExponent(Credential.getExposed(Mapper.getIdentifier(vid), signature.getSignatureTimeRoundedDown(), randomizedAuthorization, null).getHash());
            Exponent e = group.getExponent(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            
            Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(o).inverse()).pow(e.inverse()).inverse();
            
            return new TupleWrapper(new Block[]{c.toBlock(), e.toBlock(), i.toBlock()}).toBlock();
        }
        
    }
    
}
