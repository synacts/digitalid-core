package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.Category;
import net.digitalid.core.identification.identifier.Identifier;

/**
 * This interface models a digital identity, which can change identifiers and hosts.
 * Note that identity objects are not necessarily unique (e.g. after identities have been merged).
 * TODO: Explain the above properties better.
 * 
 * TODO: The equality and hash code of identities should only depend on their key (so that types are sound hash keys even after relocation).
 * 
 * @see NonHostIdentity
 * @see InternalIdentity
 */
@Mutable
// TODO: @GenerateConverter
public interface Identity extends RootInterface {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the internal number that represents this identity.
     * The key remains the same after relocation but changes after merging.
     */
    @Pure
//    @Volatile // TODO: Make the generated field volatile.
    public long getKey();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Returns the current address of this identity.
     */
    @Pure
    public @Nonnull Identifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Returns the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    // TODO: I'm still uncertain how we want to handle this in the future but tend to caching only in single-access mode and each time database lookups in multi-access mode.
    
//    /**
//     * Returns whether this identity has been merged and updates the internal number and the identifier.
//     * 
//     * @param exception the exception to be rethrown if this identity has not been merged.
//     */
//    @Pure
//    @NonCommitting
//    public boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException;
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    // TODO: Remove the following code as soon as we are able to convert identities differently depending on the format.
    
//    /**
//     * This class allows to convert an identity to its address and recover it again by downcasting the identity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class IdentifierConverter<I extends Identity> extends CastingRequestingKeyConverter<I, Object, Identifier, Object, Identity> {
//        
//        /**
//         * Creates a new identity-identifier converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected IdentifierConverter(@Nonnull Class<I> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Identifier convert(@Nonnull I identity) {
//            return identity.getAddress();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Identifier identifier) throws ExternalException {
//            return identifier.getIdentity();
//        }
//        
//    }
//    
//    /**
//     * This class allows to convert an identity to its key and recover it again by downcasting the identity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class LongConverter<I extends Identity> extends CastingNonRequestingKeyConverter<I, Object, Long, Object, Identity> {
//        
//        /**
//         * Creates a new identity-long converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected LongConverter(@Nonnull Class<I> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Long convert(@Nonnull I identity) {
//            return identity.getKey();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Long key) throws InvalidEncodingException {
//            try {
//                return Mapper.getIdentity(key);
//            } catch (@Nonnull DatabaseException exception) {
//                throw MaskingInvalidEncodingException.get(exception);
//            }
//        }
//        
//    }
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    // TODO: Remove the following code as soon as we are able to track foreign key references to the identity table.
    
//    /**
//     * The column declaration for identities that registers at the mapper.
//     */
//    @Immutable
//    public static final class Declaration extends ColumnDeclaration {
//        
//        /**
//         * Stores whether the identities can be merged.
//         */
//        private final boolean mergeable;
//        
//        /**
//         * Creates a new identity declaration with the given name.
//         * 
//         * @param name the name of the new identity declaration.
//         * @param mergeable whether the identities can be merged.
//         */
//        protected Declaration(@Nonnull @Validated String name, boolean mergeable) {
//            super(name, Integer64Wrapper.SQL_TYPE, Mapper.REFERENCE);
//            
//            this.mergeable = mergeable;
//        }
//        
//        @Locked
//        @Override
//        @NonCommitting
//        public void executeAfterCreation(@Nonnull Statement statement, @Nonnull Table table, @Nullable Site site, boolean unique, @Nullable @Validated String prefix) throws FailedUpdateExecutionException {
//            super.executeAfterCreation(statement, table, site, unique, prefix);
//            if (unique && mergeable) {
//                Mapper.addReference(table.getName(site), getName(prefix), table.getDeclaration().getPrimaryKeyColumnNames().toArray());
//            }
//        }
//        
//        @Locked
//        @Override
//        @NonCommitting
//        public void executeBeforeDeletion(@Nonnull Statement statement, @Nonnull Table table, @Nullable Site site, boolean unique, @Nullable @Validated String prefix) throws FailedUpdateExecutionException {
//            super.executeBeforeDeletion(statement, table, site, unique, prefix);
//            if (unique && mergeable) {
//                Mapper.removeReference(table.getName(site), getName(prefix), table.getDeclaration().getPrimaryKeyColumnNames().toArray());
//            }
//        }
//        
//    }
    
}
