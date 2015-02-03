package ch.virtualid.io;

import ch.virtualid.annotations.Committing;
import ch.virtualid.exceptions.io.EscapeOptionException;
import javax.annotation.Nonnull;

/**
 * Every option in the {@link Console console} has to extend this class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Option {
    
    /**
     * Stores the description of the option.
     */
    private final @Nonnull String description;
    
    /**
     * Creates a new option with the given description.
     * 
     * @param description the description of the option.
     */
    protected Option(@Nonnull String description) {
        this.description = description;
    }
    
    /**
     * Returns the description of this option.
     * 
     * @return the description of this option.
     */
    public final @Nonnull String getDescription() {
        return description;
    }
    
    /**
     * Executes this option.
     */
    @Committing
    public abstract void execute() throws EscapeOptionException;
    
}
