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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureBaseTest;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageQueueInputReaderTest extends AzureBaseTest {

    private TAzureStorageQueueInputProperties properties;

    private AzureStorageQueueInputReader reader;

    private QueueStorageException storageException;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageQueueInputProperties(PROP_ + "QueueInputReader");
        properties.setupProperties();
        properties.connection = getValidFakeConnection();
        properties.queueName.setValue("some-queue-name");

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
    public void testStartPeekAsStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartRetrieveMessageAsStartable() {
        try {
            properties.peekMessages.setValue(false);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<QueueMessageItem> messages = new ArrayList<>();
            messages.add(new QueueMessageItem());
            when(queueService.retrieveMessages(anyString(), anyInt(), anyInt()))
                    .thenReturn(new Iterable<QueueMessageItem>() {

                        @Override
                        public Iterator<QueueMessageItem> iterator() {
                            return new DummyQueueMessageIterator(messages);
                        }
                    });
            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekAsNonStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekAndDeleteMessage() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekHandleDeletionError() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new BlobStorageException("message-1 can't be deleted", null, new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartHandleError() {
        try {
            properties.peekMessages.setValue(true);
            properties.dieOnError.setValue(false);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<QueueMessageItem> messages = new ArrayList<>();
            messages.add(new QueueMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenThrow(storageException);

            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testStartDieOnError() {
        try {
            properties.peekMessages.setValue(true);
            properties.dieOnError.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<QueueMessageItem> messages = new ArrayList<>();
            messages.add(new QueueMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenThrow(storageException);

            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsAdvancable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonAdvancable() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new BlobStorageException("message-1 can't be deleted", null, new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertFalse(advancable);

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWhenNonStartble() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new BlobStorageException("message-1 can't be deleted", null, new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertFalse(startable);
            boolean advancable = reader.advance();
            assertFalse(advancable);

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWithMessageDeletion() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWithMessageDeletionHandleError() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new BlobStorageException("message-1 can't be deleted", null, new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(QueueMessageItem.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetCurrent() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            for (int idx = 1; idx < 4; idx++) {
                final PeekedMessageItem m = new PeekedMessageItem();
                m.setMessageText("message-" + idx);
                messages.add(m);
            }
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            int i = 1;
            do {
                IndexedRecord current = reader.getCurrent();
                assertNotNull(current);
                assertNotNull(current.getSchema());
                Field msgField = current.getSchema().getField(TAzureStorageQueueInputProperties.FIELD_MESSAGE_CONTENT);
                assertTrue(current.get(msgField.pos()).equals("message-" + i));
                i++;
            } while (reader.advance());

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertFalse(startable);
            reader.getCurrent(); // should throw NoSuchElementException

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotAdvancable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            boolean advancable = reader.advance();
            assertFalse(advancable);
            reader.getCurrent(); // should throw NoSuchElementException

        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetReturnValues() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<PeekedMessageItem> messages = new ArrayList<>();
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            messages.add(new PeekedMessageItem());
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<PeekedMessageItem>() {

                @Override
                public Iterator<PeekedMessageItem> iterator() {
                    return new DummyQueuePeekedMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            while (reader.advance()) {
                // read all messages to init the returned values at the end
            }
            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertEquals("some-queue-name", returnedValues.get(AzureStorageQueueDefinition.RETURN_QUEUE_NAME));
            assertEquals(3, returnedValues.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        } catch (IOException | BlobStorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }
}
