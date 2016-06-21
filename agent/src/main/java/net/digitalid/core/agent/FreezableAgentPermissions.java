package net.digitalid.core.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.converter.Brackets;
import net.digitalid.utility.collections.converter.ElementConverter;
import net.digitalid.utility.collections.converter.IterableConverter;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.freezable.NonFrozenRecipient;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingle;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingleRecipient;
import net.digitalid.utility.validation.annotations.size.Single;

import net.digitalid.database.core.Database;
import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.AttributeType;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

/**
 * This class models the permissions of agents as a mapping from attribute types to writings.
 * 
 * @invariant areValid() : "These agent permissions are always valid.";
 */
public final class FreezableAgentPermissions extends FreezableLinkedHashMap<SemanticType, Boolean> implements ReadOnlyAgentPermissions {
    
    /**
     * Stores the semantic type {@code type.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ATTRIBUTE_TYPE = SemanticType.map("type.permission.agent@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code writing.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType WRITING = SemanticType.map("writing.permission.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PERMISSION = SemanticType.map("permission.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, ATTRIBUTE_TYPE, WRITING); 
    
    /**
     * Stores the semantic type {@code list.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("list.permission.agent@core.digitalid.net").load(ListWrapper.XDF_TYPE, PERMISSION);
    
    
    /**
     * Stores the semantic type {@code general.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType GENERAL = SemanticType.map("general.permission.agent@core.digitalid.net").load(new Category[] {Category.HOST, Category.SYNTACTIC_TYPE, Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores an empty set of agent permissions.
     */
    public static final @Nonnull ReadOnlyAgentPermissions NONE = new FreezableAgentPermissions().freeze();
    
    /**
     * Stores a general read permission.
     */
    public static final @Nonnull ReadOnlyAgentPermissions GENERAL_READ = new FreezableAgentPermissions(GENERAL, false).freeze();
    
    /**
     * Stores a general write permission.
     */
    public static final @Nonnull ReadOnlyAgentPermissions GENERAL_WRITE = new FreezableAgentPermissions(GENERAL, true).freeze();
    
    
    /**
     * Creates an empty map of agent permissions.
     */
    public FreezableAgentPermissions() {}
    
    /**
     * Creates new agent permissions with the given type and access.
     * 
     * @param type the attribute type of the agent permission.
     * @param writing the access to the given attribute type.
     */
    public @Single FreezableAgentPermissions(@Nonnull @AttributeType SemanticType type, @Nonnull Boolean writing) {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        put(type, writing);
    }
    
    /**
     * Creates new agent permissions from the given agent permissions.
     * 
     * @param permissions the agent permissions to add to the new agent permissions.
     */
    public FreezableAgentPermissions(@Nonnull ReadOnlyAgentPermissions permissions) {
        putAll(permissions);
    }
    
    /**
     * Creates new agent permissions from the given block.
     * 
     * @param block the block containing the agent permissions.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public FreezableAgentPermissions(@Nonnull Block block) throws ExternalException {
        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
        
        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull ReadOnlyArray<Block> subelements = TupleWrapper.decode(element).getNonNullableElements(2);
            final @Nonnull SemanticType type = IdentityImplementation.create(subelements.getNonNullable(0)).castTo(SemanticType.class).checkIsAttributeType();
            put(type, BooleanWrapper.decode(subelements.getNonNullable(1)));
        }
        
        if (!areValid()) { throw InvalidParameterValueException.get("agent permissions", this); }
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableLinkedList<>();
        for (final @Nonnull SemanticType semanticType : keySet()) {
            final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(2);
            subelements.set(0, semanticType.toBlock(ATTRIBUTE_TYPE));
            subelements.set(1, BooleanWrapper.encode(WRITING, get(semanticType)));
            elements.add(TupleWrapper.encode(PERMISSION, subelements.freeze()));
        }
        return ListWrapper.encode(TYPE, elements.freeze());
    }
    
    
    @Override
    public @Nonnull @Frozen ReadOnlyAgentPermissions freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean areValid() {
        if (containsKey(GENERAL)) {
            if (get(GENERAL)) {
                return areEmptyOrSingle();
            } else {
                for (final @Nonnull SemanticType semanticType : keySet()) {
                    if (!semanticType.equals(GENERAL) && !get(semanticType)) { return false; }
                }
                return true;
            }
        } else {
            return true;
        }
    }
    
    @Pure
    @Override
    public boolean isSingle() {
        return size() == 1;
    }
    
    /**
     * Checks that these permissions are single.
     * 
     * @return these permissions.
     * 
     * @throws InvalidEncodingException if this is not the case.
     */
    @Pure
    public @Nonnull FreezableAgentPermissions checkIsSingle() throws InvalidParameterValueException {
        if (!isSingle()) { throw InvalidParameterValueException.get("agent permissions", this); }
        return this;
    }
    
