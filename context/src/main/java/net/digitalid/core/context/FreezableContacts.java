package net.digitalid.core.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.converter.Brackets;
import net.digitalid.utility.collections.converter.ElementConverter;
import net.digitalid.utility.collections.converter.IterableConverter;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.freezable.NonFrozenRecipient;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.contact.Contact;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class models a set of {@link Contact contacts}.
 */
public final class FreezableContacts extends FreezableLinkedHashSet<Contact> implements ReadOnlyContacts {
    
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code list.contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("list.contact@core.digitalid.net").load(ListWrapper.XDF_TYPE, Contact.TYPE);
    
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Blockable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(size());
        for (final @Nonnull Contact contact : this) { elements.add(contact.toBlock()); }
        return ListWrapper.encode(TYPE, elements.freeze());
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
    public @NonFrozen FreezableContacts(@Nonnull NonHostEntity entity, @Nonnull Block block) throws ExternalException {
        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
        
        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) { add(Contact.get(entity, element)); }
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Editing -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- SQLizable -------------------------------------------------- */
    
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
    public static @Capturable @Nonnull @NonFrozen FreezableContacts get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        final @Nonnull FreezableContacts contacts = new FreezableContacts();
        while (resultSet.next()) { contacts.add(Contact.getNotNull(entity, resultSet, columnIndex)); }
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
    public void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        for (final @Nonnull Contact contact : this) {
            contact.set(preparedStatement, parameterIndex);
            preparedStatement.addBatch();
        }
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    /**
     * Stores the converter that converts a contact to the desired string.
     */
    private static final @Nonnull ElementConverter<Contact> converter = new ElementConverter<Contact>() { @Pure @Override public String toString(@Nullable Contact contact) { return contact == null ? "null" : contact.getPerson().getAddress().toString(); } };
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(this, converter, Brackets.CURLY);
    }
    
}
