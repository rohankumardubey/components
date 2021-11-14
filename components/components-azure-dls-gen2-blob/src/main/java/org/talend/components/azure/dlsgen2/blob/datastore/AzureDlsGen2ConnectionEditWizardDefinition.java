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

public class AzureDlsGen2ConnectionEditWizardDefinition extends AzureDlsGen2ConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "azuredlsgen2.edit";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
