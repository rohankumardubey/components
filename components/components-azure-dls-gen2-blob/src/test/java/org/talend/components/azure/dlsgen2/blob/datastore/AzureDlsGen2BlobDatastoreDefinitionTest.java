/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package org.talend.components.azure.dlsgen2.blob.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobBase.RuntimeContainerMock;
import org.talend.daikon.runtime.RuntimeInfo;

public class AzureDlsGen2BlobDatastoreDefinitionTest {

    private AzureDlsGen2BlobDatastoreDefinition definition;

    private RuntimeContainer runtimeContainer;

    @Before
    public void setup() {
        definition = new AzureDlsGen2BlobDatastoreDefinition();
        runtimeContainer = new RuntimeContainerMock();
    }

    @Test
    public void testGetImagePath() {
        assertEquals("AzureDlsGen2BlobDatastore_icon32.png", definition.getImagePath());
    }

    @Test
    public final void testGetPropertiesClass() {
        assertEquals(AzureDlsGen2BlobDatastoreProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        final RuntimeInfo rt = definition.getRuntimeInfo(new AzureDlsGen2BlobDatastoreProperties("test"));
        final String expectedClass = "org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobDatastoreRuntime";
        assertEquals(expectedClass, rt.getRuntimeClassName());
        try {
            getClass().getClassLoader().loadClass(rt.getRuntimeClassName());
        } catch (ClassNotFoundException e) {
            fail("Should be able to load class.");
        }
    }

}