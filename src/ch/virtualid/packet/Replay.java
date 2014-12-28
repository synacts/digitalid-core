package ch.virtualid.packet;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.InitializationVector;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.ReplayDetectedException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.io.Level;
import ch.xdf.EncryptionWrapper;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checks that no other packet with the same encryption time and hash was received during the last half hour.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
            throw new InitializationError("The database table of the replay checker could not be created.", exception);
        }
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Statement statement = Database.createStatement()) {
                    statement.executeUpdate("DELETE FROM general_replay WHERE time < " + Time.HALF_HOUR.add(Time.MINUTE).ago());
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Database.LOGGER.log(Level.WARNING, exception);
                }
            }
        }, Time.MINUTE.getValue(), Time.QUARTER_HOUR.getValue());
    }
    
    /**
     * Checks that no other encryption with the same initialization vector was received during the last half hour.
     * 
     * @param encryption the encryption to check for a replay attack.
     */
    public static void check(@Nonnull EncryptionWrapper encryption) throws SQLException, ReplayDetectedException, PacketException {
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
                throw new ReplayDetectedException(encryption);
            }
        }
    }
    
}
