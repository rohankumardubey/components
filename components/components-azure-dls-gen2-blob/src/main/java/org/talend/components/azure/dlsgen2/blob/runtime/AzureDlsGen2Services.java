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

import java.util.HashMap;
import java.util.Map;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;

public interface AzureDlsGen2Services {

    BlobServiceClient getBlobServiceClient();

    static String getMessage(BlobStorageException e) {
        Map<String, String> details = new HashMap<>();
        if (Map.class.isInstance(e.getValue())) {
            details = Map.class.cast(e.getValue());
        }
        return String.format("[%d %s] %s.", e.getStatusCode(), details.getOrDefault("Code", ""), details
                .getOrDefault("Message", e.getMessage()));
    }

    /**
     * correct the field name and make it valid for AVRO schema
     * for example :
     * input : "CA HT", output "CA_HT"
     * input : "column?!^Name", output "column___Name"
     * input : "P1_Vente_Qté", output "P1_Vente_Qt_"
     *
     * @param name : the name will be correct
     * @return the valid name, if the input name is null or empty, or the previousNames is null, return the input name directly
     */
    static String normalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9') && (i != 0))) {
                normalized.append(c);
            } else if (c == '_') {
                normalized.append(c);
            } else {
                normalized.append('_');
            }
        }
        return normalized.toString();
    }
}
