package net.digitalid.core.signature.credentials;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.parameters.Parameters;

/**
 * Creates a credentials signature by signing an object of generic type OBJECT.
 */
@Utility
public abstract class CredentialsSignatureCreator {
    
    public interface SubjectCredentialsSignatureCreator<OBJECT> {

        /**
         * Addresses the signature to a certain subject.
         */
        public @Nonnull InnerCredentialsSignatureCreator<OBJECT> to(@Nonnull InternalIdentifier subject);

    }

    public interface CommitmentCredentialsSignatureCreator<OBJECT> {

        /**
         * Addresses the signature to a certain subject.
         */
        public @Nonnull CredentialsSignature<OBJECT> with(@Nonnull ClientCredential... clientCredentials);

    }
    
    /**
     * Inner class for the credentials signature creator which structures the parameters required for signing.
     */
    public static class InnerCredentialsSignatureCreator<OBJECT> implements SubjectCredentialsSignatureCreator<OBJECT>, CommitmentCredentialsSignatureCreator<OBJECT> {

        private final @Nonnull OBJECT object;

        private final @Nonnull Converter<OBJECT, Void> objectConverter;

        /**
         * Creates a new InnerCredentialsSignatureSigner.
         */
        private InnerCredentialsSignatureCreator(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
            this.object = object;
            this.objectConverter = objectConverter;
        }

        private @Nonnull InternalIdentifier subject;

        @Override
        public @Nonnull CredentialsSignatureCreator.InnerCredentialsSignatureCreator to(@Nonnull InternalIdentifier subject) {
            this.subject = subject;
            return this;
        }

//        @Override
        public @Nonnull CredentialsSignature<OBJECT> with(@Nonnull ClientCredential... credentials) {
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull BigInteger hash = CredentialsSignature.getContentHash(time, subject, objectConverter, object);

            final @Nonnull ClientCredential mainCredential = credentials[0];
            final @Nonnull Exponent u = mainCredential.getU();
            final @Nonnull Exponent v = mainCredential.getV();
    
            final @Nonnull SecureRandom random = new SecureRandom();
            final @Nonnull Exponent ru = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
            // TODO: remove comment
//            final @Nullable Exponent rv = mainCredential.getRestrictions() != null && (mainCredential.getIssuer().getAddress().equals(subject) || mainCredential.isRoleBased()) ? null : ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
    
            final int size = credentials.length;
            final @Nonnull ClientCredential[] randomizedCredentials = new ClientCredential[size];
    
            final @Nonnull Exponent[] res = new Exponent[size];
            final @Nonnull Exponent[] rbs = new Exponent[size];
            final @Nonnull Exponent[] ris = new Exponent[size];
    
            final @Nonnull Exponent[] rwis = new Exponent[size];
            final @Nonnull Exponent[] rwbs = new Exponent[size];
    
            final @Nonnull VerifiableEncryptionParameters[] verifiableParameters;
//            final @Nonnull Block[] wis = new Block[size];
//            final @Nonnull Block[] wbs = new Block[size];
    
            final @Nonnull Exponent[] rrwis = new Exponent[size];
            final @Nonnull Exponent[] rrwbs = new Exponent[size];
    
            final @Nonnull FreezableList<VerifiableEncryptionParameters> ts = FreezableArrayList.withNoElements();
            // TODO: implement
//            for (int i = 0; i < size; i++) {
//                randomizedCredentials[i] = credentials[i].getRandomizedCredential();
//                final @Nonnull PublicKey publicKey = randomizedCredentials[i].getPublicKey();
//                @Nonnull Element element = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
//        
//                res[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_CREDENTIAL_EXPONENT.get(), random)).build();
//                rbs[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get(), random)).build();
//        
//                if (!randomizedCredentials[i].isOneTime()) {
//                    ris[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
//                    element = element.multiply(publicKey.getAi().pow(ris[i]));
//                }
//        
//                if (rv != null) element = element.multiply(publicKey.getAv().pow(rv));
//        
//                final @Nonnull FreezableArray<Block> array = new FreezableArray<>(3);
//                array.set(0, randomizedCredentials[i].getC().pow(res[i]).multiply(publicKey.getAb().pow(rbs[i])).multiply(publicKey.getAu().pow(ru)).multiply(element).toBlock());
//        
//                if (lodged && !randomizedCredentials[i].isOneTime()) {
//                    rwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
//                    rwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
//            
//                    wis[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getI(), rwis[i]).setType(WI);
//                    wbs[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getB(), rwbs[i]).setType(WB);
//            
//                    rrwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
//                    rrwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
//            
//                    array.set(1, publicKey.getVerifiableEncryption(ris[i], rrwis[i]).setType(TWI));
//                    array.set(2, publicKey.getVerifiableEncryption(rbs[i], rrwbs[i]).setType(TWB));
//                }
//        
//                ts.add(i, VerifiableEncryptionParametersBuilder.withWi(Pair.of(ris[i], rrwis[i])).withWb(rbs[i], rrwbs[i])).build();
//            }
        
        
            
//            return CredentialsSignatureBuilder.withObject(object).withSubject(subject).withT(t).withSU(su).withSV(sv).withS(s).withTime(time).build();
            return null;
        }

    }

    /**
     * Initializes the signing of a given object with a host signature.
     */
    @Pure
    public static <OBJECT> @Nonnull SubjectCredentialsSignatureCreator<OBJECT> sign(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
        return new InnerCredentialsSignatureCreator<>(object, objectConverter);
    }
    
}
