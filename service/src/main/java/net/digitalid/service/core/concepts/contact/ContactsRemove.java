package net.digitalid.service.core.concepts.contact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.annotations.Clients;

/**
 * Removes {@link FreezableContacts contacts} from a {@link Context context}.
 */
@Immutable
final class ContactsRemove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code remove.contacts.context@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("remove.contacts.context@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Context.TYPE, FreezableContacts.TYPE);
    
    
    /**
     * Stores the context of this action.
     */
    final @Nonnull Context context;
    
    /**
     * Stores the contacts which are to be removed.
     */
    private final @Nonnull @Frozen ReadOnlyContacts contacts;
    
    /**
     * Creates an internal action to remove the given contacts from the given context.
     * 
     * @param context the context whose contacts are to be reduced.
     * @param contacts the contacts to be removed from the given context.
     */
    @Clients
    ContactsRemove(@Nonnull Context context, @Nonnull @Frozen ReadOnlyContacts contacts) {
        super(context.getRole());
        
        this.context = context;
        this.contacts = contacts;
    }
    
    /**
     * Creates an internal action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private ContactsRemove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.context = Context.get(entity.castTo(NonHostEntity.class), elements.getNonNullable(0));
        this.contacts = new FreezableContacts(entity.castTo(NonHostEntity.class), elements.getNonNullable(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, context, contacts);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Removes the contacts " + contacts + " from the context with the number " + context + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return new Restrictions(false, false, true, context);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return new Restrictions(false, false, false, context);
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws DatabaseException {
        context.removeContactsForActions(contacts);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ContactsRemove && ((ContactsRemove) action).context.equals(context) || action instanceof ContactsAdd && ((ContactsAdd) action).context.equals(context);
    }
    
    @Pure
    @Override
    public @Nonnull ContactsAdd getReverse() {
        return new ContactsAdd(context, contacts);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof ContactsRemove && this.context.equals(((ContactsRemove) object).context) && this.contacts.equals(((ContactsRemove) object).contacts);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * (89 * protectedHashCode() + context.hashCode()) + contacts.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return ContextModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new ContactsRemove(entity, signature, recipient, block);
        }
        
    }
    
}
