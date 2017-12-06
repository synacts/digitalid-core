/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.signature.credentials;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.list.ReadOnlyListConverter;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;

/**
 * Creates a credentials signature by signing an object of generic type OBJECT.
 */
@Utility
public abstract class CredentialsSignatureCreator {
    
    public interface SubjectCredentialsSignatureCreator<OBJECT> {

        /**
         * Addresses the signature to a certain subject.
         */
        @Pure
        public @Nonnull LodgedCredentialsSignatureCreator<OBJECT> to(@Nonnull InternalIdentifier subject);

    }

    public interface LodgedCredentialsSignatureCreator<OBJECT> {

        /**
         */
        @Pure
        public @Nonnull CommitmentCredentialsSignatureCreator<OBJECT> lodged(boolean lodged);

    }

    public interface CommitmentCredentialsSignatureCreator<OBJECT> {

        /**
         * Addresses the signature to a certain subject.
         */
        @Pure
        public @Nonnull CredentialsSignature<OBJECT> with(@Nonnull ClientCredential... clientCredentials);

    }
    
    /**
     * Inner class for the credentials signature creator which structures the parameters required for signing.
     */
    public static class InnerCredentialsSignatureCreator<OBJECT> implements SubjectCredentialsSignatureCreator<OBJECT>, LodgedCredentialsSignatureCreator<OBJECT>, CommitmentCredentialsSignatureCreator<OBJECT> {

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

        @Pure
        @Override
        public @Nonnull LodgedCredentialsSignatureCreator<OBJECT> to(@Nonnull InternalIdentifier subject) {
            this.subject = subject;
            return this;
        }
        
        private boolean lodged;
        
        @Pure
        @Override
        public @Nonnull CommitmentCredentialsSignatureCreator<OBJECT> lodged(boolean lodged) {
            this.lodged = lodged;
            return this;
        }
        
        private @Nullable PublicKey publicKeyOfReceivingHost;
        
        @Pure
        public @Nonnull InnerCredentialsSignatureCreator withPublicKeyOfReceivingHost(@Nullable PublicKey publicKey) {
            this.publicKeyOfReceivingHost = publicKey;
            return this;
        }
        
        private @Nullable BigInteger b_prime;
        
        @Pure
        public @Nonnull InnerCredentialsSignatureCreator withBPipe(@Nonnull BigInteger b_pipe) {
            this.b_prime = b_pipe;
            return this;
        }
        
        private @Nullable ReadOnlyList<CertifiedAttributeValue> certificates;
        
        @Pure
        public @Nonnull InnerCredentialsSignatureCreator withCertificates(@Nullable ReadOnlyList<CertifiedAttributeValue> certificates) {
            this.certificates = certificates;
            return this;
        }
    
        /**
         * Returns the verifiable encryption of the given value m with the random value r.
         */
        @Pure
        private @Nonnull VerifiableEncryptionMessage getVerifiableEncryption(@Nonnull PublicKey publicKey, @Nonnull Exponent m, @Nonnull Exponent r) {
            return VerifiableEncryptionMessageBuilder.withElement0(publicKey.getY().pow(r).multiply(publicKey.getZPlus1().pow(m))).withElement1(publicKey.getG().pow(r)).build();
        }
    
