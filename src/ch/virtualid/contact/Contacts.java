package ch.virtualid.contact;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Frozen;
import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.NonFrozen;
import ch.virtualid.annotations.NonFrozenRecipient;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a set of {@link Contact contacts}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Contacts extends FreezableLinkedHashSet<Contact> implements ReadonlyContacts, Blockable, SQLizable {
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code list.contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("list.contact@virtualid.ch").load(ListWrapper.TYPE, Contact.TYPE);
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates an empty set of contacts.
     */
    public @NonFrozen Contacts() {}
    
    /**
     * Creates new set with the given contact.
     * 
     * @param contact the contact to be added.
     */
    public @NonFrozen Contacts(@Nonnull Contact contact) {
        add(contact);
    }
    
    /**
     * Creates new set from the given contact.
     * 
     * @param contacts the contacts to be added.
     */
    public @NonFrozen Contacts(@Nonnull ReadonlyContacts contacts) {
        addAll(contacts);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Blockable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(size());
        for (final @Nonnull Contact contact : this) elements.add(contact.toBlock());
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    /**
     * Creates new contacts from the given block.
     * 
     * @param entity the entity to which the contacts belong.
     * @param block the block containing the contacts.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public @NonFrozen Contacts(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) add(Contact.get(entity, element));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull @Frozen ReadonlyContacts freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen Contacts clone() {
        return new Contacts(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Editing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Adds the given contacts to these contacts.
     * 
     * @param contacts the contacts to add to these contacts.
     */
    @NonFrozenRecipient
    public void addAll(@Nonnull ReadonlyContacts contacts) {
        super.addAll((Contacts) contacts);
    }
    
    /**
     * Removes the given contacts from these contacts.
     * 
     * @param contacts the contacts to remove from these contacts.
     */
    @NonFrozenRecipient
    public void removeAll(@Nonnull ReadonlyContacts contacts) {
        super.removeAll((Contacts) contacts);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– SQLizable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the contacts belong.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull @NonFrozen Contacts get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Contacts contacts = new Contacts();
        while (resultSet.next()) contacts.add(Contact.getNotNull(entity, resultSet, columnIndex));
        return contacts;
     }
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        for (final @Nonnull Contact contact : this) {
            contact.set(preparedStatement, parameterIndex);
            preparedStatement.addBatch();
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull Contact contact : this) {
            if (string.length() != 1) string.append(", ");
            string.append(contact.getPerson().getAddress());
        }
        string.append("]");
        return string.toString();
    }
    
}
