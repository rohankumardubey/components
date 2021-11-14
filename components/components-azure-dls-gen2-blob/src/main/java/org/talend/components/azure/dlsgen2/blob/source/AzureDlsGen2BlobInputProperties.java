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
package org.talend.components.azure.dlsgen2.blob.source;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2ProvideConnectionProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.daikon.properties.presentation.Form;

public class AzureDlsGen2BlobInputProperties extends FixedConnectorsComponentProperties
        implements AzureDlsGen2ProvideConnectionProperties, IOProperties<AzureDlsGen2BlobDatasetProperties> {

    public AzureDlsGen2BlobDatasetProperties dataset = new AzureDlsGen2BlobDatasetProperties("dataset");

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public AzureDlsGen2BlobInputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public AzureDlsGen2BlobDatasetProperties getDatasetProperties() {
        return dataset;
    }

    @Override
    public void setDatasetProperties(AzureDlsGen2BlobDatasetProperties datasetProperties) {
        this.dataset = datasetProperties;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    public AzureDlsGen2BlobDatastoreProperties getConnectionProperties() {
        return getDatasetProperties().getDatastoreProperties();
    }
}
