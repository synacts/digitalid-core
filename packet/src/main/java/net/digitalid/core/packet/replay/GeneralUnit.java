package net.digitalid.core.packet.replay;

import javax.annotation.Nonnull;

import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.type.Utility;

/**
 * This class provides a general database unit.
 */
@Utility
public abstract class GeneralUnit {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    /**
     * Stores the database unit for non-unit-specific tables.
     */
    public static final @Nonnull Unit INSTANCE = () -> "general";
    
}
