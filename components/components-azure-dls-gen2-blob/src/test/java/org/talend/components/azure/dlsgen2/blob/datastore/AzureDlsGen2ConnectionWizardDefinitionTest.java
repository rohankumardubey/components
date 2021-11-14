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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.wizard.WizardImageType;

public class AzureDlsGen2ConnectionWizardDefinitionTest {

    private AzureDlsGen2ConnectionWizardDefinition azureDlsGen2ConnectionWizardDefinition;

    @Before
    public void setUp() throws Exception {
        azureDlsGen2ConnectionWizardDefinition = new AzureDlsGen2ConnectionWizardDefinition();
    }

    /**
     * @see AzureDlsGen2ConnectionWizardDefinition#isTopLevel()
     */
    @Test
    public void testIsTopLevel() {
        boolean result = azureDlsGen2ConnectionWizardDefinition.isTopLevel();
        assertTrue("result should be true", result);
    }

    /**
     * @see AzureDlsGen2ConnectionWizardDefinition#getPngImagePath(WizardImageType)
     */
    @Test
    public void testGetPngImagePath() {
        String pngimagepath = azureDlsGen2ConnectionWizardDefinition.getPngImagePath(WizardImageType.TREE_ICON_16X16);
        assertEquals("connectionWizardIcon.png", pngimagepath);
        pngimagepath = azureDlsGen2ConnectionWizardDefinition.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66);
        assertEquals("azureStorageWizardBanner.png", pngimagepath);
    }
}
