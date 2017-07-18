package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.entity.factories.RoleFactory;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.annotations.CoreUnitBased;

/**
 * An entity captures the {@link CoreUnit unit} and the {@link Identity identity} of a core subject or handler.
 * 
 * @see NonHostEntity
 */
@Immutable
@GenerateTableConverter
@TODO(task = "This type should probably implement Subject<CoreUnit> again in order to get the foreign key constraints.", date = "2017-04-15", author = Author.KASPAR_ETTER)
public interface Entity extends CoreUnitBased {
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    /**
     * Returns the unit to which this entity belongs.
     */
    @Pure
    @Provided
    public @Nonnull CoreUnit getUnit();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the number that represents this entity in the database.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Returns the identity of this entity.
     */
    @Pure
    @NonRepresentative
    public @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    public default boolean isOnHost() {
        return getUnit().isHost();
    }
    
    @Pure
    @Override
    public default boolean isOnClient() {
        return getUnit().isClient();
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    @SuppressWarnings("unchecked")
    public static @Nonnull Entity with(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        if (unit.isHost()) {
            final @Nonnull Identity identity = IdentifierResolver.configuration.get().load(key);
            if (identity instanceof InternalIdentity) { return AccountFactory.create(unit, (InternalIdentity) identity); }
            else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " does not belong to an internal identity.").build(); }
        } else if (unit.isClient()) {
            return RoleFactory.create(unit, key);
        } else {
            throw CaseExceptionBuilder.withVariable("unit").withValue(unit).build();
        }
    }
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    // TODO: Remove the following code after having made it work with the generated converter.
    
//    /**
//     * This class allows to convert an entity to its identity and recover it again by downcasting the entity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class IdentityConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, InternalIdentity, Object, Entity> {
//        
//        /**
//         * Creates a new entity-identity converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected IdentityConverter(@Nonnull Class<E> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Object decompose(@Nonnull Site site) {
//            return None.OBJECT;
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull InternalIdentity convert(@Nonnull E entity) {
//            return entity.getIdentity();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull InternalIdentity identity) throws InternalException {
//            if (site instanceof Host) {
//                return Account.get((Host) site, identity);
//            } else {
//                // Entities are encoded through their identity, which is not enough to recover roles.
//                // (There can exist several roles for the same identity through different paths.)
//                throw InternalException.get("Roles cannot be recovered from a block.");
//            }
//        }
//        
//    }
//    
//    /**
//     * This class allows to convert an entity to its key and recover it again by downcasting the entity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class LongConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, Long, Object, Entity> {
//        
//        /**
//         * Creates a new identity-long converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected LongConverter(@Nonnull Class<E> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Long convert(@Nonnull E entity) {
//            return entity.getKey();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull Long key) throws InvalidEncodingException, InternalException {
//            try {
//                if (site instanceof Host) {
//                    return Account.get((Host) site, Mapper.getIdentity(key).castTo(InternalIdentity.class));
//                } else if (site instanceof Client) {
//                    return Role.get((Client) site, key);
//                } else {
//                    throw InternalException.get("The site is always a host or a client.");
//                }
//            } catch (@Nonnull DatabaseException exception) {
//                throw MaskingInvalidEncodingException.get(exception);
//            }
//        }
//        
//    }
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    // TODO: Are we capable to generate different foreign key constraints based on the site?
    
//    /**
//     * The column declaration for identities that registers at the mapper.
//     */
//    @Immutable
//    public static final class Declaration extends ColumnDeclaration {
//        
//        /**
//         * Creates a new entity declaration with the given name.
//         * 
//         * @param name the name of the new entity declaration.
//         */
//        protected Declaration(@Nonnull @Validated String name) {
//            super(name, Integer64Wrapper.SQL_TYPE, null);
//        }
//        
//        @Pure
//        @Override
//        public boolean isSiteSpecific() {
//            return true;
//        }
//        
//        @Locked
//        @Override
//        @NonCommitting
//        protected @Nonnull String getForeignKeys(@Nullable Site site, @Nullable @Validated String prefix) throws SQLException {
//            if (site instanceof Host) {
//                return ", FOREIGN KEY (" + getName(prefix) + ") " + Mapper.REFERENCE.get(null);
//            } else if (site instanceof Client) {
//                return ", FOREIGN KEY (" + getName(prefix) + ") " + Role.REFERENCE.get(site);
//            } else {
//                throw new SQLException("The site is always a host or a client.");
//            }
//        }
//        
//    }
    
}
