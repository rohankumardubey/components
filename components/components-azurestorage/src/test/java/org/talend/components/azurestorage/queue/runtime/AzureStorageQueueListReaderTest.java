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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueStorageException;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureBaseTest;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageQueueListReaderTest extends AzureBaseTest {

    private AzureStorageQueueListReader reader;

    private TAzureStorageQueueListProperties properties;

    private StorageSharedKeyCredential dummyCredential;

    private QueueStorageException storageException;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        dummyCredential = new StorageSharedKeyCredential("fakesaas", "fakekey");
        properties = new TAzureStorageQueueListProperties(PROP_ + "QueueList");
        properties.setupProperties();
        properties.connection = getValidFakeConnection();

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
    public void testStartAsStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {
            list.add(new QueueItem());
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartAsNonStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertFalse(reader.start());

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartHandleError() {

        properties.dieOnError.setValue(false);

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;
        try {

            when(queueService.listQueues()).thenThrow(storageException);

            assertFalse(reader.start());

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testStartDieOnError() {

        properties.dieOnError.setValue(true);

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;
        try {

            when(queueService.listQueues()).thenThrow(storageException);
            reader.start(); // should throw ComponentException

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsAdvancable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            list.add(new QueueItem());
            list.add(new QueueItem());
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());
            assertTrue(reader.advance());

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonAdvancable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            list.add(new QueueItem());
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());
            assertFalse(reader.advance());

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertFalse(reader.start());
            assertFalse(reader.advance());

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetCurrent() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {
            final QueueItem q = new QueueItem();
            q.setName("queue-1");
            list.add(q);
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());
            IndexedRecord current = reader.getCurrent();
            assertNotNull(current);
            assertEquals("queue-1", current.get(0));

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotStartable() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertFalse(reader.start());
            reader.getCurrent();

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotAdvancable() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            list.add(new QueueItem());
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());
            assertFalse(reader.advance());
            reader.getCurrent();

        } catch (IOException | BlobStorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetReturnValues() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<QueueItem> list = new ArrayList<>();
        try {

            list.add(new QueueItem());
            list.add(new QueueItem());
            list.add(new QueueItem());
            when(queueService.listQueues()).thenReturn(new Iterable<QueueItem>() {

                @Override
                public Iterator<QueueItem> iterator() {
                    return new DummyQueueItemIterator(list);
                }
            });

            assertTrue(reader.start());
            while (reader.advance()) {
                // read all records
            }

            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertEquals(3, returnedValues.get(TAzureStorageQueueListDefinition.RETURN_NB_QUEUE));

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
