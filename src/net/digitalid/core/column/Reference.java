package net.digitalid.core.column;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.entity.Site;
import net.digitalid.core.factory.GlobalFactory;

/**
 * This class allows to retrieve foreign key references in a unified way.
 * 
 * @see GlobalFactory
 * @see GeneralReference
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public abstract class Reference {
    
    /**
     * Returns whether this reference depends on an entity.
     * 
     * @return whether this reference depends on an entity.
     */
    public abstract boolean isEntityDependent();
    
    /**
     * Returns the string used to reference to an instance of the referenceable class.
     * 
     * @param site the site at which the foreign key constraint is declared and used.
     * 
     * @return the string used to reference to an instance of the referenceable class.
     * 
     * @ensure return.startsWith("REFERENCES") : "The returned string is a reference.";
     */
    @Locked
    @NonCommitting
    public abstract @Nonnull String get(@Nonnull Site site) throws SQLException;
    
}
