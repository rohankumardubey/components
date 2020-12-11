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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.azure.dlsgen2.blob.AzureDlsGen2BlobBase.RuntimeContainerMock;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

@Ignore
public class AzureDlsGen2BlobDatastoreRuntimeTest {

    private AzureDlsGen2BlobDatastoreRuntime runtime;

    private AzureDlsGen2BlobDatastoreProperties properties;

    private RuntimeContainerMock runtimeContainer;

    @Before
    public void setup() {
        runtimeContainer = new RuntimeContainerMock();

        properties = new AzureDlsGen2BlobDatastoreProperties("test");
        properties.setupProperties();
        properties.accountName.setValue(System.getProperty("azure.storage.account"));
        properties.accountKey.setValue(System.getProperty("azure.storage.key"));

        runtime = new AzureDlsGen2BlobDatastoreRuntime();
        runtime.datastore = properties;
    }

    @Test
    public void testDoHealthChecks() {
        final ValidationResult result = runtime.initialize(runtimeContainer, properties);
        assertEquals(Result.OK, result.getStatus());
        final Iterable<ValidationResult> results = runtime.doHealthChecks(runtimeContainer);
        results.forEach(r -> assertEquals(Result.OK, r.getStatus()));
    }

    @Test
    public void testDoHealthChecksFail() {
        properties.accountKey.setValue("");
        runtime.initialize(runtimeContainer, properties);
        final Iterable<ValidationResult> results = runtime.doHealthChecks(runtimeContainer);
        results.forEach(r -> assertEquals(Result.ERROR, r.getStatus()));
    }
}