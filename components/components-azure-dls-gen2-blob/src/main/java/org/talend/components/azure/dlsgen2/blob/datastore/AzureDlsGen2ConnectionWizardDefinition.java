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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.definition.DefinitionImageType;

public class AzureDlsGen2ConnectionWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_NAME = "azuredlsgen2";

    @Override
    public ComponentWizard createWizard(String location) {
        return new AzureDlsGen2ConnectionWizard(this, location);
    }

    @Override
    public ComponentWizard createWizard(ComponentProperties properties, String location) {
        AzureDlsGen2ConnectionWizard wizard = (AzureDlsGen2ConnectionWizard) createWizard(location);
        wizard.setupProperties((AzureDlsGen2BlobDatastoreProperties) properties);

        return wizard;
    }

    @Deprecated
    @Override
    public String getPngImagePath(WizardImageType imageType) {
        switch (imageType) {
        case TREE_ICON_16X16:
            return getImagePath(DefinitionImageType.TREE_ICON_16X16);
        case WIZARD_BANNER_75X66:
            return getImagePath(DefinitionImageType.WIZARD_BANNER_75X66);
        default:
            // will return null
        }
        return null;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case TREE_ICON_16X16:
            return "connectionWizardIcon.png"; //$NON-NLS-1$
        case WIZARD_BANNER_75X66:
            return "azureStorageWizardBanner.png"; // $NON-NLS-1$
        default:
            // will return null
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass) {
        return propertiesClass.isAssignableFrom(AzureDlsGen2BlobDatastoreProperties.class);
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

}
