//==============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================

package org.talend.components.azurestorage;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.azure.runtime.token.AzureActiveDirectoryTokenGetter;

import com.azure.storage.blob.models.StorageAccountInfo;

public class AzureConnectionWithTokenTest {


    @Test
    public void testCreateConnectionWithToken() throws Exception {
        String testAccountName = "someAccountName";
        AzureActiveDirectoryTokenGetter mockedTokenGetter = Mockito.mock(AzureActiveDirectoryTokenGetter.class);
        Mockito.when(mockedTokenGetter.retrieveAccessToken()).thenReturn("testToken");
        AzureConnectionWithToken sutTokenConnection = new AzureConnectionWithToken(testAccountName, mockedTokenGetter);


       // StorageAccountInfo account = sutTokenConnection.getCloudStorageAccount();

        Mockito.verify(mockedTokenGetter).retrieveAccessToken();
      //  Assert.assertEquals(testAccountName, account.getCredentials().getAccountName());
    }
}