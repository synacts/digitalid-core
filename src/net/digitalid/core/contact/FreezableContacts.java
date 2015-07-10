package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableLinkedHashSet;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;

/**
 * This class models a set of {@link Contact contacts}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class FreezableContacts extends FreezableLinkedHashSet<Contact> implements ReadOnlyContacts {
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code list.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("list.contact@core.digitalid.net").load(ListWrapper.TYPE, Contact.TYPE);
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates an empty set of contacts.
     */
    public @NonFrozen FreezableContacts() {}
    
    /**
     * Creates new set with the given contact.
     * 
     * @param contact the contact to be added.
     */
    public @NonFrozen FreezableContacts(@Nonnull Contact contact) {
        add(contact);
    }
    
    /**
     * Creates new set from the given contact.
     * 
     * @param contacts the contacts to be added.
     */
    public @NonFrozen FreezableContacts(@Nonnull ReadOnlyContacts contacts) {
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
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(size());
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
    public @NonFrozen FreezableContacts(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) add(Contact.get(entity, element));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull @Frozen ReadOnlyContacts freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableContacts clone() {
        return new FreezableContacts(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Editing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Adds the given contacts to these contacts.
     * 
     * @param contacts the contacts to add to these contacts.
     */
    @NonFrozenRecipient
    public void addAll(@Nonnull ReadOnlyContacts contacts) {
        super.addAll((FreezableContacts) contacts);
    }
    
    /**
     * Removes the given contacts from these contacts.
     * 
     * @param contacts the contacts to remove from these contacts.
     */
    @NonFrozenRecipient
    public void removeAll(@Nonnull ReadOnlyContacts contacts) {
        super.removeAll((FreezableContacts) contacts);
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
    public static @Capturable @Nonnull @NonFrozen FreezableContacts get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull FreezableContacts contacts = new FreezableContacts();
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
