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

/**
 * This class hold and provide azure storage connection using a sas token
 */
public class AzureDlsGen2ServicesWithSas implements AzureDlsGen2Services {

    private final String accountName;

    private final String sasToken;

    public String getAccountName() {
        return accountName;
    }

    public String getSasToken() {
        return sasToken;
    }

    @Override
    public BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                .sasToken(sasToken)
                .buildClient();
    }

    private AzureDlsGen2ServicesWithSas(Builder builder) {
        this.accountName = builder.accountName;
        this.sasToken = builder.sasToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String accountName;

        private String sasToken;

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder sasToken(String sasToken) {
            this.sasToken = sasToken;
            return this;
        }

        public AzureDlsGen2ServicesWithSas build() {
            return new AzureDlsGen2ServicesWithSas(this);
        }
    }

}
