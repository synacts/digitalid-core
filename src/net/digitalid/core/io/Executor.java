package net.digitalid.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Stateless;

/**
 * This class makes it easier to execute commands.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Stateless
public final class Executor {
    
    /**
     * Executes the given command.
     * 
     * @param command the command to be executed.
     * 
     * @return the first line of the command's output.
     */
    public static @Nullable String execute(@Nonnull String command) throws IOException {
        final @Nonnull Process process = Runtime.getRuntime().exec(command);
        final @Nonnull BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }
    
    /**
     * Returns the full name of the user.
     * 
     * @return the full name of the user.
     */
    public static @Nonnull String getUserName() {
        final @Nonnull String OSname = System.getProperty("os.name").toLowerCase();
        if (OSname.contains("mac")) {
            try {
                final @Nullable String userName = execute("id -F");
                if (userName != null) return userName;
            } catch (@Nonnull IOException exception) {}
        } else if (OSname.contains("win")) {
            // TODO: http://stackoverflow.com/questions/7809648/get-display-name-of-current-windows-domain-user-from-a-command-prompt
        } else if (OSname.contains("linux") || OSname.contains("unix")) {
            // TODO: getent passwd $USER | cut -d ":" -f 5
        }
        return System.getProperty("user.name");
    }
    
}
