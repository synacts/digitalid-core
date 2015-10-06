package net.digitalid.service.core.packet;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cryptography.InitializationVector;
import net.digitalid.service.core.exceptions.packet.PacketError;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.wrappers.EncryptionWrapper;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.errors.InitializationError;

/**
 * Checks that no other encryption with the same initialization vector was received during the last half hour.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class Replay {
    
    /**
     * Initializes the replay checker by creating the corresponding database tables if necessary.
     */
    static {
        assert Database.isMainThread() : "This method block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_replay (vector " + InitializationVector.FORMAT + " NOT NULL PRIMARY KEY, time " + Time.FORMAT + " NOT NULL" + Database.getConfiguration().INDEX("time") + ")");
            Database.getConfiguration().createIndex(statement, "general_replay", "time");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database table of the replay checker could not be created.", exception);
        }
        
        Database.addRegularPurging("general_replay", Time.HALF_HOUR.add(Time.MINUTE));
    }
    
    /**
     * Checks that no other encryption with the same initialization vector was received during the last half hour.
     * 
     * @param encryption the encryption to check for a replay attack.
     */
    @NonCommitting
    public static void check(@Nonnull EncryptionWrapper encryption) throws SQLException, PacketException {
        final @Nonnull Time time = encryption.getTime();
        final @Nullable InitializationVector initializationVector = encryption.getInitializationVector();
        
        if (time.isLessThan(Time.HALF_HOUR.ago())) throw new PacketException(PacketError.ENCRYPTION, "The encryption is older than half an hour.", null);
        if (time.isGreaterThan(Time.MINUTE.ahead())) throw new PacketException(PacketError.ENCRYPTION, "The encryption is more than a minute ahead.", null);
        
        if (initializationVector != null) {
            final @Nonnull String SQL = "INSERT INTO general_replay (vector, time) VALUES (?, ?)";
            try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
                initializationVector.set(preparedStatement, 1);
                time.set(preparedStatement, 2);
                preparedStatement.executeUpdate();
            } catch (@Nonnull SQLException exception) {
                throw new PacketException(PacketError.REPLAY, "The encryption has been replayed.", exception);
            }
        }
    }
    
}
