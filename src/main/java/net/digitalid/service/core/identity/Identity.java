package net.digitalid.service.core.identity;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.database.core.converter.SQL;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.exceptions.operation.FailedUpdateExecutionException;
import net.digitalid.database.core.table.Site;
import net.digitalid.database.core.table.Table;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.castable.Castable;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.CastingKeyConverter;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.external.encoding.MaskingInvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This interface models a digital identity.
 * 
 * @see IdentityImplementation
 * @see InternalIdentity
 * @see ExternalIdentity
 * 
 * @see Mapper
 */
@Immutable
public interface Identity extends Castable, XDF<Identity, Object>, SQL<Identity, Object> {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the number that represents this identity
     * 
     * @return the number that represents this identity
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Returns the current address of this identity.
     * 
     * @return the current address of this identity.
     */
    @Pure
    public @Nonnull Identifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Returns the category of this identity.
     * 
     * @return the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    /**
     * Returns whether this identity has been merged and updates the internal number and the identifier.
     * 
     * @param exception the exception to be rethrown if this identity has not been merged.
     * 
     * @return whether this identity has been merged.
     */
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException;
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    /**
     * This class allows to convert an identity to its address and recover it again by downcasting the identity returned by the overridden method to the given target class.
     */
    @Immutable
    public static final class IdentifierConverter<I extends Identity> extends CastingKeyConverter<I, Object, Identifier, Object, Identity> {
        
        /**
         * Creates a new identity-identifier converter with the given target class.
         * 
         * @param targetClass the target class to which the recovered object is cast.
         */
        protected IdentifierConverter(@Nonnull Class<I> targetClass) {
            super(targetClass);
        }
        
        @Pure
        @Override
        public @Nonnull Identifier convert(@Nonnull I identity) {
            return identity.getAddress();
        }
        
        @Pure
        @Override
        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Identifier identifier) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return identifier.getIdentity();
        }
        
    }
    
    /**
     * This class allows to convert an identity to its key and recover it again by downcasting the identity returned by the overridden method to the given target class.
     */
    @Immutable
    public static final class LongConverter<I extends Identity> extends CastingNonRequestingKeyConverter<I, Object, Long, Object, Identity> {
        
        /**
         * Creates a new identity-long converter with the given target class.
         * 
         * @param targetClass the target class to which the recovered object is cast.
         */
        protected LongConverter(@Nonnull Class<I> targetClass) {
            super(targetClass);
        }
        
        @Pure
        @Override
        public @Nonnull Long convert(@Nonnull I identity) {
            return identity.getKey();
        }
        
        @Pure
        @Override
        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Long key) throws InvalidEncodingException, InternalException {
            try {
                return Mapper.getIdentity(key);
            } catch (@Nonnull DatabaseException exception) {
                throw MaskingInvalidEncodingException.get(exception);
            }
        }
        
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.IDENTITY_IDENTIFIER;
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<Identity, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(Identity.class), Identifier.XDF_CONVERTER);
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    /**
     * The column declaration for identities that registers at the mapper.
     */
    @Immutable
    public static final class Declaration extends ColumnDeclaration {
        
        /**
         * Stores whether the identities can be merged.
         */
        private final boolean mergeable;
        
        /**
         * Creates a new identity declaration with the given name.
         * 
         * @param name the name of the new identity declaration.
         * @param mergeable whether the identities can be merged.
         */
        protected Declaration(@Nonnull @Validated String name, boolean mergeable) {
            super(name, Integer64Wrapper.SQL_TYPE, Mapper.REFERENCE);
            
            this.mergeable = mergeable;
        }
        
        @Locked
        @Override
        @NonCommitting
        public void executeAfterCreation(@Nonnull Statement statement, @Nonnull Table table, @Nullable Site site, boolean unique, @Nullable @Validated String prefix) throws FailedUpdateExecutionException {
            super.executeAfterCreation(statement, table, site, unique, prefix);
            if (unique && mergeable) {
                Mapper.addReference(table.getName(site), getName(prefix), table.getDeclaration().getPrimaryKeyColumnNames().toArray());
            }
        }
        
        @Locked
        @Override
        @NonCommitting
        public void executeBeforeDeletion(@Nonnull Statement statement, @Nonnull Table table, @Nullable Site site, boolean unique, @Nullable @Validated String prefix) throws FailedUpdateExecutionException {
            super.executeBeforeDeletion(statement, table, site, unique, prefix);
            if (unique && mergeable) {
                Mapper.removeReference(table.getName(site), getName(prefix), table.getDeclaration().getPrimaryKeyColumnNames().toArray());
            }
        }
        
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("identity", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Identity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(Identity.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Identity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
