package ch.virtualid.handler.action.internal;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class AttributeValueReplace {
    
    public AttributeValueReplace() {
        
    }
    
    /**
     * The handler for requests of type {@code request.set.attribute@virtualid.ch}.
     */
    private static class SetAttribute extends Handler {

        private SetAttribute() throws Exception { super("request.set.attribute@virtualid.ch", "response.set.attribute@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Permissions authorization = host.getAuthorization(connection, vid, signature.getClient());
            
            Block[] elements = new TupleWrapper(element).getElements();
            Block attribute = elements[0];
            String visibility = Category.isPerson(vid) ? new StringWrapper(elements[1]).getString() : null;
            
            String identifier = new SelfcontainedWrapper(new SignatureWrapper(attribute, true).getElement()).getIdentifier();
            if (!Identifier.isValid(identifier)) throw new InvalidEncodingException("The type identifier is valid.");
            long type = Mapper.getVid(identifier);
            if (!Category.isSemanticType(type)) throw new InvalidEncodingException("The identifier has to denote a semantic type.");
            if (!Type.isAttributeFor(type, Mapper.getCategory(vid))) throw new InvalidEncodingException("The attribute type is applicable to the VID's category.");
            authorization.checkWrite(type);
            host.setAttribute(connection, vid, type, attribute, visibility);
            
            return Block.EMPTY;
        }
        
    }
    
}
