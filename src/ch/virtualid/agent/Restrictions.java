package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Contact;
import ch.virtualid.concepts.Context;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the restrictions of an agent.
 * <p>
 * <em>Important:</em> Though this class promises that its objects are immutable, their hash may change!
 * 
 * @invariant getContext() == null || getContact() == null : "The context or the contact is null.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class Restrictions implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code client.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT = SemanticType.create("client.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code role.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ROLE = SemanticType.create("role.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code writing.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType WRITING = SemanticType.create("writing.restrictions.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code context.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTEXT = SemanticType.create("context.restrictions.agent@virtualid.ch").load(Context.TYPE);
    
    /**
     * Stores the semantic type {@code contact.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTACT = SemanticType.create("contact.restrictions.agent@virtualid.ch").load(Contact.TYPE);
    
    /**
     * Stores the semantic type {@code restrictions.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("restrictions.agent@virtualid.ch").load(TupleWrapper.TYPE, CLIENT, ROLE, WRITING, CONTEXT, CONTACT);
    
    
    /**
     * Stores the weakest restrictions (without a context and contact).
     */
    public static final @Nonnull Restrictions NONE = new Restrictions(false, false, false);
    
    
    /**
     * Stores whether the authorization is restricted to clients.
     */
    private final boolean client;
    
    /**
     * Stores whether the authorization is restricted to agents that can assume incoming roles.
     */
    private final boolean role;
    
    /**
     * Stores whether the authorization is restricted to agents that can write (to contexts).
     */
    private final boolean writing;
    
    /**
     * Stores the context to which the authorization is restricted (or null).
     */
    private final @Nullable Context context;
    
    /**
     * Stores the contact to which the authorization is restricted (or null).
     */
    private final @Nullable Person contact;
    
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write (to contexts).
     */
    public Restrictions(boolean client, boolean role, boolean writing) {
        this(client, role, writing, null, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write (to contexts).
     * @param context the context to which the authorization is restricted (or null).
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nullable Context context) {
        this(client, role, writing, context, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write (to contexts).
     * @param contact the contact to which the authorization is restricted (or null).
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nullable Person contact) {
        this(client, role, writing, null, contact);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write (to contexts).
     * @param context the context to which the authorization is restricted (or null).
     * @param contact the contact to which the authorization is restricted (or null).
     * 
     * @require context == null || contact == null : "The context or the contact is null.";
     */
    private Restrictions(boolean client, boolean role, boolean writing, @Nullable Context context, @Nullable Person contact) {
        assert context == null || contact == null : "The context or the contact is null.";
        
        this.client = client;
        this.role = role;
        this.writing = writing;
        this.context = context;
        this.contact = contact;
    }
    
    /**
     * Creates new restrictions from the given block.
     * 
     * @param block the block containing the restrictions.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public Restrictions(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.client = new BooleanWrapper(tuple.getElementNotNull(0)).getValue();
        this.role = new BooleanWrapper(tuple.getElementNotNull(1)).getValue();
        this.writing = new BooleanWrapper(tuple.getElementNotNull(2)).getValue();
        this.context = tuple.isElementNull(3) ? null : new Context(tuple.getElementNotNull(3));
        this.contact = tuple.isElementNull(4) ? null : new Contact(tuple.getElementNotNull(4));
        
        if (context != null && contact != null) throw new InvalidEncodingException("The context and the contact are not null.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected @Nonnull Block toBlock() {
        @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(5);
        elements.set(0, new BooleanWrapper(client).toBlock());
        elements.set(1, new BooleanWrapper(role).toBlock());
        elements.set(2, new BooleanWrapper(writing).toBlock());
        elements.set(3, Block.toBlock(context));
        elements.set(4, Block.toBlock(contact));
        return new TupleWrapper(elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns whether the authorization is restricted to clients.
     * 
     * @return whether the authorization is restricted to clients.
     */
    @Pure
    public boolean isClient() {
        return client;
    }
    
    /**
     * Returns whether the authorization is restricted to agents that can assume incoming roles.
     * 
     * @return whether the authorization is restricted to agents that can assume incoming roles.
     */
    @Pure
    public boolean isRole() {
        return role;
    }
    
    /**
     * Returns whether the authorization is restricted to agents that can write (to contexts).
     * 
     * @return whether the authorization is restricted to agents that can write (to contexts).
     */
    @Pure
    public boolean isWriting() {
        return writing;
    }
    
    /**
     * Returns the context to which the authorization is restricted (or null).
     * 
     * @return the context to which the authorization is restricted (or null).
     */
    @Pure
    public @Nullable Context getContext() {
        return context;
    }
    
    /**
     * Returns the contact to which the authorization is restricted (or null).
     * 
     * @return the contact to which the authorization is restricted (or null).
     */
    @Pure
    public @Nullable Person getContact() {
        return contact;
    }
    
    
    /**
     * Checks that the authorization is restricted to clients and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkClient() throws PacketException {
        if (!isClient()) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Checks that the authorization is restricted to agents that can assume incoming roles and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkRole() throws PacketException {
        if (!isRole()) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Checks that the authorization is restricted to agents that can write (to contexts) and throws a {@link PacketException} if not.
     */
    @Pure
    public void checkWriting() throws PacketException {
        if (!isWriting()) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    /**
     * Returns whether these restrictions cover the given context.
     * 
     * @param connection an open connection to the database.
     * @param other the context that needs to be covered.
     * 
     * @return whether these restrictions cover the given context.
     */
    @Pure
    public boolean cover(@Nonnull Connection connection, @Nonnull Context other) throws SQLException {
        return context != null && context.isSupercontextOf(connection, other);
    }
    
    /**
     * Checks that these restrictions cover the given context and throws a {@link PacketException} if not.
     * 
     * @param connection an open connection to the database.
     * @param other the context that needs to be covered.
     */
    @Pure
    public void checkCoverage(@Nonnull Connection connection, @Nonnull Context other) throws PacketException, SQLException {
        if (!cover(connection, other)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Returns whether these restrictions cover the given contact.
     * 
     * @param connection an open connection to the database.
     * @param other the contact that needs to be covered.
     * 
     * @return whether these restrictions cover the given contact.
     */
    @Pure
    public boolean cover(@Nonnull Connection connection, @Nonnull Person other) throws SQLException {
        return context != null && !context.contains(connection, other) || contact != null && !contact.equals(other);
    }
    
    /**
     * Checks that these restrictions cover the given contact and throws a {@link PacketException} if not.
     * 
     * @param connection an open connection to the database.
     * @param other the contact that needs to be covered.
     */
    @Pure
    public void checkCoverage(@Nonnull Connection connection, @Nonnull Person other) throws PacketException, SQLException {
        if (!cover(connection, other)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Returns whether these restrictions cover the given restrictions.
     * 
     * @param connection an open connection to the database.
     * @param other the restrictions that need to be covered.
     * 
     * @return whether these restrictions cover the given restrictions.
     */
    @Pure
    public boolean cover(@Nonnull Connection connection, @Nonnull Restrictions other) throws SQLException {
        if (other.client && !client) return false;
        if (other.role && !role) return false;
        if (other.writing && !writing) return false;
        @Nullable Context context = other.context;
        if (context != null && !cover(connection, context)) return false;
        @Nullable Person contact = other.contact;
        return contact == null || cover(connection, contact);
    }
    
    /**
     * Checks whether these restrictions cover the given restrictions and throws a {@link PacketException} if not.
     * 
     * @param connection an open connection to the database.
     * @param restrictions the restrictions that need to be covered.
     */
    @Pure
    public void checkCoverage(@Nonnull Connection connection, @Nonnull Restrictions restrictions) throws PacketException, SQLException {
        if (!cover(connection, restrictions)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == null || !(object instanceof Restrictions)) return false;
        @Nonnull Restrictions other = (Restrictions) object;
        return this.client == other.client && this.role == other.role && this.writing == other.writing && Objects.equals(this.context, other.context) && Objects.equals(this.contact, other.contact);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (client ? 1 : 0);
        hash = 19 * hash + (role ? 1 : 0);
        hash = 19 * hash + (writing ? 1 : 0);
        hash = 19 * hash + Objects.hashCode(context);
        hash = 19 * hash + Objects.hashCode(contact);
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "(Client: " + client + ", Role: " + role + ", Writing: " + writing + ", Context: " + context + ", Contact: " + contact + ")";
    }
    
}
