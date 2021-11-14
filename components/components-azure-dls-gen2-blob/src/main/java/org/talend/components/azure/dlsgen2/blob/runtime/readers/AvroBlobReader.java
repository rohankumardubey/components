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
 */
package org.talend.components.azure.dlsgen2.blob.runtime.readers;

import java.io.IOException;
import java.io.InputStream;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumReader;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;


public class AvroBlobReader extends BlobReader {

    public AvroBlobReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
        super(configuration, service, blobItems);
    }

    @Override
    protected IndexedRecordIterator initRecordIterator(Iterable<BlobItem> blobItems) {
        return new AvroFileRecordIterator(blobItems);
    }

    private class AvroFileRecordIterator extends IndexedRecordIterator<GenericRecord> {

        private DataFileStream<GenericRecord> avroItemIterator;

        private InputStream input;

        private AvroFileRecordIterator(Iterable<BlobItem> blobItemsList) {
            super(blobItemsList);
            peekFirstBlob();
        }

        @Override
        protected IndexedRecord convertToRecord(GenericRecord next) {
            return next;
        }

        @Override
        protected void readBlob() {
            closePreviousInputStream();
            try {
                BlobClient blobClient = service.getBlobClient(getCurrentBlob().getName());
                input = blobClient.getBlockBlobClient().openInputStream();
                DatumReader<GenericRecord> reader = new GenericDatumReader<>();
                avroItemIterator = new DataFileStream<>(input, reader);
            } catch (Exception e) {
                throw new ComponentException(e);
            }
        }

        @Override
        protected boolean hasNextBlobRecord() {
            return avroItemIterator.hasNext();
        }

        @Override
        protected GenericRecord peekNextBlobRecord() {
            return avroItemIterator.next();
        }

        @Override
        protected void complete() {
            closePreviousInputStream();
        }

        private void closePreviousInputStream() {
            if (avroItemIterator != null) {
                try {
                    avroItemIterator.close();
                    input.close();
                } catch (IOException e) {
                    LOG.error("Can't close stream: {}.", e.getMessage());
                }
            }
        }
    }
}
