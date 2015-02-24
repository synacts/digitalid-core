package net.digitalid.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.database.Database;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.wrappers.Int8Wrapper;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models a syntactic type.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class SyntacticType extends Type implements Immutable {
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType IDENTITY_IDENTIFIER = SemanticType.create("@core.digitalid.net").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code nonhost@core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType NONHOST_IDENTIFIER = SemanticType.create("nonhost@core.digitalid.net").load(IDENTITY_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code type@core.digitalid.net}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType TYPE_IDENTIFIER = SemanticType.create("type@core.digitalid.net").load(NONHOST_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("syntactic.type@core.digitalid.net").load(TYPE_IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code parameters.syntactic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.create("parameters.syntactic.type@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE}, Time.TROPICAL_YEAR, Int8Wrapper.TYPE);
    
    
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
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    SyntacticType(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Creates a new syntactic type with the given identifier.
     * 
     * @param identifier the identifier of the new syntactic type.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    public static @Nonnull SyntacticType create(@Nonnull String identifier) {
        return Mapper.mapSyntacticType(new InternalNonHostIdentifier(identifier));
    }
    
    
    @Override
    @NonCommitting
    void load() throws SQLException, IOException, PacketException, ExternalException {
        assert isNotLoaded() : "The type declaration is not loaded.";
        
        this.numberOfParameters = new Int8Wrapper(Cache.getStaleAttributeContent(this, null, PARAMETERS)).getValue();
        if (numberOfParameters < -1) throw new InvalidEncodingException("The number of parameters has to be at least -1.");
        setLoaded();
    }
    
    /**
     * Loads the type declaration from the given parameter.
     * 
     * @param numberOfParameters the number of generic parameters.
     * 
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require numberOfParameters >= -1 : "The number of parameters is at least -1.";
     * @require numberOfParameters <= 127 : "The number of parameters is at most 127.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SyntacticType load(int numberOfParameters) {
        assert isNotLoaded() : "The type declaration is not loaded.";
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
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    @Pure
    public byte getNumberOfParameters() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return numberOfParameters;
    }
    
}
