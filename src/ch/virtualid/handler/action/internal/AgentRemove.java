package ch.virtualid.handler.action.internal;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
//public class AgentRemove {
//    
//    public AgentRemove() {
//        
//    }
//    
//    /**
//     * The handler for requests of type {@code request.remove.client@virtualid.ch}.
//     */
//    private static class RemoveClient extends Handler {
//
//        private RemoveClient() throws Exception { super("request.remove.client@virtualid.ch", "response.remove.client@virtualid.ch", true); }
//        
//        @Override
//        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
//            BigInteger commitmentOfRemover = signature.getClient();
//            BigInteger commitmentOfRemovee = new IntegerWrapper(element).getValue();
//            
//            Restrictions restrictionsOfRemover = host.getRestrictions(connection, vid, commitmentOfRemover);
//            Restrictions restrictionsOfRemovee = host.getRestrictions(connection, vid, commitmentOfRemovee);
//            if (restrictionsOfRemover == null) throw new PacketException(PacketException.AUTHORIZATION);
//            restrictionsOfRemover.checkCover(restrictionsOfRemovee);
//            
//            AgentPermissions authorizationOfRemover = host.getAuthorization(connection, vid, commitmentOfRemover);
//            AgentPermissions authorizationOfRemovee = host.getAuthorization(connection, vid, commitmentOfRemovee);
//            authorizationOfRemover.checkCover(authorizationOfRemovee);
//            
//            host.removeClient(connection, vid, commitmentOfRemovee);
//            
//            return Block.EMPTY;
//        }
//        
//    }
//    
//}
