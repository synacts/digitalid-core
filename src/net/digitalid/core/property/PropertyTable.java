package net.digitalid.core.property;

/**
 * This class models a database table.
 * 
 * Table class with reference to its module, a name and the type of the stored value, an instance of which is created statically and creates the necessary semantic types for the value change.
 * The table has to provide factory methods to create the value both from SQL and from a block (needed for decoding the value change internal action).
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public abstract class PropertyTable {
    
    public PropertyTable() {
        
    }
    
}
