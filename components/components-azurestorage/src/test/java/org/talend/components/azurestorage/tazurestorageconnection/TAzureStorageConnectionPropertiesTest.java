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
package org.talend.components.azurestorage.tazurestorageconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class TAzureStorageConnectionPropertiesTest {

    TAzureStorageConnectionProperties props;

    @Before
    public void setUp() throws Exception {
        props = new TAzureStorageConnectionProperties("test");
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties#setupProperties()}.
     */
    @Test
    public final void testSetupProperties() {
        props.setupProperties();
        assertTrue(props.protocol.getValue().equals(Protocol.HTTPS));
        assertTrue(props.accountName.getValue().equals(""));
        assertTrue(props.accountKey.getValue().equals(""));
        assertTrue(props.useSharedAccessSignature.getValue().equals(false));
    }

    @Test
    public final void testSetupLayout() {
        props.setupLayout();
        props.afterUseSharedAccessSignature();
        props.afterReferencedComponent();
    }

    @Test
    public void testAfterWizardFinish() throws Exception {
        props.setupProperties();
        ValidationResult vr = props.afterFormFinishWizard(null);
        assertEquals(Result.ERROR, vr.getStatus());

    }

    @Test
    public final void testGetReferencedConnectionProperties() {
        assertNull(props.getReferencedComponentId());
        assertNull(props.getReferencedConnectionProperties());
        // props.referencedComponent.componentProperties = props;
        // props.referencedComponent.
        // assertNotNull(props.getReferencedComponentId());
        // assertNotNull(props.getReferencedConnectionProperties());

    }

    @Test
    public void testEnums() {
        assertEquals(Protocol.HTTP, Protocol.valueOf("HTTP"));
        assertEquals(Protocol.HTTPS, Protocol.valueOf("HTTPS"));
    }

}
