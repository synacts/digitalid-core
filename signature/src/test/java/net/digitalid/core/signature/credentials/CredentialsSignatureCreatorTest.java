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

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentBuilder;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.credential.ClientCredentialBuilder;
import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.ExposedExponentBuilder;
import net.digitalid.core.credential.utility.HashedOrSaltedAgentPermissions;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.GroupWithKnownOrder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsConverter;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class CredentialsSignatureCreatorTest extends CoreTest {
    
    @Test
    public void shouldSignAndCreateCredentialsSignature() throws ExternalException {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull HostIdentifier hostIdentifier = HostIdentifier.with("digitalid.net");
        final @Nonnull InternalNonHostIdentifier subject = InternalNonHostIdentifier.with("bob@digitalid.net");
    
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull Time timeRoundedDown = time.roundDown(Time.HALF_HOUR);
        final @Nonnull HostIdentity hostIdentity = hostIdentifier.resolve();
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(hostIdentity, time);
        // TODO: why host-identifier in this case?
        final @Nonnull PrivateKey privateKey = PrivateKeyRetriever.retrieve(hostIdentity.getAddress(), time);
        final @Nonnull GroupWithKnownOrder group = privateKey.getCompositeGroup();
        
        // to create a new client credential, we need to query it from credential internal query and pass the role and the randomized agent permissions.
        final @Nonnull ExposedExponent exposedExponent = ExposedExponentBuilder.withIssuer(subject.resolve().castTo(InternalPerson.class))
                .withIssuance(timeRoundedDown).withHashedOrSaltedPermissions(HashedOrSaltedAgentPermissions
                .with(ReadOnlyAgentPermissions.GENERAL_READ, true)).withRole(null).withAttributeContent(null).build();
        final @Nonnull Exponent secret = ExponentBuilder.withValue(BigInteger.TEN).build();
        
        // commitment created by client
        final @Nonnull SecretCommitment secretCommitment = SecretCommitmentBuilder.withHost(hostIdentity).withTime(time).withPublicKey(publicKey).withSecret(secret).build();
        final @Nonnull BigInteger clientSignatureCommitmentValue = secretCommitment.getValue();
        // parameters created by the host
        final @Nonnull Element f = group.getElement(clientSignatureCommitmentValue);
        final @Nonnull Exponent i = ExponentBuilder.withValue(BigInteger.ZERO).build();
        //final @Nonnull Exponent i = ExponentBuilder.withValue(new BigInteger(Parameters.HASH_SIZE.get(), new SecureRandom())).build();
    
        // restrictions can be provided to other services and validated through the hash that has been signed by the host,
        // which allows the client to prove that it has certain rights to access resources associated with a certain context
        final @Nonnull Restrictions restrictions = Restrictions.CAN_ASSUME_ROLES;
        final @Nonnull byte[] restrictionsHash = XDF.hash(RestrictionsConverter.INSTANCE, restrictions);
        final @Nonnull Exponent v = ExponentBuilder.withValue(new BigInteger(restrictionsHash)).build();
    
        final @Nonnull Exponent e = ExponentBuilder.withValue(BigInteger.probablePrime(Parameters.CREDENTIAL_EXPONENT.get(), new SecureRandom())).build();
    
        final @Nonnull Element c = f.multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)).multiply(publicKey.getAo().pow(exposedExponent.getHash()).inverse()).pow(e.inverse(group)).inverse();
        // c, e, v, i are parameters returned by the credential reply

        final ClientCredential clientCredential = ClientCredentialBuilder.withExposedExponent(exposedExponent).withC(c).withE(e).withU(secret).withV(v).withI(i).withRestrictions(restrictions).build();
        final @Nonnull CredentialsSignature<String> signedMessage = CredentialsSignatureCreator.sign(message, StringConverter.INSTANCE).about(subject).with(clientCredential.getRandomizedCredential());
        signedMessage.verifySignature();
    }
    
}
