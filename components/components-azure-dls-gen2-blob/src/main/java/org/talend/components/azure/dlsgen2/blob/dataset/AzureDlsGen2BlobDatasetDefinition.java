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
package org.talend.components.azure.dlsgen2.blob.dataset;

import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobFamilyDefinition;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class AzureDlsGen2BlobDatasetDefinition extends I18nDefinition implements DatasetDefinition<AzureDlsGen2BlobDatasetProperties> {

    public static final String NAME = "AzureDlsGen2BlobDataset";

    // DataPrep
    public static final String DATASET_RUNTIME_CLASS = "org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobDatasetRuntime";

    public AzureDlsGen2BlobDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(AzureDlsGen2BlobDatasetProperties properties) {
        return AzureDlsGen2BlobFamilyDefinition.getCommonRuntimeInfo(DATASET_RUNTIME_CLASS);
    }

    @Override
    public Class<AzureDlsGen2BlobDatasetProperties> getPropertiesClass() {
        return AzureDlsGen2BlobDatasetProperties.class;
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

}
