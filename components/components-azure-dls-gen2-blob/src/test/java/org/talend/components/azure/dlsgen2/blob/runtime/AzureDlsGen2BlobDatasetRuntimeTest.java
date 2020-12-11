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
package org.talend.components.azure.dlsgen2.blob.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobBase.RuntimeContainerMock;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.ValueFormat;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;
import org.talend.daikon.properties.ValidationResult.Result;

@Ignore
public class AzureDlsGen2BlobDatasetRuntimeTest {

    private AzureDlsGen2BlobDatasetRuntime runtime;

    private RuntimeContainer runtimeContainer;

    private AzureDlsGen2BlobDatasetProperties properties;

    private AzureDlsGen2BlobInputProperties inputProperties;

    @Before
    public void setUp() throws Exception {
        runtimeContainer = new RuntimeContainerMock();

        final AzureDlsGen2BlobDatastoreProperties datastore = new AzureDlsGen2BlobDatastoreProperties("test");
        datastore.setupProperties();
        datastore.setupLayout();
        datastore.accountName.setValue(System.getProperty("azure.storage.account"));
        datastore.accountKey.setValue(System.getProperty("azure.storage.key"));

        properties = new AzureDlsGen2BlobDatasetProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        properties.setDatastoreProperties(datastore);
        properties.container.setValue("adls-gen2");
        properties.path.setValue("/TestIT/in/avro");
        properties.format.setValue(ValueFormat.AVRO);

        inputProperties = new AzureDlsGen2BlobInputProperties("test");
        inputProperties.setupProperties();
        inputProperties.setupLayout();
        inputProperties.setDatasetProperties(properties);

        runtime = new AzureDlsGen2BlobDatasetRuntime();
        runtime.initialize(runtimeContainer, properties);
    }

    @Test
    public void testInitialize() {
        assertEquals(Result.OK, runtime.initialize(runtimeContainer, properties).getStatus());
    }

    @Test
    public void testGetSchema() {
        final Schema schema = runtime.getSchema();
        assertNotNull(schema);
        assertEquals("org.talend.components.adlsgen2", schema.getNamespace());
        assertEquals(7, schema.getFields().size());
    }

    @Test
    public void testGetSample() {
        runtime.getSample(15, record -> {
            assertNotNull(record);
        });
    }

    @Test
    public void testCreateDataSource() throws Exception {
        final AzureDlsGen2BlobSource source = runtime.createDataSource(inputProperties);
        assertNotNull(source);
        final BoundedReader reader = source.createReader(runtimeContainer);
        assertNotNull(reader);
        assertTrue(reader.start());
    }
}