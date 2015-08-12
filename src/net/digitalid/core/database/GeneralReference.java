package net.digitalid.core.database;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.entity.Site;

/**
 * This class allows to retrieve foreign key references that are {@link Site site}-independent.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public abstract class GeneralReference extends Reference {
    
    /**
     * Returns the string used to reference to an instance of the referenceable class.
     * 
     * @return the string used to reference to an instance of the referenceable class.
     * 
     * @ensure return.startsWith("REFERENCES") : "The returned string is a reference.";
     */
    @Locked
    @NonCommitting
    public abstract @Nonnull String get() throws SQLException;
    
    @Locked
    @Override
    @NonCommitting
    public final @Nonnull String get(@Nonnull Site site) throws SQLException {
        return get();
    }
    
}
