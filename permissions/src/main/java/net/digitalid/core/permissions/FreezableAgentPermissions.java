package net.digitalid.core.permissions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.collections.map.FreezableLinkedHashMap;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Freezable;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.freezable.annotations.NonFrozenRecipient;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.method.Chainable;
import net.digitalid.utility.validation.annotations.size.Empty;
import net.digitalid.utility.validation.annotations.size.Single;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class models the permissions of agents as a mapping from attribute types to writings.
 */
@GenerateSubclass
@Freezable(ReadOnlyAgentPermissions.class)
public abstract class FreezableAgentPermissions extends FreezableLinkedHashMap<@Nonnull SemanticType, @Nonnull Boolean> implements ReadOnlyAgentPermissions {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    // TODO: Remove the following code once these types are generated implicitly.
    
//    /**
//     * Stores the semantic type {@code type.permission.agent@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType ATTRIBUTE_TYPE = SemanticType.map("type.permission.agent@core.digitalid.net").load(SemanticType.IDENTIFIER);
//    
//    /**
//     * Stores the semantic type {@code writing.permission.agent@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType WRITING = SemanticType.map("writing.permission.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code permission.agent@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType PERMISSION = SemanticType.map("permission.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, ATTRIBUTE_TYPE, WRITING); 
//    
//    /**
//     * Stores the semantic type {@code list.permission.agent@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType TYPE = SemanticType.map("list.permission.agent@core.digitalid.net").load(ListWrapper.XDF_TYPE, PERMISSION);
    
