package ch.virtualid.credential;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.entity.Role;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.service.CoreServiceExternalQuery;
import javax.annotation.Nonnull;

/**
 * Requests a new attribute-based credential with the given permissions and attribute content.
 * 
 * @see CredentialReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
final class CredentialExternalQuery extends CoreServiceExternalQuery {
    
    CredentialExternalQuery(@Nonnull Role role, @Nonnull InternalNonHostIdentity subject, @Nonnull RandomizedAgentPermissions permissions) {
        super(role);
        
        
    }
    
}