        @Pure
        @Override
        public @Nonnull CredentialsSignature<OBJECT> with(@Nonnull ClientCredential... credentials) {
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull BigInteger contentHash = CredentialsSignature.getContentHash(time, subject, objectConverter, object);

            final @Nonnull ClientCredential mainCredential = credentials[0];
            final @Nonnull Exponent u = mainCredential.getU();
            final @Nonnull Exponent v = mainCredential.getV();
    
            final @Nonnull SecureRandom random = new SecureRandom();
            final @Nonnull Exponent ru = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
            final @Nullable Restrictions restrictions = mainCredential.getRestrictions();
            // TODO: check with Kaspar if it is correct to use the issuer of the exposed exponent
            final @Nullable Exponent rv = restrictions != null && (mainCredential.getExposedExponent().getIssuer().getAddress().equals(subject) || mainCredential.isRoleBased()) ? null : ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
    
            final int size = credentials.length;
            final @Nonnull ClientCredential[] randomizedCredentials = new ClientCredential[size];
    
            final @Nonnull Exponent[] res = new Exponent[size];
            final @Nonnull Exponent[] rbs = new Exponent[size];
            final @Nonnull Exponent[] ris = new Exponent[size];
    
            final @Nonnull Exponent[] rwis = new Exponent[size];
            final @Nonnull Exponent[] rwbs = new Exponent[size];
    
            final @Nonnull FreezableArrayList<VerifiableEncryptionMessage> wis = FreezableArrayList.withInitialCapacity(size);
            final @Nonnull FreezableArrayList<VerifiableEncryptionMessage> wbs = FreezableArrayList.withInitialCapacity(size);
    
            final @Nonnull Exponent[] rrwis = new Exponent[size];
            final @Nonnull Exponent[] rrwbs = new Exponent[size];
    
            final @Nonnull FreezableList<VerifiableEncryptionParameters> ts = FreezableArrayList.withNoElements();
            for (int i = 0; i < size; i++) {
                randomizedCredentials[i] = credentials[i].getRandomizedCredential();
                final @Nonnull PublicKey publicKey = randomizedCredentials[i].getExposedExponent().getPublicKey();
                @Nonnull Element element = publicKey.getCompositeGroup().getElement(BigInteger.ONE);

                res[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_CREDENTIAL_EXPONENT.get(), random)).build();
                rbs[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get(), random)).build();

                if (!randomizedCredentials[i].isOneTime()) {
                    ris[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_EXPONENT.get(), random)).build();
                    element = element.multiply(publicKey.getAi().pow(ris[i]));
                }

                if (rv != null) element = element.multiply(publicKey.getAv().pow(rv));

                final @Nonnull Element ao = randomizedCredentials[i].getC().pow(res[i]).multiply(publicKey.getAb().pow(rbs[i])).multiply(publicKey.getAu().pow(ru)).multiply(element);
                final @Nonnull VerifiableEncryptionParametersBuilder.InnerVerifiableEncryptionParametersBuilder verifiableEncryptionParametersBuilder = VerifiableEncryptionParametersBuilder.withAo(ao);

