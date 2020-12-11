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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobFamilyDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class AzureDlsGen2BlobInputDefinition extends AbstractComponentDefinition {

    public static final String NAME = "AzureDlsGen2BlobInput";

    // DataPrep
    public static final String SOURCE_RUNTIME_CLASS = "org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobSource";

    public AzureDlsGen2BlobInputDefinition() {
        super(NAME, ExecutionEngine.DI, ExecutionEngine.BEAM);
        setupI18N(new Property<?>[]{ RETURN_ERROR_MESSAGE_PROP });
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return AzureDlsGen2BlobInputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology topology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(topology);
        return AzureDlsGen2BlobFamilyDefinition.getCommonRuntimeInfo(SOURCE_RUNTIME_CLASS);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public List<String> getSupportedProducts() {
        return Collections.singletonList(SupportedProduct.DATAPREP);
    }


    @Override
    public String[] getFamilies() {
        return new String[]{ "Cloud/Azure Storage/Blob DLS Gen2" }; //$NON-NLS-1$
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[]{ RETURN_ERROR_MESSAGE_PROP };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[]{ AzureDlsGen2BlobDatastoreProperties.class };
    }

    @Override
    public boolean isStartable() {
        return true;
    }


}
