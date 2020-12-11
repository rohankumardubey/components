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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class AzureDlsGen2ConnectionEditWizardDefinitionTest {

    private AzureDlsGen2ConnectionEditWizardDefinition azureStorageConnectionEditWizardDefinition;

    @Before
    public void setUp() throws Exception {
        azureStorageConnectionEditWizardDefinition = new AzureDlsGen2ConnectionEditWizardDefinition();

    }

    /**
     * @see AzureDlsGen2ConnectionEditWizardDefinition#getName()
     */
    @Test
    public void testGetName() {
        String name = azureStorageConnectionEditWizardDefinition.getName();
        assertEquals("name cannot be null", AzureDlsGen2ConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME, name);
    }

    /**
     * @see AzureDlsGen2ConnectionEditWizardDefinition#isTopLevel()
     */
    @Test
    public void isTopLevel() {
        boolean result = azureStorageConnectionEditWizardDefinition.isTopLevel();
        assertFalse("result cannot be true", result);
    }

}
