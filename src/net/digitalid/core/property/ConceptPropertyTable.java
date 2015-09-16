package net.digitalid.core.property;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.data.StateTable;
import net.digitalid.core.storable.SimpleFactory;

/**
 * This class models a database table.
 * 
 * Table class with reference to its module, a name and the type of the stored value, an instance of which is created statically and creates the necessary semantic types for the value change.
 * The table has to provide factory methods to create the value both from SQL and from a block (needed for decoding the value change internal action).
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Immutable
public abstract class ConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends StateTable<ConceptPropertyTable<V, C, E>> {
    
    private final @Nonnull SimpleFactory<V, E> valueFactory;
    
    private final @Nonnull SimpleFactory<C, E> conceptFactory;
    
    protected ConceptPropertyTable() {
        
    }
    
}
