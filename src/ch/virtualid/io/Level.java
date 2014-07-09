package ch.virtualid.io;

import javax.annotation.Nonnull;

/**
 * This class enumerates the various level of log messages.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public enum Level {
    INFORMATION(0),
    WARNING(1),
    ERROR(2),
    OFF(3);
    
    /**
     * Stores the byte representation of the level.
     */
    private final byte value;
    
    /**
     * Creates a new level with the given value.
     * 
     * @param value the value encoding the level.
     */
    Level(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the byte representation of this level.
     * 
     * @return the byte representation of this level.
     */
    public byte getValue() {
        return value;
    }
    
    /**
     * Returns a string representation of this level.
     * 
     * @return a string representation of this level.
     */
    @Override
    public @Nonnull String toString() {
        final @Nonnull String string = this.name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
}
