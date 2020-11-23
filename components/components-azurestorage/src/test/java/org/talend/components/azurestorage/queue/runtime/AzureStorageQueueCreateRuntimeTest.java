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
package org.talend.components.azurestorage.queue.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.queue.models.QueueStorageException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class AzureStorageQueueCreateRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueCreateRuntime.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStorageQueueCreateProperties properties;

    private AzureStorageQueueCreateRuntime azureStorageQueueCreate;

    private QueueStorageException storageException;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageQueueCreateProperties(PROP_ + "Get");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
        this.azureStorageQueueCreate = new AzureStorageQueueCreateRuntime();

        storageException = new QueueStorageException("storage exception message", new HttpResponse(null) {

            @Override
            public int getStatusCode() {
                return 500;
            }

            @Override
            public String getHeaderValue(String name) {
                return "headers.getValue(name)";
            }

            @Override
            public HttpHeaders getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-ms-error-code", "500");
                return new HttpHeaders(headers);
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.empty();
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(new byte[0]);
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just("");
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just("");
            }

        }, new RuntimeException());
    }

    @Test
    public void testInitializeEmptyQueue() {
        properties.queueName.setValue("");
        ValidationResult validationResult = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.NameEmpty"), validationResult.getMessage());
    }

    @Test
    public void testInitializeNonentityLocal() {

        ValidationResult vrSize = new ValidationResult(Result.ERROR, messages.getMessage("error.LengthError"));
        ValidationResult vrDash = new ValidationResult(Result.ERROR, messages.getMessage("error.TwoDashError"));
        ValidationResult vrName = new ValidationResult(Result.ERROR, messages.getMessage("error.QueueNameError"));

        // invalid queue size
        properties.queueName.setValue("in");
        ValidationResult validationResult = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(vrSize.getMessage(), validationResult.getMessage());

        properties.queueName.setValue("a-too-long-queue-name-a-too-long-queue-name-a-too-long-queue-name");
        ValidationResult validationResult2 = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult2.getStatus());
        assertEquals(vrSize.getMessage(), validationResult2.getMessage());

        // invalid queue name dashes
        properties.queueName.setValue("in--in");
        ValidationResult validationResult3 = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult3.getStatus());
        assertEquals(vrDash.getMessage(), validationResult3.getMessage());

        // invalid queue name
        properties.queueName.setValue("a-wrongQueueName");
        ValidationResult validationResult4 = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult4.getStatus());
        assertEquals(vrName.getMessage(), validationResult4.getMessage());

    }

    @Test
    public void testInitializeValidProperties() {
        properties.queueName.setValue("a-good-queue-name");
        ValidationResult validationResult = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertNull(validationResult.getMessage());
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        properties.queueName.setValue("2queue-name-with-numbers2");
        ValidationResult validationResult2 = azureStorageQueueCreate.initialize(runtimeContainer, properties);
        assertNull(validationResult2.getMessage());
        assertEquals(ValidationResult.OK.getStatus(), validationResult2.getStatus());
    }

    @Test
    public void testRunAtDriverQueueCreationSuccess() {
        properties.queueName.setValue("a-good-queue-name");
        azureStorageQueueCreate.initialize(runtimeContainer, properties);
        azureStorageQueueCreate.queueService = queueService;
        try {
            when(queueService.createQueueIfNotExists(anyString())).thenReturn(true);
            azureStorageQueueCreate.runAtDriver(runtimeContainer);
        } catch (QueueStorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverQueueAlReadyExist() {
        properties.queueName.setValue("a-good-queue-name");
        azureStorageQueueCreate.initialize(runtimeContainer, properties);
        azureStorageQueueCreate.queueService = queueService;
        try {
            when(queueService.createQueueIfNotExists(anyString())).thenReturn(false);
            azureStorageQueueCreate.runAtDriver(runtimeContainer);
        } catch (QueueStorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverHandleStorageException() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(false);
        azureStorageQueueCreate.initialize(runtimeContainer, properties);
        azureStorageQueueCreate.queueService = queueService;
        try {
            when(queueService.createQueueIfNotExists(anyString()))
                    .thenThrow(new QueueStorageException("errorCode", null, new RuntimeException()));
            azureStorageQueueCreate.runAtDriver(runtimeContainer);
        } catch (QueueStorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(true);
        azureStorageQueueCreate.initialize(runtimeContainer, properties);
        azureStorageQueueCreate.queueService = queueService;
        try {
            when(queueService.createQueueIfNotExists(anyString())).thenThrow(storageException);
            azureStorageQueueCreate.runAtDriver(runtimeContainer);
        } catch (QueueStorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
