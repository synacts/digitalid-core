package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Group;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class GetCredentialQuery {
    
    public GetCredentialQuery() {
        
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