                if (lodged && !randomizedCredentials[i].isOneTime()) {
                    rwis[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get() - Parameters.HASH_SIZE.get(), random)).build();
                    rwbs[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get() - Parameters.HASH_SIZE.get(), random)).build();

                    wis.add(getVerifiableEncryption(publicKey, randomizedCredentials[i].getI(), rwis[i]));
                    wbs.add(getVerifiableEncryption(publicKey, randomizedCredentials[i].getB(), rwbs[i]));

                    rrwis[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get(), random)).build();
                    rrwbs[i] = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get(), random)).build();

                    verifiableEncryptionParametersBuilder.withTwi(VerifiableEncryptionExponentPairBuilder.withElement0(ris[i]).withElement1(rrwis[i]).build()).withTwb(VerifiableEncryptionExponentPairBuilder.withElement0(rbs[i]).withElement1(rrwbs[i]).build());
                }

                ts.add(i, verifiableEncryptionParametersBuilder.build());
            }
            
            @Nonnull BigInteger tf = BigInteger.ZERO;
            @Nullable Element f_prime = null;
            @Nullable Exponent rb = null;
            if (b_prime != null) {
                Require.that(publicKeyOfReceivingHost != null).orThrow("If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.");
                rb = ExponentBuilder.withValue(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT.get(), random)).build();
        
                f_prime = publicKeyOfReceivingHost.getAu().pow(ru).multiply(publicKeyOfReceivingHost.getAb().pow(rb));
                if (rv != null) f_prime = f_prime.multiply(publicKeyOfReceivingHost.getAv().pow(rv));
                tf = new BigInteger(1, XDF.hash(ElementConverter.INSTANCE, f_prime));
        
                f_prime = publicKeyOfReceivingHost.getAu().pow(u).multiply(publicKeyOfReceivingHost.getAb().pow(b_prime));
                if (rv != null) f_prime = f_prime.multiply(publicKeyOfReceivingHost.getAv().pow(v));
            }
    
            final @Nonnull Exponent t = ExponentBuilder.withValue(contentHash.xor(new BigInteger(1, XDF.hash(ReadOnlyListConverter.INSTANCE, ts.freeze()))).xor(tf)).build();
    
            final @Nonnull Exponent su = ru.subtract(t.multiply(u));
            final @Nullable Exponent sv = rv != null ? rv.subtract(t.multiply(v)): null;
            final @Nullable Restrictions restrictionsInCredentialSignature = rv != null ? null : restrictions;
    
            final @Nonnull FreezableArrayList<PublicClientCredential> publicClientCredentials = FreezableArrayList.withNoElements();
            for (int i = 0; i < size; i++) {
                final PublicClientCredentialBuilder.CPublicClientCredentialBuilder publicClientCredentialBuilderForMandatoryValues = PublicClientCredentialBuilder.withExposedExponent(randomizedCredentials[i].getExposedExponent());
                final @Nonnull Exponent se = res[i].subtract(t.multiply(randomizedCredentials[i].getE()));
                final @Nonnull Exponent sb = rbs[i].subtract(t.multiply(randomizedCredentials[i].getB()));
                final PublicClientCredentialBuilder.@Nonnull InnerPublicClientCredentialBuilder publicClientCredentialBuilderForOptionalValues = publicClientCredentialBuilderForMandatoryValues.withC(randomizedCredentials[i].getC()).withSe(se).withSb(sb);

                if (b_prime == null || mainCredential.isRoleBased()) {
                    // TODO: Remove eventually: We do not necessarily need that. In the verification process, 
                    // when a HostCredential is created, the salted agent permissions are fetched from the exposed exponent.
                    publicClientCredentialBuilderForOptionalValues.withSaltedAgentPermissions(randomizedCredentials[i].getExposedExponent().getHashedOrSaltedPermissions().getSaltedPermissions());
                }
                if (randomizedCredentials[i].isOneTime()) {
                    publicClientCredentialBuilderForOptionalValues.withI(randomizedCredentials[i].getI());
                } else {
                    final @Nonnull Exponent si = ris[i].subtract(t.multiply(randomizedCredentials[i].getI()));
                    publicClientCredentialBuilderForOptionalValues.withSi(si);
                    if (lodged) {
                        final @Nonnull Exponent swi = rrwis[i].subtract(t.multiply(rwis[i]));
                        final @Nonnull Exponent swb = rrwbs[i].subtract(t.multiply(rwbs[i]));
                        final @Nonnull VerifiableEncryption verifiableEncryption = VerifiableEncryptionBuilder.withEncryptionForSerial(wis.get(i)).withSolutionForSerial(swi).withEncryptionForBlindingValue(wbs.get(i)).withSolutionForBlindingValue(swb).build();
                        publicClientCredentialBuilderForOptionalValues.withVerifiableEncryption(verifiableEncryption);
                    }
                }
                publicClientCredentials.add(publicClientCredentialBuilderForOptionalValues.build());
            }
    
            final @Nullable Exponent sb_prime;
            
            if (b_prime != null) {
                assert f_prime != null && rb != null : "If the credential is shortened, f_prime and rb are not null (see the code above).";
                sb_prime = rb.subtract(t.multiply(ExponentBuilder.withValue(b_prime).build())); 
            } else {
                sb_prime = null;
            }
            
            return CredentialsSignatureBuilder.withObjectConverter(objectConverter).withObject(object).withSubject(subject).withT(t).withSU(su).withCredentials(publicClientCredentials.freeze()).withCertificates(certificates).withSV(sv).withSBPrime(sb_prime).withFPrime(f_prime).withRestrictions(restrictionsInCredentialSignature).withTime(time).build();
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
