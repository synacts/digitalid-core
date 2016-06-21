package net.digitalid.core.entity;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.external.MaskingInvalidEncodingException;
import net.digitalid.utility.system.castable.Castable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Locked;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.client.Client;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.conversion.Converters;
import net.digitalid.core.conversion.key.CastingNonRequestingKeyConverter;
import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.conversion.xdf.ChainingRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.handler.Handler;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.resolution.Mapper;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept} or {@link Handler handler}.
 * 
 * @see EntityImplementation
 * @see HostEntity
 * @see NonHostEntity
 */
@Immutable
public interface Entity extends Castable, XDF<Entity, Site>, SQL<Entity, Site> {
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public @Nonnull InternalIdentity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    /**
     * This class allows to convert an entity to its identity and recover it again by downcasting the entity returned by the overridden method to the given target class.
     */
    @Immutable
    public static final class IdentityConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, InternalIdentity, Object, Entity> {
        
        /**
         * Creates a new entity-identity converter with the given target class.
         * 
         * @param targetClass the target class to which the recovered object is cast.
         */
        protected IdentityConverter(@Nonnull Class<E> targetClass) {
            super(targetClass);
        }
        
        @Pure
        @Override
        public @Nonnull Object decompose(@Nonnull Site site) {
            return None.OBJECT;
        }
        
        @Pure
        @Override
        public @Nonnull InternalIdentity convert(@Nonnull E entity) {
            return entity.getIdentity();
        }
        
        @Pure
        @Override
        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull InternalIdentity identity) throws InternalException {
            if (site instanceof Host) {
                return Account.get((Host) site, identity);
            } else {
                // Entities are encoded through their identity, which is not enough to recover roles.
                // (There can exist several roles for the same identity through different paths.)
                throw InternalException.get("Roles cannot be recovered from a block.");
            }
        }
        
    }
    
    /**
     * This class allows to convert an entity to its key and recover it again by downcasting the entity returned by the overridden method to the given target class.
     */
    @Immutable
    public static final class LongConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, Long, Object, Entity> {
        
        /**
         * Creates a new identity-long converter with the given target class.
         * 
         * @param targetClass the target class to which the recovered object is cast.
         */
        protected LongConverter(@Nonnull Class<E> targetClass) {
            super(targetClass);
        }
        
        @Pure
        @Override
        public @Nonnull Long convert(@Nonnull E entity) {
            return entity.getKey();
        }
        
        @Pure
        @Override
        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull Long key) throws InvalidEncodingException, InternalException {
            try {
                if (site instanceof Host) {
                    return Account.get((Host) site, Mapper.getIdentity(key).castTo(InternalIdentity.class));
                } else if (site instanceof Client) {
                    return Role.get((Client) site, key);
                } else {
                    throw InternalException.get("The site is always a host or a client.");
                }
            } catch (@Nonnull DatabaseException exception) {
                throw MaskingInvalidEncodingException.get(exception);
            }
        }
        
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<Entity, Site> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Entity.IdentityConverter<>(Entity.class), InternalIdentity.XDF_CONVERTER);
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    /**
     * The column declaration for identities that registers at the mapper.
     */
    @Immutable
    public static final class Declaration extends ColumnDeclaration {
        
        /**
         * Creates a new entity declaration with the given name.
         * 
         * @param name the name of the new entity declaration.
         */
        protected Declaration(@Nonnull @Validated String name) {
            super(name, Integer64Wrapper.SQL_TYPE, null);
        }
        
        @Pure
        @Override
        public boolean isSiteSpecific() {
            return true;
        }
        
        @Locked
        @Override
        @NonCommitting
        protected @Nonnull String getForeignKeys(@Nullable Site site, @Nullable @Validated String prefix) throws SQLException {
            if (site instanceof Host) {
                return ", FOREIGN KEY (" + getName(prefix) + ") " + Mapper.REFERENCE.get(null);
            } else if (site instanceof Client) {
                return ", FOREIGN KEY (" + getName(prefix) + ") " + Role.REFERENCE.get(site);
            } else {
                throw new SQLException("The site is always a host or a client.");
            }
        }
        
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Entity.Declaration DECLARATION = new Entity.Declaration("entity");
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Entity, Site> SQL_CONVERTER = ChainingSQLConverter.get(new Entity.LongConverter<>(Entity.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Entity, Site> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
