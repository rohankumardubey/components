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
package org.talend.components.azurestorage.blob.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobStorageException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageContainerDeleteRuntimeTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    private TAzureStorageContainerDeleteProperties properties;

    private AzureStorageContainerDeleteRuntime deleteContainer;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        properties = new TAzureStorageContainerDeleteProperties(PROP_ + "DeleteContainer");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
        this.deleteContainer = new AzureStorageContainerDeleteRuntime();
    }

    @Test
    public void testInitializeEmptyContainerName() {
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
    }

    @Test
    public void testInitializeValide() {
        properties.container.setValue("container");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleStorageException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;

        try {
            when(blobService.deleteContainerIfExist(anyString()))
                    .thenThrow(new BlobStorageException("storage exception message", null, new RuntimeException()));
            deleteContainer.runAtDriver(runtimeContainer);
        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleInvalidKeyException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;

        try {
            when(blobService.deleteContainerIfExist(anyString())).thenThrow(new InvalidKeyException());
            deleteContainer.runAtDriver(runtimeContainer);
        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleURISyntaxException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;
        try {

            when(blobService.deleteContainerIfExist(anyString()))
                    .thenThrow(new URISyntaxException("bad url", "some reason"));
            deleteContainer.runAtDriver(runtimeContainer);
        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {

        properties.container.setValue("container-name-ok");
        properties.dieOnError.setValue(true);
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;

        try {
            when(blobService.deleteContainerIfExist(anyString()))
                    .thenThrow(new BlobStorageException("storage exception message", new HttpResponse(null) {
                        @Override
                        public int getStatusCode() {
                            return 500;
                        }

                        @Override
                        public String getHeaderValue(final String s) {
                            return s;
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return null;
                        }

                        @Override
                        public Flux<ByteBuffer> getBody() {
                            return "#getBodyAsString()";
                        }

                        @Override
                        public Mono<byte[]> getBodyAsByteArray() {
                            throw new UnsupportedOperationException("#getBodyAsByteArray()");
                        }

                        @Override
                        public Mono<String> getBodyAsString() {
                            throw new UnsupportedOperationException("#getBodyAsString()");
                        }

                        @Override
                        public Mono<String> getBodyAsString(final Charset charset) {
                            throw new UnsupportedOperationException("#getBodyAsString()");
                        }
                    } , new RuntimeException()));
            deleteContainer.runAtDriver(runtimeContainer);

        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverContainerDontExist() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;
        try {
            when(blobService.deleteContainerIfExist(anyString())).thenReturn(false);
            deleteContainer.runAtDriver(runtimeContainer);
        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }

    }

    @Test
    public void testrunAtDriverValid() {
        properties.container.setValue("container");
        ValidationResult validationResult = deleteContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteContainer.blobService = blobService;
        try {

            when(blobService.deleteContainerIfExist(anyString())).thenReturn(true);
            deleteContainer.runAtDriver(runtimeContainer);
        } catch (BlobStorageException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

}
