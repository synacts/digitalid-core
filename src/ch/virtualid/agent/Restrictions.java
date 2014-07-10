package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Context;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the restrictions of an authorization.
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
     * Stores the semantic type {@code time@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("time@virtualid.ch").load(Int64Wrapper.TYPE);
    
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
     * @param context the context to which the authorization is restricted.
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nonnull Context context) {
        this(client, role, writing, context, null);
    }
    
    /**
     * Creates new restrictions with the given arguments.
     * 
     * @param client whether the authorization is restricted to clients.
     * @param role whether the authorization is restricted to agents that can assume incoming roles.
     * @param writing whether the authorization is restricted to agents that can write (to contexts).
     * @param contact the contact to which the authorization is restricted.
     */
    public Restrictions(boolean client, boolean role, boolean writing, @Nonnull Person contact) {
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
     * @require context == null || contact == null : "The context or the contact have to be null.";
     */
    private Restrictions(boolean client, boolean role, boolean writing, @Nullable Context context, @Nullable Person contact) {
        assert context == null || contact == null : "The context or the contact have to be null.";
        
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
     */
    public Restrictions(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        super(block);
        
        @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(5);
        client = new BooleanWrapper(elements.getNotNull(0)).getValue();
        role = new BooleanWrapper(elements.getNotNull(1)).getValue();
        writing = new BooleanWrapper(elements.getNotNull(2)).getValue();
        context = elements.getNotNull(3).isNotEmpty() ? new Context(elements.getNotNull(3)) : null;
        contact = elements.getNotNull(4).isNotEmpty() ? new NonHostIdentifier(elements.getNotNull(4)).getIdentity().toPerson() : null;
        
        if (context != null && contact != null) throw new InvalidEncodingException("The context or the contact is null.");
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
