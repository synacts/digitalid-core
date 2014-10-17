package ch.virtualid.concepts;

import ch.virtualid.concept.Concept;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.SemanticType;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Certificate extends Concept {
    
    /**
     * Stores the semantic type {@code certificate@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("certificate@virtualid.ch").load(SignatureWrapper.TYPE, Attribute.TYPE);
    
    /**
     * Stores the semantic type {@code list.certificate@virtualid.ch}.
     */
    public static final @Nonnull SemanticType LIST = SemanticType.create("list.certificate@virtualid.ch").load(ListWrapper.TYPE, TYPE);
    
    
    public Certificate(@Nonnull Entity entity) {
        super(entity);
    }
    
}
