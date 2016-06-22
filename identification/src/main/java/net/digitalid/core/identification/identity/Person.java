package net.digitalid.core.identification.identity;

import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 */
@Mutable
@GenerateConverter
public abstract class Person extends RelocatableIdentity {
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    // TODO: Remove the following code after settling on a merging strategy.
    
//    @Override
//    @NonCommitting
//    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
//        final @Nullable InternalNonHostIdentifier successor = Successor.get(getAddress());
//        if (successor != null && successor.isMapped()) {
//            final @Nonnull InternalNonHostIdentity person = successor.getMappedIdentity();
//            setAddress(person.getAddress());
//            setKey(person.getKey());
//            return true;
//        } else {
//            Mapper.unmap(this);
//            throw exception;
//        }
//    }
    
}
