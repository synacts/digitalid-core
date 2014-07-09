package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Mapper;
import ch.xdf.Block;
import ch.xdf.Int32Wrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class ContactsAdd {
    
    public ContactsAdd() {
        
    }
    
    /**
     * The handler for requests of type {@code request.add.contact@virtualid.ch}.
     */
    private static class AddContact extends Handler {

        private AddContact() throws Exception { super("request.add.contact@virtualid.ch", "response.add.contact@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Block[] elements = new TupleWrapper(element).getElementsNotNull(2);
            
            String identifier = new StringWrapper(elements[0]).getString();
            if (!Identifier.isValid(identifier)) throw new InvalidEncodingException("The person identifier is valid.");
            long contact = Mapper.getVid(identifier);
            if (!Category.isPerson(contact)) throw new InvalidEncodingException("The identifier has to denote a person.");
            
            int context = new Int32Wrapper(elements[1]).getValue();
            Restrictions restrictions = host.getRestrictions(connection, vid, signature.getClient());
            restrictions.checkCoverage(context);
            restrictions.checkWriting();
            
            host.addContact(connection, vid, contact, context);
            
            return Block.EMPTY;
        }
        
    }
    
}
