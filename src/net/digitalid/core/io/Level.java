package net.digitalid.core.io;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * This class enumerates the various level of log messages.
 * 
 * @see Logger
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public enum Level {
    
    /**
     * The level for information.
     */
    INFORMATION(0),
    
    /**
     * The level for warnings.
     */
    WARNING(1),
    
    /**
     * The level for errors.
     */
    ERROR(2),
    
    /**
     * The level for off.
     */
    OFF(3);
    
    /**
     * Stores the byte representation of this level.
     */
    private final byte value;
    
    /**
     * Creates a new level with the given value.
     * 
     * @param value the value encoding the level.
     */
    private Level(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the byte representation of this level.
     * 
     * @return the byte representation of this level.
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull String string = name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
}
