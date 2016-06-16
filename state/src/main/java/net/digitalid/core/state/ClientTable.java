package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.client.Client;

/**
 * This class models a database table that can be created and deleted on {@link Client clients}.
 * 
 * @see HostTable
 * @see SiteTable
 */
@Immutable
public abstract class ClientTable extends ClientTableImplementation<ClientModule> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new client table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     */
    protected ClientTable(@Nonnull ClientModule module, @Nonnull @Validated String name, @Nonnull Declaration declaration) {
        super(module, name, declaration);
        
        module.registerClientStorage(this);
    }
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    @Pure
    @Override
    protected final boolean isTableFor(@Nonnull Site site) {
        return site instanceof Client;
    }
    
}
