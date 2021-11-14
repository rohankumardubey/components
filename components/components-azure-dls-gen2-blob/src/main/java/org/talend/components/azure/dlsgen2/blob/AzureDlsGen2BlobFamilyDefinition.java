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
package org.talend.components.azure.dlsgen2.blob;

import com.google.auto.service.AutoService;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2ConnectionEditWizardDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2ConnectionWizardDefinition;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Install all of the definitions provided for the Azure DLS Gen2 family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + AzureDlsGen2BlobFamilyDefinition.NAME, service = ComponentInstaller.class)
public class AzureDlsGen2BlobFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "AzureDLSGen2"; //$NON-NLS-1$

    public static final String MAVEN_URL = "mvn:org.talend.components/components-azure-dls-gen2-blob";

    public static final String MAVEN_ARTIFACT_ID = "components-azure-dls-gen2-blob";

    public static final String MAVEN_GROUP_ID = "org.talend.components";


    public AzureDlsGen2BlobFamilyDefinition() {
        super(NAME,
              // wizards
              new AzureDlsGen2ConnectionWizardDefinition(),
              new AzureDlsGen2ConnectionEditWizardDefinition(),
              // dataprep
              new AzureDlsGen2BlobDatastoreDefinition(),
              new AzureDlsGen2BlobDatasetDefinition(),
              new AzureDlsGen2BlobInputDefinition()
        );
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

    /**
     * getCommonRuntimeInfo.
     *
     * @param classLoader {@link ClassLoader} class loader
     * @param clazz       {@link SourceOrSink} clazz
     *
     * @return {@link RuntimeInfo} runtime info
     */
    public static RuntimeInfo getCommonRuntimeInfo(ClassLoader classLoader, Class<? extends RuntimableRuntime<?>> clazz) {
        return new SimpleRuntimeInfo(classLoader,
                                     DependenciesReader
                                             .computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), clazz
                                             .getCanonicalName());
    }

    public static RuntimeInfo getCommonRuntimeInfo(String clazzFullName) {
        return new JarRuntimeInfo(MAVEN_URL, DependenciesReader
                .computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), clazzFullName);
    }


}