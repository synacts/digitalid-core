package net.digitalid.service.core.property.nonnullable;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.encoding.Encode;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.ConceptPropertyInternalAction;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
final class NonNullableConceptPropertyInternalAction<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends ConceptPropertyInternalAction {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private final @Nonnull NonNullableConceptPropertyTable<V, C, E> table;
    
    private final @Nonnull NonNullableConceptProperty<V, C, E> property;
    
    private final @Nonnull Time oldTime;
    
    private final @Nonnull Time newTime;
    
    private final @Nonnull V oldValue;
    
    private final @Nonnull V newValue;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private NonNullableConceptPropertyInternalAction(@Nonnull NonNullableConceptPropertyTable<V, C, E> table, @Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull V oldValue, @Nonnull V newValue) throws SQLException {
        super(property.getConcept().getRole(), property.getTable().getService());
        
        this.table = table;
        this.property = property;
        this.oldTime = oldTime;
        this.newTime = newTime;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    @Pure
    static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity<E>> NonNullableConceptPropertyInternalAction<V, C, E> get(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull V oldValue, @Nonnull V newValue) throws SQLException {
        return new NonNullableConceptPropertyInternalAction<>(property.getTable(), property, property.getTime(), Time.getCurrent(), oldValue, newValue);
    }
    
    private NonNullableConceptPropertyInternalAction(@Nonnull E entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block content, @Nonnull NonNullableConceptPropertyTable<V, C, E> table) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient, table.getService());
        
        this.table = table;
        final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(content).getNonNullableElements(5);
        final @Nonnull C concept = table.getConceptFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(0));
        this.property = (NonNullableConceptProperty<V, C, E>) concept.getProperty(table); // TODO: Find a better alternative than casting here (e.g. a toNonNullableConceptProperty() with some kind of exception).
        this.oldTime = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
        this.newTime = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
        this.oldValue = table.getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(3));
        this.newValue = table.getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(4));
        if (newValue.equals(oldValue)) throw new InvalidEncodingException("The old and new value may not be equal.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(table.getActionType(), property.getConcept(), Encode.nonNullable(oldTime, OLD_TIME), Encode.nonNullable(newTime, NEW_TIME), table.getValueFactories().getEncodingFactory().encodeNonNullable(oldValue).setType(property.getTable().getOldValueType()), table.getValueFactories().getEncodingFactory().encodeNonNullable(newValue).setType(property.getTable().getNewValueType()));
    }
    
    @Override
    protected void executeOnBoth() throws SQLException {
        property.replace(oldTime, newTime, oldValue, newValue);
    }
    
    @Override
    public boolean interferesWith(Action action) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public @Nonnull NonNullableConceptPropertyInternalAction<V, C, E> getReverse() throws SQLException {
        return new NonNullableConceptPropertyInternalAction<>(table, property, newTime, oldTime, newValue, oldValue);
    }
    
    @Override
    public @Nonnull StateModule getModule() {
        return table.getModule();
    }
    
    @Override
    public @Nonnull SemanticType getType() {
        return table.getActionType();
    }
    
    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }
    
    // TODO: Get the required permissions, restrictions, etc. from the concept?
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public boolean equals(Object object) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public AbstractEncodingFactory getEncodingFactory() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
    
    The Method.add(...) registration with a new action factory needs to be called for each property table.
    
    This method factory probably has to pass a locally stored (as a field) concept property table to the property action constructor.
    
    */
    
    /**
     * The factory class for the surrounding method.
     */
    @Immutable
    private static final class Factory<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends Method.Factory {
        
    	private @Nonnull NonNullableConceptPropertyTable<V, C, E> conceptPropertyTable;
    	
    	// TODO: parameterize
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
        	
            return new NonNullableConceptPropertyInternalAction(entity, signature, recipient, conceptPropertyTable);
        }
        
    }
    
}
