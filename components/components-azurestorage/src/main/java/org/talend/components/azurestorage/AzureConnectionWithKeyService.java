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
package org.talend.components.azurestorage;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;


/**
 * This class hold and provide azure storage connection using a key
 */
public class AzureConnectionWithKeyService implements AzureConnection {

    private String protocol;

    private String accountName;

    private String accountKey;

    AzureConnectionWithKeyService(Builder builder) {
        this.protocol = builder.protocol;
        this.accountName = builder.accountName;
        this.accountKey = builder.accountKey;
    }

    @Override
    public BlobServiceClient getBlobServiceClient()  {
        return  new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                .credential(new StorageSharedKeyCredential(accountName, accountKey))
                .buildClient();
    }

    @Override
    public QueueServiceClient getQueueServiceClient() {
        return new QueueServiceClientBuilder()
                .endpoint(String.format("https://%s.queue.core.windows.net", accountName))
                .credential(new StorageSharedKeyCredential(accountName, accountKey))
                .buildClient();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public static Protocol builder() {
        return new Builder();
    }

    private static class Builder implements Build, Protocol, AccountName, AccountKey {

        private String protocol;

        private String accountName;

        private String accountKey;

        public AccountName protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public AccountKey accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Build accountKey(String accountKey) {
            this.accountKey = accountKey;
            return this;
        }

        public AzureConnectionWithKeyService build() {
            return new AzureConnectionWithKeyService(this);
        }
    }

    public interface Protocol {

        public AccountName protocol(String protocol);
    }

    public interface AccountName {

        public AccountKey accountName(String accountName);
    }

    public interface AccountKey {

        public Build accountKey(String accountKey);
    }

    public interface Build {

        public AzureConnectionWithKeyService build();
    }

}
