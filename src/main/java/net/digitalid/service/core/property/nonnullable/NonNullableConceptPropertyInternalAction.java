package net.digitalid.service.core.property.nonnullable;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreServiceInternalAction;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class NonNullableConceptPropertyInternalAction<E extends Entity<E>, C extends Concept<C, E, ?>> extends CoreServiceInternalAction {
    
	private final NonNullableConceptPropertyTable<?,C,E> conceptPropertyTable;
	
    protected NonNullableConceptPropertyInternalAction(@Nonnull Role role, NonNullableConceptPropertyTable<?,C,E> conceptPropertyTable) {
		super(role);
		this.conceptPropertyTable = conceptPropertyTable;
	}
    
    protected NonNullableConceptPropertyInternalAction(@Nonnull Entity<E> entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, NonNullableConceptPropertyTable<?,C,E> conceptPropertyTable) throws SQLException, IOException, PacketException, ExternalException {
		super(entity, signature, recipient);
		this.conceptPropertyTable = conceptPropertyTable;
	}

	@Override
	protected void executeOnBoth() throws SQLException {
		conceptPropertyTable.replace(property, oldTime, newTime, oldValue, newValue);
	}

	@Override
	public boolean interferesWith(Action action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InternalAction getReverse() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StateModule getModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SemanticType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

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
    private static final class Factory<E extends Entity<E>, C extends Concept<C,E,?>> extends Method.Factory {
        
    	private NonNullableConceptPropertyTable<?,C,E> conceptPropertyTable;
    	
    	// TODO: parameterize
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
        	
            return new NonNullableConceptPropertyInternalAction(entity, signature, recipient, conceptPropertyTable);
        }
        
    }
	
}
