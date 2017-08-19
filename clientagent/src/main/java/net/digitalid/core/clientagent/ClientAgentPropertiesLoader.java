package net.digitalid.core.clientagent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.identification.identity.IdentifierResolver;

/**
 * The following code cannot be in the client agent class because instantiating the initializer would trigger the client agent class to get loaded which in turn would trigger an IdentifierResolver not initialized error because the CoreService maps a SemanticType.
 */
@Utility
@TODO(task = "Find a better solution for this problem!", date = "2017-07-19", author = Author.KASPAR_ETTER)
public abstract class ClientAgentPropertiesLoader {
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE).addDependency(IdentifierResolver.configuration);
    
    /**
     * Loads the attribute subclass.
     */
    @PureWithSideEffects
    @Initialize(target = ClientAgentPropertiesLoader.class, dependencies = IdentifierResolver.class)
    public static void initializeSubclass() {
        ClientAgentSubclass.MODULE.getName();
    }
    
}
