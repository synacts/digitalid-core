package ch.virtualid.packet;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.ReplayDetectedException;
import ch.virtualid.io.Level;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
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
        assert Database.isMainThread(): "This method block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS replay (hash " + Database.getConfiguration().HASH() + " NOT NULL PRIMARY KEY, time " + Time.FORMAT + " NOT NULL)");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database table of the replay checker could not be created.", exception);
        }
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
                    statement.executeUpdate("DELETE FROM replay WHERE time < " + Time.HALF_HOUR.add(Time.MINUTE).ago());
                    Database.getConnection().commit();
                } catch (@Nonnull SQLException exception) {
                    Database.LOGGER.log(Level.WARNING, exception);
                }
            }
        }, Time.MINUTE.getValue(), Time.QUARTER_HOUR.getValue());
    }
    
    /**
     * Checks that no other packet with the same hash was received during the last half hour.
     * 
     * @param packet the packet to check for a replay attack.
     */
    public static void check(@Nonnull Packet packet) throws SQLException, ReplayDetectedException, InvalidEncodingException {
        final @Nonnull String SQL = "INSERT INTO replay (hash, time) VALUES (?, ?)";
        final @Nullable Savepoint savepoint = Database.getConfiguration().setSavepoint();
        try (@Nonnull PreparedStatement preparedStatement = Database.getConnection().prepareStatement(SQL)) {
            preparedStatement.setBytes(1, packet.getEncryption().getElementNotNull().getHash().toByteArray());
            packet.getEncryption().getTime().set(preparedStatement, 2);
            preparedStatement.executeUpdate();
        } catch (@Nonnull SQLException exception) {
            Database.getConfiguration().rollback(savepoint);
            throw new ReplayDetectedException(packet);
        }
    }
    
}
