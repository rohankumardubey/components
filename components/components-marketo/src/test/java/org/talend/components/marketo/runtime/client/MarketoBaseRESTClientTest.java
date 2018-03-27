//  ============================================================================
//
//  Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.components.marketo.runtime.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;

public class MarketoBaseRESTClientTest {

    MarketoRESTClient client;

    @Before
    public void setUp() throws Exception {
        TMarketoConnectionProperties conn = new TMarketoConnectionProperties("test");
        conn.apiMode.setValue(APIMode.REST);
        conn.endpoint.setValue("https://fake.io");
        conn.clientAccessId.setValue("client");
        conn.secretKey.setValue("sekret");
        client = new MarketoRESTClient(conn);
    }

    @Test
    public void testIsAccessTokenExpired() throws Exception {
        assertFalse(client.isAccessTokenExpired(null));
        MarketoError error = new MarketoException("REST", "602", "Access token expired").toMarketoError();
        assertTrue(client.isAccessTokenExpired(Arrays.asList(error)));
    }

    @Test
    public void testIsErrorRecoverable() throws Exception {
        MarketoError error = new MarketoException("REST", "602", "Access token expired").toMarketoError();
        assertTrue(client.isErrorRecoverable(Arrays.asList(error)));
        for (String code : new String[] { "502", "604", "606", "608", "611", "614", "615" }) {
            error = new MarketoException("REST", code, "API Temporarily Unavailable").toMarketoError();
            assertTrue(client.isErrorRecoverable(Arrays.asList(error)));
        }
        error = new MarketoException("REST", "404", "Page not found").toMarketoError();
        assertFalse(client.isErrorRecoverable(Arrays.asList(error)));
    }

}
