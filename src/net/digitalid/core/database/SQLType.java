package net.digitalid.core.database;

import java.sql.Types;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;

/**
 * This class enumerates the various SQL types.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public enum SQLType {
    
    /**
     * The SQL type for big integers.
     */
    BIGINT(Types.BIGINT, "BIGINT"),
    
    /**
     * The SQL type for tiny integers.
     */
    TINYINT(Types.TINYINT, Database.getConfiguration().TINYINT()),
    
    /**
     * The SQL type for floats.
     */
    FLOAT(Types.FLOAT, Database.getConfiguration().FLOAT()),
    
    /**
     * The SQL type for doubles.
     */
    DOUBLE(Types.DOUBLE, Database.getConfiguration().DOUBLE()),
    
    /**
     * The SQL type for strings.
     */
    STRING(Types.VARCHAR, "VARCHAR(50)"),
    
    /**
     * The SQL type for chars.
     */
    CHAR(Types.VARCHAR, "CHAR(1)"),
    
    /**
     * The SQL type for large objects.
     */
    BLOB(Types.BLOB, Database.getConfiguration().BLOB());
    
    
    /**
     * Stores the JDBC code of this SQL type.
     */
    private final int code;
    
    /**
     * Returns the JDBC code of this SQL type.
     * 
     * @return the JDBC code of this SQL type.
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Stores the string of this SQL type.
     */
    private final @Nonnull String string;
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return string;
    }
    
    /**
     * Creates a new SQL type with the given code and string.
     * 
     * @param code the JDBC code of this SQL type.
     * @param string the string of this SQL type.
     */
    private SQLType(int code, @Nonnull String string) {
        this.code = code;
        this.string = string;
    }
    
}
