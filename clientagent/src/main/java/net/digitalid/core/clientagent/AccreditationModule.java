package net.digitalid.core.clientagent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;

/**
 * This class provides database access to the accreditation requests of the core service.
 * 
 * @see ClientAgent
 */
@Stateless
public final class AccreditationModule /* implements ClientModule */ {
    
    /**
     * Stores an instance of this module.
     */
    public static final AccreditationModule MODULE = new AccreditationModule();
    
    @Pure
//    @Override
    public @Nonnull Service getService() {
        return CoreService.INSTANCE;
    }
    
//    @Override
//    @NonCommitting
//    public void createTables(@Nonnull Site site) throws DatabaseException {
//            // TODO: Create the tables of this module.
//    }
//    
//    @Override
//    @NonCommitting
//    public void deleteTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            // TODO: Delete the tables of this module.
//        }
//    }
//    
//    static { CoreService.SERVICE.add(MODULE); }
    
}
