package ch.virtualid.credential;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
final class CredentialReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.internal.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("reply.internal.credential@virtualid.ch").load(TupleWrapper.TYPE, RandomizedAgentPermissions.TYPE, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the restrictions for which a credential is requested.
     */
    private final @Nonnull Restrictions restrictions;
    
    private final @Nonnull Element c;
    
    private final @Nonnull Exponent e;
    
    private final @Nonnull Exponent i;
    
    CredentialReply(@Nonnull NonHostAccount account, @Nonnull Restrictions restrictions, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent i) {
        super(account);
        
        this.restrictions = restrictions;
        this.c = c;
        this.e = e;
        this.i = i;
    }
    
}
