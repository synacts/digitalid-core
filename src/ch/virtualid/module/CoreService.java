package ch.virtualid.module;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.both.Agents;
import ch.virtualid.module.both.Attributes;
import ch.virtualid.module.both.Certificates;
import ch.virtualid.module.both.Contacts;
import ch.virtualid.module.both.Contexts;
import ch.virtualid.module.both.Passwords;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;

/**
 * This class represents the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CoreService extends Service {
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("@virtualid.ch").load(Identity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType FORMAT = SemanticType.create("module@virtualid.ch").load(TupleWrapper.TYPE, Passwords.TYPE, Attributes.TYPE, Contexts.TYPE, Contacts.TYPE, Agents.TYPE, Certificates.TYPE);
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull Service SERVICE = new CoreService();
    
    
    /**
     * Initializes the core service by adding the modules.
     */
    private CoreService() {
        super("Core Service", "1.0");
        
        addToTuple(new Passwords());
        addToTuple(new Attributes());
        addToTuple(new Contexts());
        addToTuple(new Contacts());
        addToTuple(new Agents());
        addToTuple(new Certificates());
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return FORMAT;
    }
    
}
