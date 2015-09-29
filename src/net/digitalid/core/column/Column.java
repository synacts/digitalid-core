package net.digitalid.core.column;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.factory.GlobalFactory;

/**
 * Columns are used to store data in the database.
 * 
 * @see GlobalFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public class Column {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Name –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether the given name is valid.
     * 
     * @param name the name to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public static boolean isValidName(@Nonnull String name) {
        return name.length() <= 22 && !name.equalsIgnoreCase("entity") && Database.getConfiguration().isValidIdentifier(name); // TODO: Why to exclude "entity" and limit the length? Remove both!
    }
    
    /**
     * Stores the name of this column.
     */
    private final @Nonnull @Validated String name;
    
    /**
     * Returns the name of this column.
     * 
     * @return the name of this column.
     */
    @Pure
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the SQL type of this column.
     */
    private final @Nonnull SQLType type;
    
    /**
     * Returns the SQL type of this column.
     * 
     * @return the SQL type of this column.
     */
    @Pure
    public final @Nonnull SQLType getType() {
        return type;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Nullable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores whether this column is nullable.
     */
    private final boolean nullable; // TODO: I think this should not be determined by the column but rather by its occurrence (the table). Or both?
    
    /**
     * Returns whether this column is nullable.
     * 
     * @return whether this column is nullable.
     */
    @Pure
    public final boolean isNullable() {
        return nullable;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Reference –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the foreign key reference of this column or null if there is none.
     */
    private final @Nullable Reference reference;
    
    /**
     * Returns the foreign key reference of this column or null if there is none.
     * 
     * @return the foreign key reference of this column or null if there is none.
     */
    @Pure
    public final @Nullable Reference getReference() {
        return reference;
    }
    
    /**
     * Returns the foreign key constraint of this column if there is one.
     * 
     * @param prefix the prefix that is to be prepended to the column name.
     * @param site the site at which the foreign key constraint is declared.
     * 
     * @return the foreign key constraint of this column if there is one.
     * 
     * @ensure return.isEmpty() || return.startsWith(",") : "The returned string is either empty or starts with a comma.";
     */
    @Locked
    @NonCommitting
    public @Nonnull String getForeignKey(@Nonnull @Validated String prefix, @Nonnull Site site) throws SQLException {
        if (reference != null) return ", FOREIGN KEY (" + (reference.isEntityDependent() ? "entity, " : "") + prefix + name + ") " + reference.get(site);
        else return "";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new column with the given parameters.
     * 
     * @param name the name of the new column.
     * @param type the SQL type of the new column.
     * @param nullable whether the new column is nullable.
     * @param reference the foreign key reference of the new column or null if there is none.
     */
    protected Column(@Nonnull @Validated String name, @Nonnull SQLType type, boolean nullable, @Nullable Reference reference) {
        assert isValidName(name) : "The name is valid.";
        
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.reference = reference;
    }
    
    /**
     * Returns a new column with the given parameters.
     * 
     * @param name the name of the new column.
     * @param type the SQL type of the new column.
     * @param nullable whether the new column is nullable.
     * @param reference the foreign key reference of the new column or null if there is none.
     * 
     * @return a new column with the given parameters.
     */
    public static final @Nonnull Column get(@Nonnull @Validated String name, @Nonnull SQLType type, boolean nullable, @Nullable Reference reference) {
        return new Column(name, type, nullable, reference);
    }
    
    /**
     * Returns a new column with the given parameters.
     * 
     * @param name the name of the new column.
     * @param type the SQL type of the new column.
     * 
     * @return a new column with the given parameters.
     */
    public static final @Nonnull Column get(@Nonnull @Validated String name, @Nonnull SQLType type) {
        return get(name, type, true, null);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Declaration –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return name + " " + type + (nullable ? "" : " NOT NULL");
    }
    
}
