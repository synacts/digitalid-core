package ch.virtualid.packet;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.InitializationVector;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.xdf.EncryptionWrapper;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checks that no other encryption with the same initialization vector was received during the last half hour.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Replay {
    
    /**
     * Initializes the replay checker by creating the corresponding database tables if necessary.
     */
    static {
        assert Database.isMainThread() : "This method block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_replay (vector " + InitializationVector.FORMAT + " NOT NULL PRIMARY KEY, time " + Time.FORMAT + " NOT NULL, INDEX(time))");
        } catch (@Nonnull SQLException exception) {
            try { Database.rollback(); } catch (@Nonnull SQLException exc) { throw new InitializationError("Could not rollback.", exc); }
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
