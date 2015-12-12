package net.digitalid.service.core.concept.property.nonnullable;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.property.ConceptPropertyInternalAction;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.encoding.InvalidConceptPropertyActionException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.storage.SiteModule;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.system.exceptions.external.ExternalException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

/**
 * This class models the {@link InternalAction internal action} of a {@link NonNullableConceptProperty non-nullable concept property}. 
 */
@Immutable
final class NonNullableConceptPropertyInternalAction<V, C extends Concept<C, E, ?>, E extends Entity> extends ConceptPropertyInternalAction {
    
    /* -------------------------------------------------- Immutable Fields -------------------------------------------------- */
    
    /**
     * The setup which contains the configuration needed for this action.
     */
    private final @Nonnull NonNullableConceptPropertySetup<V, C, E> setup;
    
    /**
     * The property that holds the value that is changed.
     */
    private final @Nonnull NonNullableConceptProperty<V, C, E> property;
    
    /**
     * The time of the last modification
     */
    private final @Nonnull Time oldTime;
    
    /**
     * The current time.
     */
    private final @Nonnull Time newTime;
    
    /**
     * The previous value of the property.
     */
    private final @Nonnull V oldValue;
    
    /**
     * The new value of the property.
     */
    private final @Nonnull V newValue;
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new non-nullable concept property internal action for a given property using the property setup and the given old and new values.
     * 
     * @param setup the property setup, which corresponds to the configuration of the property.
     * @param property the property, which value is replaced.
     * @param oldTime the time of the last modification.
     * @param newTime the current time.
     * @param oldValue the value of the last modification.
     * @param newValue the new value.
     */
    private NonNullableConceptPropertyInternalAction(@Nonnull NonNullableConceptPropertySetup<V, C, E> setup, @Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull V oldValue, @Nonnull V newValue) throws DatabaseException {
        super(property.getConcept().getRole(), setup.getConceptSetup().getService());
        
        this.setup = setup;
        this.property = property;
        this.oldTime = oldTime;
        this.newTime = newTime;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    @Pure
    static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity> NonNullableConceptPropertyInternalAction<V, C, E> get(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull V oldValue, @Nonnull V newValue) throws DatabaseException {
        return new NonNullableConceptPropertyInternalAction<>(property.getConceptPropertySetup(), property, property.getTime(), Time.getCurrent(), oldValue, newValue); // TODO: Let all the arguments be determined by the caller.
    }
    
    private NonNullableConceptPropertyInternalAction(@Nonnull E entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block content, @Nonnull NonNullableConceptPropertySetup<V, C, E> setup) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient, setup.getConceptSetup().getService());
        
