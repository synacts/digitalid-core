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
package net.digitalid.core.packet.replay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.threading.Threading;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.encryption.RequestEncryption;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.symmetrickey.InitializationVector;

/**
 * Checks that no other encryption with the same initialization vector was received during the last half hour.
 */
@Utility
@TODO(task = "Implement the replay detection with the new database layer.", date = "2016-11-06", author = Author.KASPAR_ETTER)
public abstract class Replay {
    
    /**
     * Initializes the replay checker by creating the corresponding database tables if necessary.
     */
    static {
        Require.that(Threading.isMainThread()).orThrow("This method block is called in the main thread.");
        
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_replay (vector " + InitializationVector.FORMAT + " NOT NULL PRIMARY KEY, time " + Time.FORMAT + " NOT NULL" + Database.getConfiguration().INDEX("time") + ")");
//            Database.getConfiguration().createIndex(statement, "general_replay", "time");
//        } catch (@Nonnull SQLException exception) {
//            throw InitializationError.get("The database table of the replay checker could not be created.", exception);
//        }
//        
//        Database.addRegularPurging("general_replay", Time.HALF_HOUR.add(Time.MINUTE));
    }
    
    /**
     * Checks that no other encryption with the same initialization vector was received during the last half hour.
     * 
     * @param encryption the encryption to check for a replay attack.
     */
    @Pure
    @NonCommitting
    public static void check(@Nonnull RequestEncryption<?> encryption) throws DatabaseException, RequestException {
        final @Nonnull Time time = encryption.getTime();
        final @Nullable InitializationVector initializationVector = encryption.getInitializationVector();
        
        if (time.isLessThan(Time.HALF_HOUR.ago())) { throw RequestExceptionBuilder.withCode(RequestErrorCode.ENCRYPTION).withMessage("The encryption is older than half an hour.").build(); }
        if (time.isGreaterThan(Time.MINUTE.ahead())) { throw RequestExceptionBuilder.withCode(RequestErrorCode.ENCRYPTION).withMessage("The encryption is more than a minute ahead.").build(); }
        
//        if (initializationVector != null) {
//            final @Nonnull String SQL = "INSERT INTO general_replay (vector, time) VALUES (?, ?)";
//            try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//                initializationVector.set(preparedStatement, 1);
//                time.set(preparedStatement, 2);
//                preparedStatement.executeUpdate();
//            } catch (@Nonnull SQLException exception) {
//                throw RequestException.get(RequestErrorCode.REPLAY, "The encryption has been replayed.", exception);
//            }
//        }
    }
    
}
