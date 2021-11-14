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

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;


/**
 * This class hold and provide azure storage connection using a key
 */
public class AzureDlsGen2ServicesWithKey implements AzureDlsGen2Services {

    private String accountName;

    private String accountKey;

    AzureDlsGen2ServicesWithKey(Builder builder) {
        this.accountName = builder.accountName;
        this.accountKey = builder.accountKey;
    }

    @Override
    public BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                .credential(new StorageSharedKeyCredential(accountName, accountKey))
                .buildClient();
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String accountName;

        private String accountKey;

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder accountKey(String accountKey) {
            this.accountKey = accountKey;
            return this;
        }

        public AzureDlsGen2ServicesWithKey build() {
            return new AzureDlsGen2ServicesWithKey(this);
        }
    }
}
