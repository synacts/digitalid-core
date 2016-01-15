package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Validated;

import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.table.Site;

import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class models a database table that contains part of an {@link Entity entity's} state.
 */
@Immutable
public abstract class SiteTable extends SiteTableImplementation<SiteModule> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new site table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     * @param dumpType the dump type of the new host table.
     * @param stateType the state type of the new site table.
     */
    protected SiteTable(@Nonnull SiteModule module, @Nonnull @Validated String name, @Nonnull Declaration declaration, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType) {
        super(module, name, declaration, dumpType, stateType);
        
        module.registerSiteStorage(this);
    }
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    @Pure
    @Override
    protected final boolean isTableFor(@Nonnull Site site) {
        return true;
    }
    
}
