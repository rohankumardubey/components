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
package org.talend.components.azure.dlsgen2.blob.datastore;

import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

public class AzureDlsGen2ConnectionWizard extends ComponentWizard {

    AzureDlsGen2BlobDatastoreProperties cProperties;

    public AzureDlsGen2ConnectionWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        super(definition, repositoryLocation);

        cProperties = new AzureDlsGen2BlobDatastoreProperties("connection");
        cProperties.init();
        cProperties.setRepositoryLocation(repositoryLocation);
        addForm(cProperties.getForm(AzureDlsGen2BlobDatastoreProperties.FORM_WIZARD));
    }

    public void setupProperties(AzureDlsGen2BlobDatastoreProperties properties) {
        cProperties.setupProperties();
        cProperties.copyValuesFrom(properties);
    }

}
