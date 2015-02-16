package ch.virtualid.contact;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Frozen;
import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.OnlyForClients;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.collections.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Adds {@link Contacts contacts} to a {@link Context context}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class ContactsAdd extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code add.contacts.context@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("add.contacts.context@virtualid.ch").load(TupleWrapper.TYPE, Context.TYPE, Contacts.TYPE);
    
    
    /**
     * Stores the context of this action.
     */
    final @Nonnull Context context;
    
    /**
     * Stores the contacts which are to be added.
     */
    private final @Nonnull @Frozen ReadonlyContacts contacts;
    
    /**
     * Creates an internal action to add the given contacts to the given context.
     * 
     * @param context the context whose contacts are to be extended.
     * @param contacts the contacts to be added to the given context.
     */
    @OnlyForClients
    ContactsAdd(@Nonnull Context context, @Nonnull @Frozen ReadonlyContacts contacts) {
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
    private ContactsAdd(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.context = Context.get(entity.toNonHostEntity(), elements.getNotNull(0));
        this.contacts = new Contacts(entity.toNonHostEntity(), elements.getNotNull(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, context, contacts).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Adds the contacts " + contacts + " to the context with the number " + context + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return new Restrictions(false, false, true, context);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return new Restrictions(false, false, false, context);
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        context.addContactsForActions(contacts);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ContactsAdd && ((ContactsAdd) action).context.equals(context) || action instanceof ContactsRemove && ((ContactsRemove) action).context.equals(context);
    }
    
    @Pure
    @Override
    public @Nonnull ContactsRemove getReverse() {
        return new ContactsRemove(context, contacts);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof ContactsAdd && this.context.equals(((ContactsAdd) object).context) && this.contacts.equals(((ContactsAdd) object).contacts);
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
    public @Nonnull BothModule getModule() {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new ContactsAdd(entity, signature, recipient, block);
        }
        
    }
    
}
