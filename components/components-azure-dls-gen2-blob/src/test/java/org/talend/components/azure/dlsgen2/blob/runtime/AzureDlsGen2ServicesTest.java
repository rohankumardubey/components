// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azure.dlsgen2.blob.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AzureDlsGen2ServicesTest {

    @Test
    public void testBuildInstanceWithKey() {
        AzureDlsGen2ServicesWithKey instance = AzureDlsGen2ServicesWithKey.builder()//
                .accountName("talendAccount")//
                .accountKey("aValide64baseKey")//
                .build();
        assertNotNull(instance);
        assertEquals("talendAccount", instance.getAccountName());
        assertEquals("aValide64baseKey", instance.getAccountKey());
    }

    @Test
    public void testBuildInstanceWithSas() {
        AzureDlsGen2ServicesWithSas instance = AzureDlsGen2ServicesWithSas.builder()//
                .accountName("talendAccount")//
                .sasToken("aValidSASToken")//
                .build();
        assertNotNull(instance);
        assertEquals("talendAccount", instance.getAccountName());
        assertEquals("aValidSASToken", instance.getSasToken());
    }

    @Test
    public void testBuildInstanceWithToken() throws Exception {
        String testAccountName = "someAccountName";
        AzureDlsGen2ServicesWithToken instance = AzureDlsGen2ServicesWithToken.builder()
                .accountName(testAccountName)
                .tenantId("tenantId")
                .clientId("clientId")
                .clientSecret("clientSecret")
                .build();
        assertNotNull(instance);
        assertNotNull(instance.getClientSecretCredential());
        assertEquals(testAccountName, instance.getAccountName());
        assertNotNull(instance.getBlobServiceClient());
    }
}
