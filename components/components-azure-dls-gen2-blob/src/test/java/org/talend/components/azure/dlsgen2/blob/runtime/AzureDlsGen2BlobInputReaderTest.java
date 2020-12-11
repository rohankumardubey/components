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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobBase.RuntimeContainerMock;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.CsvFieldDelimiter;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.ValueFormat;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;

@Ignore
public class AzureDlsGen2BlobInputReaderTest {

    private RuntimeContainer runtimeContainer;

    private AzureDlsGen2BlobInputProperties inputProperties;

    private AzureDlsGen2BlobSource source;

    private AzureDlsGen2BlobInputReader reader;


    @Before
    public void setup() {
        runtimeContainer = new RuntimeContainerMock();

        final AzureDlsGen2BlobDatastoreProperties datastore = new AzureDlsGen2BlobDatastoreProperties("test");
        datastore.setupProperties();
        datastore.accountName.setValue(System.getProperty("azure.storage.account"));
        datastore.accountKey.setValue(System.getProperty("azure.storage.key"));

        final AzureDlsGen2BlobDatasetProperties datasetProperties = new AzureDlsGen2BlobDatasetProperties("test");
        datasetProperties.setupProperties();
        datasetProperties.setDatastoreProperties(datastore);
        datasetProperties.container.setValue("adls-gen2");
        datasetProperties.path.setValue("/TestIT/in/avro");
        datasetProperties.format.setValue(ValueFormat.AVRO);

        inputProperties = new AzureDlsGen2BlobInputProperties("test");
        inputProperties.setupProperties();
        inputProperties.dataset = datasetProperties;

        source = new AzureDlsGen2BlobSource();
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
    }

    private void checkReaderProcess() {
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
        }
        assertNotNull(reader.getReturnValues());
        assertTrue(reader.getReturnValues().size() == 3);
    }

    @Test
    public void testReadAvroCustomer() {
        checkReaderProcess();
    }

    @Test
    public void testReadAvroBusiness() {
        inputProperties.dataset.path.setValue("TestIT/in/business-avro");
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
        //
        checkReaderProcess();
    }

    @Test
    public void testReadParquet() {
        inputProperties.dataset.format.setValue(ValueFormat.PARQUET);
        inputProperties.dataset.path.setValue("TestIT/in/parquet");
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
        //
        checkReaderProcess();
    }

    @Test
    public void testReadJson() {
        inputProperties.dataset.format.setValue(ValueFormat.JSON);
        inputProperties.dataset.path.setValue("TestIT/in/json-mixed");
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
        //
        checkReaderProcess();
    }

    @Test
    public void testReadCsvWithHeaders() {
        inputProperties.dataset.format.setValue(ValueFormat.CSV);
        inputProperties.dataset.delimiter.setValue(CsvFieldDelimiter.SEMICOLON);
        inputProperties.dataset.header.setValue(true);

        inputProperties.dataset.path.setValue("TestIT/in/csv-w-header");
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
        //
        checkReaderProcess();
    }

    @Test
    public void testReadCsvWithoutHeaders() {
        inputProperties.dataset.format.setValue(ValueFormat.CSV);
        inputProperties.dataset.delimiter.setValue(CsvFieldDelimiter.SEMICOLON);
        inputProperties.dataset.header.setValue(false);

        inputProperties.dataset.path.setValue("TestIT/in/csv-wo-header");
        source.initialize(runtimeContainer, inputProperties);
        reader = (AzureDlsGen2BlobInputReader) source.createReader(runtimeContainer);
        //
        checkReaderProcess();
    }
}
