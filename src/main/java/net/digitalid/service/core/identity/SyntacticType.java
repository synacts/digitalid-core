package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.block.wrappers.Int8Wrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidDeclarationException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.identity.annotations.LoadedRecipient;
import net.digitalid.service.core.identity.annotations.NonLoaded;
import net.digitalid.service.core.identity.annotations.NonLoadedRecipient;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnMainThread;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This class models a syntactic type.
 */
@Immutable
public final class SyntacticType extends Type {
    
    /* -------------------------------------------------- Semantic Type Hacks -------------------------------------------------- */
    
    // TODO: Document the reason for these hacks more precisely.
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType IDENTITY_IDENTIFIER = SemanticType.map("@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code nonhost@core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType NONHOST_IDENTIFIER = SemanticType.map("nonhost@core.digitalid.net").load(IDENTITY_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code type@core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType TYPE_IDENTIFIER = SemanticType.map("type@core.digitalid.net").load(NONHOST_IDENTIFIER);
    
    /* -------------------------------------------------- Number of Parameters -------------------------------------------------- */
    
    /**
     * Stores the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @invariant !isLoaded() || numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    private byte numberOfParameters;
    
    /**
     * Returns the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @return the number of generic parameters of this syntactic type.
     * 
     * @ensure numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    @Pure
    @LoadedRecipient
    public byte getNumberOfParameters() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return numberOfParameters;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    @NonLoaded SyntacticType(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Maps the syntactic type with the given identifier.
     * 
     * @param identifier the identifier of the syntactic type.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @OnMainThread
    public static @Nonnull @NonLoaded SyntacticType map(@Nonnull String identifier) {
        return Mapper.mapSyntacticType(new InternalNonHostIdentifier(identifier));
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code parameters.syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.syntactic.type@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE}, Time.TROPICAL_YEAR, Int8Wrapper.XDF_TYPE);
    
    @Override
    @NonCommitting
    void load() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        assert !isLoaded() : "The type declaration is not loaded.";
        
        this.numberOfParameters = Int8Wrapper.decode(Cache.getStaleAttributeContent(this, null, PARAMETERS));
        if (numberOfParameters < -1) { throw InvalidDeclarationException.get("The number of parameters has to be at least -1 but was " + numberOfParameters + ".", getAddress()); }
        setLoaded();
    }
    
    /**
     * Loads the type declaration from the given parameter.
     * 
     * @param numberOfParameters the number of generic parameters.
     * 
     * @require numberOfParameters >= -1 : "The number of parameters is at least -1.";
     * @require numberOfParameters <= 127 : "The number of parameters is at most 127.";
     */
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SyntacticType load(int numberOfParameters) {
        assert !isLoaded() : "The type declaration is not loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert numberOfParameters >= -1 : "The number of parameters is at least -1.";
        assert numberOfParameters <= 127 : "The number of parameters is at most 127.";
        
        this.numberOfParameters = (byte) numberOfParameters;
        setLoaded();
        
        return this;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SYNTACTIC_TYPE;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("syntactic.type@core.digitalid.net").load(TYPE_IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<SyntacticType, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(SyntacticType.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("syntactic_type", false);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<SyntacticType, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(SyntacticType.class), Int64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<SyntacticType, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