        this.setup = setup;
        final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(content).getNonNullableElements(5);
        final @Nonnull C concept = setup.getConceptSetup().getConceptConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(0));
        this.property = (NonNullableConceptProperty<V, C, E>) concept.getProperty(setup.getPropertyTable()); // TODO: Find a better alternative than casting here (e.g. a toNonNullableConceptProperty() with some kind of exception).
        this.oldTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
        this.newTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
        this.oldValue = setup.getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(3));
        this.newValue = setup.getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(4));
        if (newValue.equals(oldValue)) { throw InvalidConceptPropertyActionException.get(this); }
    }
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    @Override
    protected void executeOnBoth() throws DatabaseException {
        property.replace(oldTime, newTime, oldValue, newValue);
    }
    
    @Override
    public boolean interferesWith(Action action) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public @Nonnull NonNullableConceptPropertyInternalAction<V, C, E> getReverse() throws DatabaseException {
        return new NonNullableConceptPropertyInternalAction<>(setup, property, newTime, oldTime, newValue, oldValue);
    }
    
    @Override
    public @Nonnull SiteModule getModule() {
        return setup.getPropertyTable().getModule();
    }
    
    @Override
    public @Nonnull SemanticType getType() {
        return setup.getActionType();
    }
    
    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return setup.getRequiredAuthorization().getRequiredPermissions(property.getConcept());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return setup.getRequiredAuthorization().getRequiredAgentToExecuteMethod(property.getConcept());
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return new FreezableAgentPermissions(setup.getPropertyType(), true).freeze();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean equals(Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof NonNullableConceptPropertyInternalAction)) { return false; }
        final @Nonnull NonNullableConceptPropertyInternalAction<?, ?, ?> other = (NonNullableConceptPropertyInternalAction) object;
        return other.property.equals(this.property) && 
               other.oldValue.equals(this.oldValue) && 
               other.oldTime.equals(this.oldTime) && 
               other.newValue.equals(this.newValue) && 
               other.newTime.equals(this.newTime);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + property.hashCode();
        hash = 89 * hash + Objects.hashCode(oldTime);
        hash = 89 * hash + Objects.hashCode(newTime);
        hash = 89 * hash + Objects.hashCode(oldValue);
        hash = 89 * hash + Objects.hashCode(newValue);
        return hash;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    // TODO: must be re-done and merged with Method.Factory.
    @Immutable
    public static final class XDFConverter<V, C extends Concept<C, E, ?>, E extends Entity> extends AbstractXDFConverter<NonNullableConceptPropertyInternalAction<V, C, E>, ReadOnlyPair<E, NonNullableConceptPropertySetup<V, C, E>>> {
        
        /**
         * Creates a new XDF converter.
         */
        private XDFConverter(@Nonnull SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull NonNullableConceptPropertyInternalAction<V, C, E> internalAction) {
            return TupleWrapper.encode(internalAction.getType(), internalAction.property.getConcept(), ConvertToXDF.nonNullable(OLD_TIME, internalAction.oldTime), ConvertToXDF.nonNullable(NEW_TIME, internalAction.newTime), internalAction.setup.getValueConverters().getXDFConverter().encodeNonNullable(internalAction.oldValue).setType(internalAction.setup.getOldValueType()), internalAction.setup.getValueConverters().getXDFConverter().encodeNonNullable(internalAction.newValue).setType(internalAction.setup.getNewValueType()));
        }
        
        @Pure
        @Override
        public @Nonnull NonNullableConceptPropertyInternalAction<V, C, E> decodeNonNullable(@Nonnull ReadOnlyPair<E, NonNullableConceptPropertySetup<V, C, E>> pair, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
            
            final E entity = pair.getNonNullableElement0();
            final NonNullableConceptPropertySetup<V, C, E> setup = pair.getNonNullableElement1();
            
            final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(5);
            final @Nonnull C concept = setup.getConceptSetup().getConceptConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(0));
            
            final NonNullableConceptProperty<V, C, E> property = (NonNullableConceptProperty<V, C, E>) concept.getProperty(setup.getPropertyTable()); // TODO: Find a better alternative than casting here (e.g. a toNonNullableConceptProperty() with some kind of exception).
            final Time oldTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
            final Time newTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
            final V oldValue = setup.getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(3));
            final V newValue = setup.getValueConverters().getXDFConverter().decodeNonNullable(entity, elements.getNonNullable(4));
            if (newValue.equals(oldValue)) { throw InvalidConceptPropertyActionException.get(property); }
            // TODO: call other constructor. This constructor is reserved for clients that do not have or need a signature. For methods executed on the host, the signature must be stored.
            return new NonNullableConceptPropertyInternalAction<V, C, E>(setup, property, oldTime, newTime, oldValue, newValue);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter<V, C, E> getXDFConverter() {
        return new XDFConverter<>(setup.getActionType());
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory class for this method.
     */
    @Immutable
    static final class Factory<V, C extends Concept<C, E, ?>, E extends Entity> extends Method.Factory<E> {
        
        private final @Nonnull NonNullableConceptPropertySetup<V, C, E> setup;
        
        Factory(@Nonnull NonNullableConceptPropertySetup<V, C, E> setup) {
            this.setup = setup;
            
            Method.add(setup.getActionType(), this);
        }
        
        @Pure
        @Override
        @NonCommitting
        @SuppressWarnings("unchecked")
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
               return new NonNullableConceptPropertyInternalAction<>((E) entity.castTo(NonHostEntity.class), signature, recipient, block, setup);
        }
        
    }
    
}
