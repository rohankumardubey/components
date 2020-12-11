/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package org.talend.components.azure.dlsgen2.blob.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties.AuthType;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class AzureDlsGen2BlobDatastorePropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private AzureDlsGen2BlobDatastoreProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new AzureDlsGen2BlobDatastoreProperties("test");
        properties.init();
        properties.setupProperties();
        properties.setupLayout();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    @Test
    public void testLabelProperties() {
        Locale.setDefault(new Locale("en", "US"));
        final I18nMessages messages = GlobalI18N
                .getI18nMessageProvider().getI18nMessages(AzureDlsGen2BlobDatastoreProperties.class);
        assertEquals("Authentication type", messages.getMessage(properties.authenticationType.getDisplayName()));
        assertEquals("Account Name", messages.getMessage(properties.accountName.getDisplayName()));
        assertEquals("Account Key", messages.getMessage(properties.accountKey.getDisplayName()));
        assertEquals("Tenant ID", messages.getMessage(properties.tenantId.getDisplayName()));
        assertEquals("Client ID", messages.getMessage(properties.clientId.getDisplayName()));
        assertEquals("Client Secret", messages.getMessage(properties.clientSecret.getDisplayName()));
        assertEquals("Name", messages.getMessage(properties.name.getDisplayName()));
        assertEquals("Azure Shared Access Signature", messages.getMessage(properties.sharedAccessSignature.getDisplayName()));
    }

    /**
     * Test method for
     * {@link AzureDlsGen2BlobDatastoreProperties#setupProperties()}.
     */
    @Test
    public final void testSetupProperties() {
        assertEquals(properties.authenticationType.getValue(), AuthType.SHAREDKEY);
        assertEquals("", properties.accountName.getValue());
        assertEquals("", properties.accountKey.getValue());
    }

    @Test
    public final void testSetupLayout() {
        properties.afterUseSharedAccessSignature();
        properties.afterReferencedComponent();
    }

    @Test
    public void testAfterWizardFinish() throws Exception {
        properties.accountName.setValue("talendrd");
        properties.accountKey.setValue("key");
        ValidationResult vr = properties.afterFormFinishWizard(null);
        assertEquals(Result.OK, vr.getStatus());
    }

    @Test
    public final void testvalidateTestConnection() throws Exception {
        properties.accountName.setValue("talendrd");
        properties.authenticationType.setValue(AuthType.SAS);
        properties.sharedAccessSignature
                .setValue("https://talendrd.blob.core.windows.net/?sv=2016-05-31&ss=f&srt=sco&sp=rwdlacup&se=2017-06-07T23:50:05Z&st=2017-05-24T15:50:05Z&spr=https&sig=fakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSAS");
        assertEquals(ValidationResult.Result.OK, properties.validateTestConnection().getStatus());
    }

    @Test
    public final void testGetReferencedConnectionProperties() {
        assertNull(properties.getReferencedComponentId());
        assertNull(properties.getReferencedConnectionProperties());
    }

    @Test
    public void testRefreshLayoutAfterChangeAuthTypeToActiveDirectory() {
        properties.authenticationType.setValue(AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL);
        properties.afterAuthenticationType();

        Form mainForm = properties.getForm(Form.MAIN);

        assertTrue(mainForm.getWidget(properties.accountName).isVisible());
        assertTrue(mainForm.getWidget(properties.tenantId).isVisible());
        assertTrue(mainForm.getWidget(properties.clientId).isVisible());
        assertTrue(mainForm.getWidget(properties.clientSecret).isVisible());
        assertTrue(mainForm.getWidget(properties.accountKey).isHidden());
        assertTrue(mainForm.getWidget(properties.sharedAccessSignature).isHidden());
    }

    @Test
    public void testRefreshLayoutAfterChangeAuthTypeTwice() {
        properties.authenticationType.setValue(AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL);
        properties.afterAuthenticationType();
        properties.authenticationType.setValue(AuthType.SHAREDKEY);
        properties.afterAuthenticationType();

        Form mainForm = properties.getForm(Form.MAIN);

        assertTrue(mainForm.getWidget(properties.accountName).isVisible());
        assertTrue(mainForm.getWidget(properties.tenantId).isHidden());
        assertTrue(mainForm.getWidget(properties.clientId).isHidden());
        assertTrue(mainForm.getWidget(properties.clientSecret).isHidden());
        assertTrue(mainForm.getWidget(properties.accountKey).isVisible());
        assertTrue(mainForm.getWidget(properties.sharedAccessSignature).isHidden());
    }

}