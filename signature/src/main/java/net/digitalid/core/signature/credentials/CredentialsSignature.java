package net.digitalid.core.signature.credentials;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.list.ReadOnlyListConverter;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.HostCredential;
import net.digitalid.core.credential.HostCredentialBuilder;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsConverter;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.signature.exceptions.ExpiredSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * 
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class CredentialsSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    public abstract @Nonnull Exponent getT();
    
    @Pure
    public abstract @Nonnull Exponent getSU();
    
    @Pure
    public abstract @Nonnull ReadOnlyList<@Nonnull PublicClientCredential> getCredentials();
    
    @Pure
    public abstract @Nullable ReadOnlyList<@Nonnull CertifiedAttributeValue> getCertificates();
    
    @Pure
    public abstract @Nullable Restrictions getRestrictions();
    
    @Pure
    public abstract @Nullable Exponent getSV();
    
    @Pure
    public abstract @Nullable Element getFPrime();
    
    @Pure
    public abstract @Nullable Exponent getSBPrime();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    // TODO: time is required for credential signature validation
    /**
     */
    @Pure
    @Override
    public void verifySignature() throws InvalidSignatureException, ExpiredSignatureException, RecoveryException {
//        assert !isVerified() : "This signature is not verified.";
        
        final @Nonnull Time start = TimeBuilder.build();
        
        checkExpiration();
        
        final @Nonnull BigInteger hash = getContentHash(getTime(), getSubject(), getObjectConverter(), getObject());
        
        if (getSU().getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
            // TODO: the invalid signature exception should take a message, so that the caller knows why the signature verification failed. In this case, it is: "The value su is too big."
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
        
        @Nullable Exponent v = null, sv = null;
        if (getSV() != null && getSV().getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
            // TODO: The value sv is too big.
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        } else {
            Require.that(getRestrictions() != null).orThrow("If SV is null, restrictions must be null");
            // TODO: check if BigInteger generation is correct
            v = ExponentBuilder.withValue(new BigInteger(XDF.hash(RestrictionsConverter.INSTANCE, getRestrictions()))).build();
        }
        
        // credentials
        final @Nonnull ReadOnlyList<@Nonnull PublicClientCredential> publicClientCredentials = getCredentials();
        final @Nonnull FreezableArrayList<@Nonnull HostCredential> hostCredentials = FreezableArrayList.withInitialCapacity(publicClientCredentials.size());
        for (@Nonnull PublicClientCredential publicClientCredential : publicClientCredentials) {
            final @Nonnull HostCredential hostCredential = HostCredentialBuilder.withExposedExponent(publicClientCredential.getExposedExponent()).withI(publicClientCredential.getI()).build();
            hostCredentials.add(hostCredential);
        }
        final @Nonnull FreezableList<VerifiableEncryptionVerificationParameters> verifiableEncryptionVerificationParametersList = FreezableArrayList.withInitialCapacity(publicClientCredentials.size());
        for (int i = 0; i < publicClientCredentials.size(); i++) {
            final @Nonnull PublicKey publicKey = hostCredentials.get(i).getExposedExponent().getPublicKey();
            final @Nonnull Exponent o = hostCredentials.get(i).getO();
            final @Nonnull PublicClientCredential publicClientCredential = publicClientCredentials.get(i);
            // final @Nonnull Element c = publicKey.getCompositeGroup().getElement(publicClientCredential.getC());
            // TODO: check with Kaspar if this c is in the correct group.
            final @Nonnull Element c = publicClientCredential.getC();
            
            final @Nonnull Exponent se = publicClientCredential.getSe();
            if (se.getBitLength() > Parameters.RANDOM_CREDENTIAL_EXPONENT.get()) {
                // TODO: set message: "The credentials signature is invalid: The value se is too big."
                throw InvalidSignatureExceptionBuilder.withSignature(this).build();
            }
            final @Nonnull Exponent sb = publicClientCredential.getSb();
            if (sb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT.get() + 1) {
                // TODO: "The credentials signature is invalid: The value sb is too big."
                throw InvalidSignatureExceptionBuilder.withSignature(this).build();
            }
            
            @Nonnull Element hiddenElement = c.pow(se).multiply(publicKey.getAb().pow(sb)).multiply(publicKey.getAu().pow(getSU()));
            @Nonnull Element shownElement = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            @Nullable Exponent si = null;
            if (publicClientCredential.getI() == null) {
                si = publicClientCredential.getSi();
                assert si != null : "If I is null, SI must not be null.";
                if (si.getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
                    // TODO: "The credentials signature is invalid: The value si is too big."
                    throw InvalidSignatureExceptionBuilder.withSignature(this).build();
                }
                hiddenElement = hiddenElement.multiply(publicKey.getAi().pow(si));
            } else {
                shownElement = publicKey.getAi().pow(publicClientCredential.getI());
            }
            
            if (v == null) {
                assert sv != null : "The value sv cannot be null if v is null (see code above).";
                hiddenElement = hiddenElement.multiply(publicKey.getAv().pow(sv));
            } else {
                shownElement = shownElement.multiply(publicKey.getAv().pow(v));
            }
            
            shownElement = shownElement.inverse().multiply(publicKey.getAo().pow(o));
            
            final @Nonnull VerifiableEncryptionVerificationParametersBuilder.InnerVerifiableEncryptionVerificationParametersBuilder verifiableEncryptionParametersBuilder = VerifiableEncryptionVerificationParametersBuilder.withVerificationElement(hiddenElement.multiply(shownElement.pow(getT())));
            
            if (publicClientCredential.getVerifiableEncryption() != null && si != null) {
                final @Nonnull VerifiableEncryption verifiableEncryption = publicClientCredential.getVerifiableEncryption();
                @Nonnull final VerifiableEncryptionMessage wis = verifiableEncryption.getEncryptionForSerial();
                final @Nonnull Exponent swi = verifiableEncryption.getSolutionForSerial();
                if (swi.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT.get()) {
                    // TODO: "The credentials signature is invalid: The value swi is too big."
                    throw InvalidSignatureExceptionBuilder.withSignature(this).build();
                }
                final @Nonnull VerifiableEncryptionMessage wbs = verifiableEncryption.getEncryptionForBlindingValue();
                final @Nonnull Exponent swb = verifiableEncryption.getSolutionForBlindingValue();
                if (swb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT.get()) {
                    // TODO: "The credentials signature is invalid: The value swb is too big.");
                    throw InvalidSignatureExceptionBuilder.withSignature(this).build();
                }
                
                final @Nonnull Element wis1 = publicKey.getY().pow(swi).multiply(publicKey.getZPlus1().pow(si)).multiply(publicKey.getSquareGroup().getElement(wis.getElement0().getValue()).pow(getT()));
                final @Nonnull Element wis2 = publicKey.getG().pow(swi).multiply(publicKey.getSquareGroup().getElement(wis.getElement1().getValue()).pow(getT()));
                
                final @Nonnull Element wbs1 = publicKey.getY().pow(swb).multiply(publicKey.getZPlus1().pow(sb)).multiply(publicKey.getSquareGroup().getElement(wbs.getElement0().getValue()).pow(getT()));
                final @Nonnull Element wbs2 = publicKey.getG().pow(swb).multiply(publicKey.getSquareGroup().getElement(wbs.getElement1().getValue())).pow(getT());
                
                verifiableEncryptionParametersBuilder.withVerificationForBlindingValue(VerifiableEncryptionElementPairBuilder.withElement0(wbs1).withElement1(wbs2).build()).withVerificationForSerial(VerifiableEncryptionElementPairBuilder.withElement0(wis1).withElement1(wis2).build());
            }
            
            verifiableEncryptionVerificationParametersList.add(verifiableEncryptionParametersBuilder.build());
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        if (getFPrime() != null) {
            final @Nonnull PublicKey publicKey;
            try {
                publicKey = PublicKeyRetriever.retrieve(getSubject().getHostIdentifier(), getTime());
                
            } catch (@Nonnull ExternalException exception) {
                throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the public key of $.", getSubject().getHostIdentifier())).withCause(exception).build();
            }
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            final @Nonnull Exponent sb = getSBPrime();
            
            @Nonnull Element element = publicKey.getAu().pow(getSU()).multiply(publicKey.getAb().pow(sb));
            if (sv != null) element = element.multiply(publicKey.getAv().pow(sv));
            final @Nonnull Element tfBeforeHash = publicKey.getCompositeGroup().getElement(getFPrime().getValue()).pow(getT()).multiply(element);
            tf = new BigInteger(XDF.hash(ElementConverter.INSTANCE, tfBeforeHash));
        }
        final @Nonnull BigInteger hashOfVerificationParameters = new BigInteger(1, XDF.hash(ReadOnlyListConverter.INSTANCE, verifiableEncryptionVerificationParametersList.freeze()));
        
        if (!getT().getValue().equals(hash.xor(hashOfVerificationParameters).xor(tf))) {
            // The credentials signature is invalid: The value t is not correct.
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
        
        if (getCertificates() != null) {
            for (final @Nonnull CertifiedAttributeValue certificate : getCertificates()) {
                try {
                    certificate.verify();
                    certificate.checkIsValid(getTime());
                } catch (ExternalException e) {
                    throw InvalidSignatureExceptionBuilder.withSignature(this).build();
                }
            }
        }
        
        Log.verbose("Signature verified in " + start.ago().getValue() + " ms.");
        
//        setVerified();
    }
    
}
