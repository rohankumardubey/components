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

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;

import org.talend.components.azure.runtime.token.AzureActiveDirectoryTokenGetter;

public class AzureConnectionWithToken implements AzureConnection {

    private final String accountName;

    private final AzureActiveDirectoryTokenGetter tokenGetter;

    private ClientSecretCredential clientSecretCredential;

    public AzureConnectionWithToken(String accountName, String tenantId, String clientId, String clientSecret) {
        this.accountName = accountName;
        this.tokenGetter = new AzureActiveDirectoryTokenGetter(tenantId, clientId, clientSecret);
        clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
    }

    public AzureConnectionWithToken(String accountName, AzureActiveDirectoryTokenGetter tokenGetter) {
        this.accountName = accountName;
        this.tokenGetter = tokenGetter;
    }

    @Override
    public BlobServiceClient getBlobServiceClient() {
        String endpoint = "https://" + accountName + ".dfs.core.windows.net";
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(clientSecretCredential)
                .buildClient();
    }

    @Override
    public QueueServiceClient getQueueServiceClient() {
        String endpoint = "https://" + accountName + ".dfs.core.windows.net";
        return new QueueServiceClientBuilder()
                .endpoint(endpoint)
                .credential(clientSecretCredential)
                .buildClient();
    }


}
