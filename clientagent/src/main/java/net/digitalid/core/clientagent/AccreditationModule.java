/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
