package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the permissions of clients as a mapping from attribute types to writings.
 * 
 * @invariant areValid() : "These permissions are always valid.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Permissions extends FreezableLinkedHashMap<SemanticType, Boolean> implements ReadonlyPermissions {
    
    /**
     * Stores the semantic type {@code general.permission.client@virtualid.ch}.
     */
    public static final @Nonnull SemanticType CLIENT_GENERAL_PERMISSION = mapSemanticType(NonHostIdentifier.CLIENT_GENERAL_PERMISSION);    
    
    /**
     * Stores an empty set of permissions.
     */
    public static final @Nonnull ReadonlyPermissions NONE = new Permissions().freeze();
    
    
    /**
     * Creates an empty set of permissions.
     */
    public Permissions() {}
    
    /**
     * Creates new permissions with the given type and access.
     * 
     * @param type the attribute type of the permission.
     * @param writing the access to the given attribute type.
     * 
     * @ensure areSingle() : "The new permissions are single.";
     */
    public Permissions(@Nonnull SemanticType type, @Nonnull Boolean writing) {
        put(type, writing);
    }
    
    /**
     * Creates new permissions from the given block.
     * 
     * @param block the block containing the permissions.
     */
    public Permissions(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        @Nonnull List<Block> elements = new ListWrapper(block).getElements();
        for (@Nonnull Block element : elements) {
            @Nonnull Block[] subelements = new TupleWrapper(element).getElementsNotNull(2);
            
            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(subelements[0]);
            @Nonnull SemanticType type = identifier.getIdentity().toSemanticType();
            
            put(type, new BooleanWrapper(subelements[1]).getValue());
        }
        
        if (!areValid()) throw new InvalidEncodingException("The permissions in the given block are not valid.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        @Nonnull List<Block> elements = new LinkedList<Block>();
        for (@Nonnull SemanticType semanticType : keySet()) {
            @Nonnull Block[] subelements = new Block[2];
            subelements[0] = semanticType.getAddress().toBlock();
            subelements[1] = new BooleanWrapper(get(semanticType)).toBlock();
            elements.add(new TupleWrapper(subelements).toBlock());
        }
        return new ListWrapper(elements).toBlock();
    }
    
    
    @Pure
    @Override
    public boolean areValid() {
        if (containsKey(SemanticType.CLIENT_GENERAL_PERMISSION)) {
            if (get(SemanticType.CLIENT_GENERAL_PERMISSION)) {
                return areSingle();
            } else {
                for (@Nonnull SemanticType semanticType : keySet()) {
                    if (!semanticType.equals(SemanticType.CLIENT_GENERAL_PERMISSION) && !get(semanticType)) return false;
                }
                return true;
            }
        } else {
            return true;
        }
    }
    
    @Pure
    @Override
    public boolean areSingle() {
        return size() == 1;
    }
    
    
    @Override
    public @Nonnull ReadonlyPermissions freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean canRead(@Nonnull SemanticType type) {
        return containsKey(type) || containsKey(SemanticType.CLIENT_GENERAL_PERMISSION);
    }
    
    @Pure
    @Override
    public void checkRead(@Nonnull SemanticType type) throws PacketException {
        if (!canRead(type)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    @Pure
    @Override
    public boolean canWrite(@Nonnull SemanticType type) {
        return containsKey(type) && get(type) || containsKey(SemanticType.CLIENT_GENERAL_PERMISSION) && get(SemanticType.CLIENT_GENERAL_PERMISSION);
    }
    
    @Pure
    @Override
    public void checkWrite(@Nonnull SemanticType type) throws PacketException {
        if (!canWrite(type)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    @Pure
    @Override
    public boolean cover(@Nonnull ReadonlyPermissions permissions) {
        boolean generalPermission = containsKey(SemanticType.CLIENT_GENERAL_PERMISSION);
        boolean writingPermission = generalPermission ? get(SemanticType.CLIENT_GENERAL_PERMISSION) : false;
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
    public void checkCoverage(@Nonnull ReadonlyPermissions permissions) throws PacketException {
        if (!cover(permissions)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    /**
     * Restricts these permissions to the given permissions.
     * 
     * @param permissions the permissions with which to restrict these permissions.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void restrictTo(@Nonnull ReadonlyPermissions permissions) {
        assert isNotFrozen() : "This object is not frozen.";
        
        if (containsKey(SemanticType.CLIENT_GENERAL_PERMISSION)) {
            if (get(SemanticType.CLIENT_GENERAL_PERMISSION)) {
                clear();
                putAll(permissions);
            } else {
                if (!permissions.get(SemanticType.CLIENT_GENERAL_PERMISSION)) {
                    remove(SemanticType.CLIENT_GENERAL_PERMISSION);
                    for (@Nonnull SemanticType type : keySet()) {
                        if (permissions.containsKey(type)) {
                            if (get(type) && !permissions.get(type)) put(type, false);
                        } else {
                            remove(type);
                        }
                    }
                    for (@Nonnull SemanticType semanticType : permissions.keySet()) {
                        if (!containsKey(semanticType)) put(semanticType, false);
                    }
                }
            }
        } else {
            boolean generalPermission = permissions.containsKey(SemanticType.CLIENT_GENERAL_PERMISSION);
            boolean writingPermission = generalPermission ? permissions.get(SemanticType.CLIENT_GENERAL_PERMISSION) : false;
            for (@Nonnull SemanticType type : keySet()) {
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
     * The permission is only added if it is not yet covered.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable Boolean put(@Nonnull SemanticType type, @Nonnull Boolean writing) {
        boolean put;
        if (type.equals(SemanticType.CLIENT_GENERAL_PERMISSION)) {
            if (writing) {
                super.clear();
            } else {
                for (@Nonnull SemanticType key : keySet()) {
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
     * Only those permissions are added that are not yet covered.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public void putAll(@Nonnull ReadonlyPermissions permissions) {
        for (@Nonnull SemanticType type : permissions.keySet()) {
            put(type, permissions.get(type));
        }
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        @Nonnull StringBuilder string = new StringBuilder("[");
        for (@Nonnull SemanticType type : keySet()) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString()).append(": ").append(get(type) ? "write" : "read");
        }
        string.append("]");
        return string.toString();
    }
    
}
