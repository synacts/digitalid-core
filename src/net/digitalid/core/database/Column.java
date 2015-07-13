package net.digitalid.core.database;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
@Immutable
public final class Column {
    
    private final @Nonnull String name;
    
    /**
     * Stores the SQL type of this column.
     */
    private final @Nonnull String type; // TODO: Maybe as an enum of supported types?
    
    private final boolean nullable;
    
    // 
    
    public Column(@Nonnull String name, @Nonnull String type, boolean nullable) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return name + " " + type + (nullable ? "" : " NOT NULL");
    }
    
}