    @Pure
    @Override
    public boolean areEmptyOrSingle() {
        return size() <= 1;
    }
    
    /**
     * Checks that these permissions are empty or single.
     * 
     * @return these permissions.
     * 
     * @throws InvalidEncodingException if this is not the case.
     */
    @Pure
    public @Nonnull FreezableAgentPermissions checkAreEmptyOrSingle() throws InvalidParameterValueException {
        if (!areEmptyOrSingle()) { throw InvalidParameterValueException.get("agent permissions", this); }
        return this;
    }
    
    
    @Pure
    @Override
    public boolean canRead(@Nonnull SemanticType type) {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        return containsKey(type) || containsKey(GENERAL);
    }
    
    @Pure
    @Override
    public void checkCanRead(@Nonnull SemanticType type) throws RequestException {
        if (!canRead(type)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "These agent permissions cannot read " + type.getAddress() + "."); }
    }
    
    @Pure
    @Override
    public boolean canWrite(@Nonnull SemanticType type) {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        return containsKey(type) && get(type) || containsKey(GENERAL) && get(GENERAL);
    }
    
    @Pure
    @Override
    public void checkCanWrite(@Nonnull SemanticType type) throws RequestException {
        if (!canWrite(type)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "These agent permissions cannot write " + type.getAddress() + "."); }
    }
    
    @Pure
    @Override
    public boolean cover(@Nonnull ReadOnlyAgentPermissions permissions) {
        final boolean generalPermission = containsKey(GENERAL);
        final boolean writingPermission = generalPermission ? get(GENERAL) : false;
        for (final @Nonnull SemanticType type : permissions.keySet()) {
            if (containsKey(type)) {
                if (permissions.get(type) && !get(type)) { return false; }
            } else if (generalPermission) {
                if (permissions.get(type) && !writingPermission) { return false; }
            } else {
                return false;
            }
        }
        return true;
    }
    
    @Pure
    @Override
    public void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws RequestException {
        if (!cover(permissions)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "These agent permissions do not cover " + permissions + "."); }
    }
    
    
    /**
     * Restricts these agent permissions to the given agent permissions.
     * 
     * @param permissions the agent permissions with which to restrict these agent permissions.
     */
    @NonFrozenRecipient
    public void restrictTo(@Nonnull ReadOnlyAgentPermissions permissions) {
        Require.that(!isFrozen()).orThrow("This object is not frozen.");
        
        if (containsKey(GENERAL)) {
            if (get(GENERAL)) {
                clear();
                putAll(permissions);
            } else {
                if (!permissions.get(GENERAL)) {
                    remove(GENERAL);
                    for (final @Nonnull SemanticType type : keySet()) {
                        if (permissions.containsKey(type)) {
                            if (get(type) && !permissions.get(type)) { put(type, false); }
                        } else {
                            remove(type);
                        }
                    }
                    for (final @Nonnull SemanticType semanticType : permissions.keySet()) {
                        if (!containsKey(semanticType)) { put(semanticType, false); }
                    }
                }
            }
        } else {
            final boolean generalPermission = permissions.containsKey(GENERAL);
            final boolean writingPermission = generalPermission ? permissions.get(GENERAL) : false;
            for (final @Nonnull SemanticType type : keySet()) {
                if (permissions.containsKey(type)) {
                    if (get(type) && !permissions.get(type)) { put(type, false); }
                } else if (generalPermission) {
                    if (get(type) && !writingPermission) { put(type, false); }
                } else {
                    remove(type);
                }
            }
        }
    }
    
    
    /**
     * The agent permission is only added if it is not yet covered.
     * 
     * @param type the attribute type of the permission to add.
     * @param writing the access to the given attribute type.
     * 
     * @return the previous value associated with <tt>key</tt> or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    @Override
    @NonFrozenRecipient
    public @Nullable Boolean put(@Nonnull @AttributeType SemanticType type, @Nonnull Boolean writing) {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        boolean put;
        if (type.equals(GENERAL)) {
            if (writing) {
                super.clear();
            } else {
                for (final @Nonnull SemanticType key : keySet()) {
                    if (!get(key)) { remove(key); }
                }
            }
            put = true;
        } else {
            put = writing && !canWrite(type) || !writing && !canRead(type);
        }
        return put ? super.put(type, writing) : null;
    }
    
    /**
     * Only those agent permissions are added that are not yet covered.
     * 
     * @param permissions the permissions to add to these permissions.
     */
    @NonFrozenRecipient
    public void putAll(@Nonnull ReadOnlyAgentPermissions permissions) {
        for (final @Nonnull SemanticType type : permissions.keySet()) {
            put(type, permissions.get(type));
        }
    }
    
    /**
     * Removes the given permissions from these permissions.
     * 
     * @param permissions the permissions to remove from these permissions.
     */
    @NonFrozenRecipient
    public void removeAll(@Nonnull ReadOnlyAgentPermissions permissions) {
        for (final @Nonnull SemanticType type : permissions.keySet()) {
            remove(type);
        }
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions clone() {
        return new FreezableAgentPermissions(this);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(keySet(), new ElementConverter<SemanticType>() { @Pure @Override public String toString(@Nullable SemanticType type) { return type == null ? "null" : type.getAddress().getString() + ": " + (get(type) ? "write" : "read"); } }, Brackets.SQUARE);
    }
    
    
    @Pure
    @Override
    public @Nonnull String allTypesToString() {
        if (!canRead(GENERAL)) {
            final @Nonnull StringBuilder string = new StringBuilder(" AND ");
            if (!isEmpty()) {
                string.append("type IN (");
                for (final @Nonnull SemanticType type : keySet()) {
                    if (string.length() != 14) { string.append(", "); }
                    string.append(type);
                }
                string.append(")");
            } else {
                string.append(Database.toBoolean(false));
            }
            return string.toString();
        } else {
            return "";
        }
    }
    
    @Pure
    @Override
    public @Nonnull String writeTypesToString() {
        if (!canWrite(GENERAL)) {
            final @Nonnull StringBuilder string = new StringBuilder("(");
            for (final @Nonnull SemanticType type : keySet()) {
                if (get(type)) {
                    if (string.length() != 1) { string.append(", "); }
                    string.append(type);
                }
            }
            string.append(")");
            final @Nonnull String list = string.toString();
            if (!list.equals("()")) { return " AND type IN " + list; }
            else { return " AND " + Database.toBoolean(false); }
        } else {
            return "";
        }
    }
    
    
    /**
     * Stores the columns used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT_NOT_NULL = "type " + Mapper.FORMAT + " NOT NULL, type_writing BOOLEAN NOT NULL";
    
    /**
     * Stores the columns used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT_NULL = "type " + Mapper.FORMAT + ", type_writing BOOLEAN";
    
    /**
     * Stores the columns used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String COLUMNS = "type, type_writing";
    
    /**
     * Stores the condition used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String CONDITION = "type = ? AND type_writing = ?";
    
    /**
     * Stores the foreign key constraints used by instances of this class.
     */
    public static final @Nonnull String REFERENCE = "FOREIGN KEY (type) " + Mapper.REFERENCE;
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param startIndex the start index of the columns containing the data.
     * 
     * @return the given columns of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull @NonFrozen FreezableAgentPermissions get(@Nonnull ResultSet resultSet, int startIndex) throws DatabaseException {
        try {
            final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
            while (resultSet.next()) {
                final @Nonnull SemanticType type = IdentityImplementation.getNotNull(resultSet, startIndex).castTo(SemanticType.class).checkIsAttributeType();
                final boolean writing = resultSet.getBoolean(startIndex + 1);
                permissions.put(type, writing);
            }
            return permissions;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
     }
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param startIndex the start index of the columns containing the data.
     * 
     * @return the given columns of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Capturable @Nonnull @NonFrozen @EmptyOrSingle FreezableAgentPermissions getEmptyOrSingle(@Nonnull ResultSet resultSet, int startIndex) throws DatabaseException {
        try {
            final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
            final @Nullable Identity identity = IdentityImplementation.get(resultSet, startIndex);
            if (identity != null) { permissions.put(identity.castTo(SemanticType.class).checkIsAttributeType(), resultSet.getBoolean(startIndex + 1)); }
            return permissions;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
     }
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException {
        for (final @Nonnull SemanticType type : keySet()) {
            type.set(preparedStatement, startIndex);
            preparedStatement.setBoolean(startIndex + 1, get(type));
            preparedStatement.addBatch();
        }
    }
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    @Override
    @NonCommitting
    @EmptyOrSingleRecipient
    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException {
        Require.that(areEmptyOrSingle()).orThrow("These permissions are empty or single.");
        
        final @Nonnull FreezableSet<SemanticType> keySet = keySet();
        if (keySet.isEmpty()) {
            preparedStatement.setNull(startIndex, Types.BIGINT);
            preparedStatement.setNull(startIndex + 1, Types.BOOLEAN);
        } else {
            for (final @Nonnull SemanticType type : keySet()) {
                type.set(preparedStatement, startIndex);
                preparedStatement.setBoolean(startIndex + 1, get(type));
            }
        }
    }
    
}
