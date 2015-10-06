package net.digitalid.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.LoadedRecipient;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.core.annotations.NonLoaded;
import net.digitalid.core.annotations.NonLoadedRecipient;
import net.digitalid.database.annotations.OnMainThread;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.database.configuration.Database;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.wrappers.Int8Wrapper;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models a syntactic type.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class SyntacticType extends Type {
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType IDENTITY_IDENTIFIER = SemanticType.map("@core.digitalid.net").load(StringWrapper.TYPE);
    
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
    
    /**
     * Stores the semantic type {@code syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("syntactic.type@core.digitalid.net").load(TYPE_IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code parameters.syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.syntactic.type@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE}, Time.TROPICAL_YEAR, Int8Wrapper.TYPE);
    
    
    /**
     * Stores the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @invariant !isLoaded() || numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    private byte numberOfParameters;
    
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
    
    
    @Override
    @NonCommitting
    void load() throws SQLException, IOException, PacketException, ExternalException {
        assert !isLoaded() : "The type declaration is not loaded.";
        
        this.numberOfParameters = new Int8Wrapper(Cache.getStaleAttributeContent(this, null, PARAMETERS)).getValue();
        if (numberOfParameters < -1) throw new InvalidEncodingException("The number of parameters has to be at least -1.");
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
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SYNTACTIC_TYPE;
    }
    
    
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
    
}
