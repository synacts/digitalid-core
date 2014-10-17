package ch.virtualid.agent;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the permissions of agents as a mapping from attribute types to writings.
 * 
 * @invariant areValid() : "These agent permissions are always valid.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class AgentPermissions extends FreezableLinkedHashMap<SemanticType, Boolean> implements ReadonlyAgentPermissions, Blockable {
    
    /**
     * Stores the semantic type {@code type.permission.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ATTRIBUTE_TYPE = SemanticType.create("type.permission.agent@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code writing.permission.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType WRITING = SemanticType.create("writing.permission.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code permission.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType PERMISSION = SemanticType.create("permission.agent@virtualid.ch").load(TupleWrapper.TYPE, ATTRIBUTE_TYPE, WRITING); 
    
    /**
     * Stores the semantic type {@code list.permission.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("list.permission.agent@virtualid.ch").load(ListWrapper.TYPE, PERMISSION);
    
    
    /**
     * Stores the semantic type {@code general.permission.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType GENERAL = SemanticType.create("general.permission.agent@virtualid.ch").load(new Category[] {Category.HOST}, Time.TROPICAL_YEAR, BooleanWrapper.TYPE);
    
    /**
     * Stores an empty set of agent permissions.
     */
    public static final @Nonnull ReadonlyAgentPermissions NONE = new AgentPermissions().freeze();
    
    
    /**
     * Creates an empty map of agent permissions.
     */
    public AgentPermissions() {}
    
    /**
     * Creates new agent permissions with the given type and access.
     * 
     * @param type the attribute type of the agent permission.
     * @param writing the access to the given attribute type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure areSingle() : "The new agent permissions are single.";
     */
    public AgentPermissions(@Nonnull SemanticType type, @Nonnull Boolean writing) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        put(type, writing);
    }
    
    /**
     * Creates new agent permissions from the given agent permissions.
     * 
     * @param permissions the agent permissions to add to the new agent permissions.
     */
    public AgentPermissions(@Nonnull ReadonlyAgentPermissions permissions) {
        putAll(permissions);
    }
    
    /**
     * Creates new agent permissions from the given block.
     * 
     * @param block the block containing the agent permissions.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public AgentPermissions(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull ReadonlyArray<Block> subelements = new TupleWrapper(element).getElementsNotNull(2);
            final @Nonnull SemanticType type = new NonHostIdentifier(subelements.getNotNull(0)).getIdentity().toSemanticType();
            type.checkIsAttributeType();
            put(type, new BooleanWrapper(subelements.getNotNull(1)).getValue());
        }
        
        if (!areValid()) throw new InvalidEncodingException("The agent permissions in the given block are not valid.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableLinkedList<Block>();
        for (final @Nonnull SemanticType semanticType : keySet()) {
            final @Nonnull FreezableArray<Block> subelements = new FreezableArray<Block>(2);
            subelements.set(0, semanticType.getAddress().toBlock().setType(ATTRIBUTE_TYPE));
            subelements.set(1, new BooleanWrapper(WRITING, get(semanticType)).toBlock());
            elements.add(new TupleWrapper(PERMISSION, subelements.freeze()).toBlock());
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    @Override
    public @Nonnull ReadonlyAgentPermissions freeze() {
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
                    if (!semanticType.equals(GENERAL) && !get(semanticType)) return false;
                }
                return true;
            }
        } else {
            return true;
        }
    }
    
    @Pure
    @Override
    public boolean areEmptyOrSingle() {
        return size() <= 1;
    }
    
    
    @Pure
    @Override
    public boolean canRead(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        return containsKey(type) || containsKey(GENERAL);
    }
    
    @Pure
    @Override
    public void checkCanRead(@Nonnull SemanticType type) throws PacketException {
        if (!canRead(type)) throw new PacketException(PacketError.AUTHORIZATION, "These agent permissions cannot read " + type.getAddress() + ".");
    }
    
    @Pure
    @Override
    public boolean canWrite(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        return containsKey(type) && get(type) || containsKey(GENERAL) && get(GENERAL);
    }
    
    @Pure
    @Override
    public void checkCanWrite(@Nonnull SemanticType type) throws PacketException {
        if (!canWrite(type)) throw new PacketException(PacketError.AUTHORIZATION, "These agent permissions cannot write " + type.getAddress() + ".");
    }
    
    @Pure
    @Override
    public boolean cover(@Nonnull ReadonlyAgentPermissions permissions) {
        final boolean generalPermission = containsKey(GENERAL);
        final boolean writingPermission = generalPermission ? get(GENERAL) : false;
        for (@Nonnull SemanticType type : permissions.keySet()) {
            if (containsKey(type)) {
                if (permissions.get(type) && !get(type)) return false;
            } else if (generalPermission) {
                if (permissions.get(type) && !writingPermission) return false;
            } else {
                return false;
            }
        }
        return true;
    }
    
    @Pure
    @Override
    public void checkCover(@Nonnull ReadonlyAgentPermissions permissions) throws PacketException {
        if (!cover(permissions)) throw new PacketException(PacketError.AUTHORIZATION, "These agent permissions do not cover " + permissions + ".");
    }
    
    
    /**
     * Restricts these agent permissions to the given agent permissions.
     * 
     * @param permissions the agent permissions with which to restrict these agent permissions.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void restrictTo(@Nonnull ReadonlyAgentPermissions permissions) {
        assert isNotFrozen() : "This object is not frozen.";
        
        if (containsKey(GENERAL)) {
            if (get(GENERAL)) {
                clear();
                putAll(permissions);
            } else {
                if (!permissions.get(GENERAL)) {
                    remove(GENERAL);
                    for (final @Nonnull SemanticType type : keySet()) {
                        if (permissions.containsKey(type)) {
                            if (get(type) && !permissions.get(type)) put(type, false);
                        } else {
                            remove(type);
                        }
                    }
                    for (final @Nonnull SemanticType semanticType : permissions.keySet()) {
                        if (!containsKey(semanticType)) put(semanticType, false);
                    }
                }
            }
        } else {
            final boolean generalPermission = permissions.containsKey(GENERAL);
            final boolean writingPermission = generalPermission ? permissions.get(GENERAL) : false;
            for (final @Nonnull SemanticType type : keySet()) {
                if (permissions.containsKey(type)) {
                    if (get(type) && !permissions.get(type)) put(type, false);
                } else if (generalPermission) {
                    if (get(type) && !writingPermission) put(type, false);
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
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Override
    public @Nullable Boolean put(@Nonnull SemanticType type, @Nonnull Boolean writing) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        boolean put;
        if (type.equals(GENERAL)) {
            if (writing) {
                super.clear();
            } else {
                for (final @Nonnull SemanticType key : keySet()) {
                    if (!get(key)) remove(key);
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
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void putAll(@Nonnull ReadonlyAgentPermissions permissions) {
        for (final @Nonnull SemanticType type : permissions.keySet()) {
            put(type, permissions.get(type));
        }
    }
    
    /**
     * Removes the given permissions from these permissions.
     * 
     * @param permissions the permissions to remove from these permissions.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void removeAll(@Nonnull ReadonlyAgentPermissions permissions) {
        for (final @Nonnull SemanticType type : permissions.keySet()) {
            remove(type);
        }
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull AgentPermissions clone() {
        return new AgentPermissions(this);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull SemanticType type : keySet()) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString()).append(": ").append(get(type) ? "write" : "read");
        }
        string.append("]");
        return string.toString();
    }
    
}
