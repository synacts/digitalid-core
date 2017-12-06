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
package net.digitalid.core.packet;

import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class compresses, signs and encrypts requests by hosts.
 */
@Immutable
@TODO(task = "Do we still need/want this class?", date = "2016-11-06", author = Author.KASPAR_ETTER)
public abstract class HostRequest extends Request {
    
//    /**
//     * Stores the identifier of the signing host.
//     */
//    private @Nonnull InternalIdentifier signer;
//    
//    /**
//     * Packs the given methods with the given arguments signed by the given host.
//     * 
//     * @param methods the methods of this request.
//     * @param recipient the recipient of this request.
//     * @param subject the subject of this request.
//     * @param signer the identifier of the signing host.
//     * 
//     * @require methods.isFrozen() : "The list of methods is frozen.";
//     * @require !methods.isEmpty() : "The list of methods is not empty.";
//     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
//     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
//     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
//     */
//    @NonCommitting
//    public HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer) throws ExternalException {
//        this(methods, recipient, subject, signer, 0);
//    }
//    
//    /**
//     * Packs the given methods with the given arguments signed by the given host.
//     * 
//     * @param methods the methods of this request.
//     * @param recipient the recipient of this request.
//     * @param subject the subject of this request.
//     * @param signer the identifier of the signing host.
//     * @param iteration how many times this request was resent.
//     */
//    @NonCommitting
//    private HostRequest(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, @Nonnull InternalIdentifier signer, int iteration) throws ExternalException {
//        super(methods, recipient, getSymmetricKey(recipient, Time.TROPICAL_YEAR), subject, null, signer, iteration);
//    }
//    
//    
//    @Override
//    @RawRecipient
//    void setField(@Nullable Object field) {
//        Require.that(field != null).orThrow("See the constructor above.");
//        this.signer = (InternalIdentifier) field;
//    }
//    
//    @Pure
//    @Override
//    @RawRecipient
//    @Nonnull HostSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
//        return HostSignatureWrapper.sign(Packet.SIGNATURE, compression, subject, audit, signer);
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean isSigned() {
//        return true;
//    }
//    
//    @Override
//    @NonCommitting
//    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws ExternalException {
//        return new HostRequest(methods, recipient, subject, signer).send(verified);
//    }
    
}
