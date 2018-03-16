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
package net.digitalid.core.setup;

import java.io.File;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.jdbc.JDBCDatabaseBuilder;

import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.host.Host;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;
import net.digitalid.core.signature.host.HostSignature;
import net.digitalid.core.signature.host.HostSignatureCreator;

import org.h2.Driver;

/**
 * The setup creates the root host with its keys so that the self-signed certificate can be added to the library as a trust anchor.
 */
@Utility
public abstract class Setup {
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    /**
     * Initializes the database in memory because persistence is not necessary.
     */
    @PureWithSideEffects
    @Initialize(target = Database.class)
    public static void initializeDatabase() throws SQLException {
        if (!Database.instance.isSet()) {
            final @Nonnull String URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS " + Unit.DEFAULT.getName() + ";MODE=MySQL;";
            Database.instance.set(JDBCDatabaseBuilder.withDriver(new Driver()).withURL(URL).withUser("sa").withPassword("sa").build());
        }
    }
    
    /* -------------------------------------------------- Main Method -------------------------------------------------- */
    
    /**
     * The main method starts the setup (without using any arguments).
     */
    @Impure
    @Committing
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(@Nonnull String[] arguments) throws FileException, ConversionException {
        Configuration.initializeAllConfigurations();
        Database.commit();
        
        System.out.println(Strings.format("Creating the host $, which can take several minutes.", HostIdentifier.DIGITALID));
        final @Nonnull Host host = HostBuilder.withIdentifier(HostIdentifier.DIGITALID).build();
        final @Nonnull HostSignature<Pack> hostSignature = HostSignatureCreator.sign(host.publicKeyChain.get().pack(), PackConverter.INSTANCE).about(HostIdentifier.DIGITALID).as(PublicKeyChain.TYPE.getAddress());
        final @Nonnull CertifiedAttributeValue value = CertifiedAttributeValue.with(hostSignature);
        final @Nonnull File certificateFile = Files.relativeToConfigurationDirectory("core.digitalid.net.certificate.xdf");
        value.pack().storeTo(certificateFile);
        System.out.println(Strings.format("Stored the self-signed certificate at $.", certificateFile));
    }
    
}
