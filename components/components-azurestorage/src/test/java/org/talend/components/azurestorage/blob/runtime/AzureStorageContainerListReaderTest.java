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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobStorageException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;

public class AzureStorageContainerListReaderTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    private AzureStorageContainerListReader reader;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {

        runtimeContainer = new RuntimeContainerMock();

        AzureStorageSource source = new AzureStorageSource();
        TAzureStorageContainerListProperties properties = new TAzureStorageContainerListProperties(PROP_ + "ContainerList");
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.setupProperties();

        source.initialize(runtimeContainer, properties);
        reader = (AzureStorageContainerListReader) source.createReader(runtimeContainer);
        reader.blobService = blobService;
    }


    @Test(expected = NoSuchElementException.class)
    public void getCurrentOnNonStartableReader() {
        reader.getCurrent();
        fail("should throw NoSuchElementException");
    }


        @Test
    public void testStartAsNonStartable() {
        // init mock
        try {
            when(blobService.listContainers()).thenReturn(new PagedIterable<BlobContainerItem>(new PagedFlux<BlobContainerItem>(() -> {return null;})) {

                @Override
                public Iterator<BlobContainerItem> iterator() {
                    return new DummyBlobContainerItemIterator(new ArrayList<BlobContainerItem>());
                }
            });

            boolean startable = reader.start();
            assertFalse(startable);
            assertFalse(reader.advance());

        } catch (Exception e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartAsStartabke() {

        try {
            final List<BlobContainerItem> list = new ArrayList<>();
            list.add(new BlobContainerItem());
            list.add(new BlobContainerItem());
            list.add(new BlobContainerItem());

            when(blobService.listContainers()).thenReturn(new PagedIterable<BlobContainerItem>(new PagedFlux<BlobContainerItem>(() -> {return null;})) {

                @Override
                public Iterator<BlobContainerItem> iterator() {
                    return new DummyBlobContainerItemIterator(list);
                }
            });

            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            while (reader.advance()) {
                assertNotNull(reader.getCurrent());
            }
            assertNotNull(reader.getReturnValues());
            assertEquals(3, reader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void getCurrentOnNonAdvancableReader() {

        try {
            final List<BlobContainerItem> list = new ArrayList<>();
            list.add(new BlobContainerItem());
            when(blobService.listContainers()).thenReturn(new PagedIterable<BlobContainerItem>(new PagedFlux<BlobContainerItem>(() -> {return null;})) {

                @Override
                public Iterator<BlobContainerItem> iterator() {
                    return new DummyBlobContainerItemIterator(list);
                }
            });

            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            assertFalse(reader.advance());

            reader.getCurrent();
            fail("should throw NoSuchElementException");

        } catch (BlobStorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
