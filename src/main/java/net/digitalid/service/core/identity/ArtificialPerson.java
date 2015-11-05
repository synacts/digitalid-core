package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models an artificial person.
 */
@Immutable
public final class ArtificialPerson extends InternalPerson {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code artificial.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("artificial.person@core.digitalid.net").load(InternalPerson.IDENTIFIER);
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new artificial person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    ArtificialPerson(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.ARTIFICIAL_PERSON;
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identities to this subclass.
     */
    public static final @Nonnull Caster<Identity, ArtificialPerson> CASTER = new Caster<Identity, ArtificialPerson>() {
        @Pure
        @Override
        protected @Nonnull ArtificialPerson cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity.toArtificialPerson();
        }
    };
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<ArtificialPerson, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(CASTER), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<ArtificialPerson, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter("artificial_person")); // TODO: Add GeneralReference.get("REFERENCES general_identity (identity) ON DELETE RESTRICT ON UPDATE RESTRICT")
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<ArtificialPerson, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