    // TODO: Remove the following code once the generated converter for the read-only type handles this type properly.
    
//    @NonCommitting
//    public FreezableAgentPermissions(@Nonnull Block block) throws ExternalException {
//        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
//        
//        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
//        for (final @Nonnull Block element : elements) {
//            final @Nonnull ReadOnlyArray<Block> subelements = TupleWrapper.decode(element).getNonNullableElements(2);
//            final @Nonnull SemanticType type = IdentityImplementation.create(subelements.getNonNullable(0)).castTo(SemanticType.class).checkIsAttributeType();
//            put(type, BooleanWrapper.decode(subelements.getNonNullable(1)));
//        }
//        
//        if (!areValid()) { throw InvalidParameterValueException.get("agent permissions", this); }
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Block toBlock() {
//        final @Nonnull FreezableList<Block> elements = new FreezableLinkedList<>();
//        for (final @Nonnull SemanticType semanticType : keySet()) {
//            final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(2);
//            subelements.set(0, semanticType.toBlock(ATTRIBUTE_TYPE));
//            subelements.set(1, BooleanWrapper.encode(WRITING, get(semanticType)));
//            elements.add(TupleWrapper.encode(PERMISSION, subelements.freeze()));
//        }
//        return ListWrapper.encode(TYPE, elements.freeze());
//    }
    
    /* -------------------------------------------------- Modifications -------------------------------------------------- */
    
    /**
     * Adds the given permission if it is not yet covered and returns the previous permission associated with the given type.
     */
    @Impure
    @Override
    @NonFrozenRecipient
    public @Nullable Boolean put(@Nonnull @AttributeType SemanticType type, @Nonnull Boolean writing) {
        final boolean put;
        if (type.equals(GENERAL)) {
            if (writing) {
                super.clear();
            } else {
                removeAll(readableTypes());
            }
            put = true;
        } else {
            put = writing && !allowToWrite(type) || !writing && !allowToRead(type);
        }
        return put ? super.put(type, writing) : null;
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected FreezableAgentPermissions() {
        super(4, 0.75f, false);
    }
    
    /**
     * Returns new agent permissions with no permissions.
     */
    @Pure
    public static @Nonnull @NonFrozen @Empty FreezableAgentPermissions withNoPermissions() {
        return new FreezableAgentPermissionsSubclass();
    }
    
    /**
     * Returns new agent permissions with the given access to the given type.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableAgentPermissions withPermission(@Nonnull @AttributeType SemanticType type, @Nonnull Boolean writing) {
        final @Nonnull FreezableAgentPermissions result = new FreezableAgentPermissionsSubclass();
        result.put(type, writing);
        return result;
    }
    
    /**
     * Returns new agent permissions with the given agent permissions.
     */
    @Pure
    public static @Nonnull @NonFrozen FreezableAgentPermissions withPermissionsOf(@Nonnull ReadOnlyAgentPermissions permissions) {
        final @Nonnull FreezableAgentPermissions result = new FreezableAgentPermissionsSubclass();
        result.putAll(permissions);
        return result;
    }
    
    /* -------------------------------------------------- Restriction -------------------------------------------------- */
    
    /**
     * Restricts these agent permissions to the given agent permissions.
     */
    @Impure
    @NonFrozenRecipient
    public void restrictTo(@Nonnull ReadOnlyAgentPermissions permissions) {
        for (@Nonnull SemanticType type : keySet()) {
            if (!permissions.allowToRead(type)) { remove(type); }
            else if (get(type) && !permissions.allowToWrite(type)) { put(type, false); }
        }
    }
    
    /* -------------------------------------------------- Validatable -------------------------------------------------- */
    
    @Pure
    @Override
    public void validate() {
        if (containsKey(GENERAL)) {
            if (get(GENERAL)) {
                Validate.that(isSingle()).orThrow("These permissions may contain no other permissions besides the general write permission but were $.", this);
            } else {
                for (@Nonnull SemanticType semanticType : keySet()) {
                    Validate.that(semanticType.equals(GENERAL) || get(semanticType)).orThrow("These permissions may contain no other read permissions besides the general read permission but were $.", this);
                }
            }
        }
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public @Chainable @Nonnull @Frozen ReadOnlyAgentPermissions freeze() {
        super.freeze();
        return this;
    }
    
    /* -------------------------------------------------- Size -------------------------------------------------- */
    
    // TODO: Remove the following code if it is no longer needed because such aspects should now be checked with contracts.
    
//    /**
//     * Checks that these permissions are single.
//     * 
//     * @return these permissions.
//     * 
//     * @throws InvalidEncodingException if this is not the case.
//     */
//    @Pure
//    @Chainable
//    public @Nonnull FreezableAgentPermissions checkIsSingle() throws InvalidParameterValueException {
//        if (!isSingle()) { throw InvalidParameterValueException.get("agent permissions", this); }
//        return this;
//    }
//    
//    /**
//     * Checks that these permissions are empty or single.
//     * 
//     * @return these permissions.
//     * 
//     * @throws InvalidEncodingException if this is not the case.
//     */
//    @Pure
//    @Chainable
//    public @Nonnull FreezableAgentPermissions checkIsEmptyOrSingle() throws InvalidParameterValueException {
//        if (!isEmptyOrSingle()) { throw InvalidParameterValueException.get("agent permissions", this); }
//        return this;
//    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions clone() {
        return FreezableAgentPermissions.withPermissionsOf(this);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return entrySet().map(entry -> entry.getKey().getAddress() + ": " + (entry.getValue() ? "write" : "read")).join(Brackets.CURLY);
    }
    
    /* -------------------------------------------------- SQL Condition -------------------------------------------------- */
    
    // TODO: Do we still need something like this? And if yes, we probably want to return an SQL syntax node.
    
//    @Pure
//    @Override
//    public @Nonnull String allTypesToString() {
//        if (!canRead(GENERAL)) {
//            final @Nonnull StringBuilder string = new StringBuilder(" AND ");
//            if (!isEmpty()) {
//                string.append("type IN (");
//                for (final @Nonnull SemanticType type : keySet()) {
//                    if (string.length() != 14) { string.append(", "); }
//                    string.append(type);
//                }
//                string.append(")");
//            } else {
//                string.append(Database.toBoolean(false));
//            }
//            return string.toString();
//        } else {
//            return "";
//        }
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String writeTypesToString() {
//        if (!canWrite(GENERAL)) {
//            final @Nonnull StringBuilder string = new StringBuilder("(");
//            for (final @Nonnull SemanticType type : keySet()) {
//                if (get(type)) {
//                    if (string.length() != 1) { string.append(", "); }
//                    string.append(type);
//                }
//            }
//            string.append(")");
//            final @Nonnull String list = string.toString();
//            if (!list.equals("()")) { return " AND type IN " + list; }
//            else { return " AND " + Database.toBoolean(false); }
//        } else {
//            return "";
//        }
//    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    // TODO: Remove the following code once agent permissions are successfully stored in the database.
    
//    /**
//     * Stores the columns used to store instances of this class in the database.
//     */
//    public static final @Nonnull String FORMAT_NOT_NULL = "type " + Mapper.FORMAT + " NOT NULL, type_writing BOOLEAN NOT NULL";
//    
//    /**
//     * Stores the columns used to store instances of this class in the database.
//     */
//    public static final @Nonnull String FORMAT_NULL = "type " + Mapper.FORMAT + ", type_writing BOOLEAN";
//    
//    /**
//     * Stores the columns used to retrieve instances of this class from the database.
//     */
//    public static final @Nonnull String COLUMNS = "type, type_writing";
//    
//    /**
//     * Stores the condition used to retrieve instances of this class from the database.
//     */
//    public static final @Nonnull String CONDITION = "type = ? AND type_writing = ?";
//    
//    /**
//     * Stores the foreign key constraints used by instances of this class.
//     */
//    public static final @Nonnull String REFERENCE = "FOREIGN KEY (type) " + Mapper.REFERENCE;
//    
//    /**
//     * Returns the given columns of the result set as an instance of this class.
//     * 
//     * @param resultSet the result set to retrieve the data from.
//     * @param startIndex the start index of the columns containing the data.
//     * 
//     * @return the given columns of the result set as an instance of this class.
//     */
//    @Pure
//    @NonCommitting
//    public static @Capturable @Nonnull @NonFrozen FreezableAgentPermissions get(@Nonnull ResultSet resultSet, int startIndex) throws DatabaseException {
//        try {
//            final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
//            while (resultSet.next()) {
//                final @Nonnull SemanticType type = IdentityImplementation.getNotNull(resultSet, startIndex).castTo(SemanticType.class).checkIsAttributeType();
//                final boolean writing = resultSet.getBoolean(startIndex + 1);
//                permissions.put(type, writing);
//            }
//            return permissions;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//     }
//    
//    /**
//     * Returns the given columns of the result set as an instance of this class.
//     * 
//     * @param resultSet the result set to retrieve the data from.
//     * @param startIndex the start index of the columns containing the data.
//     * 
//     * @return the given columns of the result set as an instance of this class.
//     */
//    @Pure
//    @NonCommitting
//    public static @Capturable @Nonnull @NonFrozen @EmptyOrSingle FreezableAgentPermissions getEmptyOrSingle(@Nonnull ResultSet resultSet, int startIndex) throws DatabaseException {
//        try {
//            final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
//            final @Nullable Identity identity = IdentityImplementation.get(resultSet, startIndex);
//            if (identity != null) { permissions.put(identity.castTo(SemanticType.class).checkIsAttributeType(), resultSet.getBoolean(startIndex + 1)); }
//            return permissions;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//     }
//    
//    /**
//     * Sets the parameters at the given start index of the prepared statement to this object.
//     * 
//     * @param preparedStatement the prepared statement whose parameters are to be set.
//     * @param startIndex the start index of the parameters to set.
//     */
//    @Override
//    @NonCommitting
//    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException {
//        for (final @Nonnull SemanticType type : keySet()) {
//            type.set(preparedStatement, startIndex);
//            preparedStatement.setBoolean(startIndex + 1, get(type));
//            preparedStatement.addBatch();
//        }
//    }
//    
//    /**
//     * Sets the parameters at the given start index of the prepared statement to this object.
//     * 
//     * @param preparedStatement the prepared statement whose parameters are to be set.
//     * @param startIndex the start index of the parameters to set.
//     */
//    @Override
//    @NonCommitting
//    @EmptyOrSingleRecipient
//    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException {
//        Require.that(areEmptyOrSingle()).orThrow("These permissions are empty or single.");
//        
//        final @Nonnull FreezableSet<SemanticType> keySet = keySet();
//        if (keySet.isEmpty()) {
//            preparedStatement.setNull(startIndex, Types.BIGINT);
//            preparedStatement.setNull(startIndex + 1, Types.BOOLEAN);
//        } else {
//            for (final @Nonnull SemanticType type : keySet()) {
//                type.set(preparedStatement, startIndex);
//                preparedStatement.setBoolean(startIndex + 1, get(type));
//            }
//        }
//    }
    
}
