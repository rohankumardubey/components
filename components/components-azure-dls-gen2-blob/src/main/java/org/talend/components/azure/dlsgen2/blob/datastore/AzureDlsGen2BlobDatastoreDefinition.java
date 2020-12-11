// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azure.dlsgen2.blob.datastore;

import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobFamilyDefinition;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputDefinition;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class AzureDlsGen2BlobDatastoreDefinition extends I18nDefinition implements DatastoreDefinition<AzureDlsGen2BlobDatastoreProperties> {

    public static final String NAME = "AzureDlsGen2BlobDatastore";

    // DataPrep
    public static final String DATASTORE_RUNTIME_CLASS = "org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobDatastoreRuntime";

    public AzureDlsGen2BlobDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(AzureDlsGen2BlobDatastoreProperties properties) {
        return AzureDlsGen2BlobFamilyDefinition.getCommonRuntimeInfo(DATASTORE_RUNTIME_CLASS);
    }

    @Override
    public String getImagePath() {
        return getImagePath(DefinitionImageType.PALETTE_ICON_32X32);
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        default:
            return null;
        }
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public Class<AzureDlsGen2BlobDatastoreProperties> getPropertiesClass() {
        return AzureDlsGen2BlobDatastoreProperties.class;
    }

    @Override
    public DatasetProperties createDatasetProperties(AzureDlsGen2BlobDatastoreProperties storeProp) {
        AzureDlsGen2BlobDatasetProperties dataset = new AzureDlsGen2BlobDatasetProperties("dataset");
        dataset.init();
        dataset.setDatastoreProperties(storeProp);
        return dataset;
    }

    @Override
    public String getInputCompDefinitionName() {
        return AzureDlsGen2BlobInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return null;
    }

}
