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

import java.util.Iterator;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;

public abstract class BlobReader {

    protected final IndexedRecordIterator iterator;

    protected final AzureDlsGen2BlobDatasetProperties configuration;

    protected final BlobContainerClient service;

    protected static final Logger LOG = LoggerFactory.getLogger(BlobReader.class);

    public BlobReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
        this.configuration = configuration;
        this.service = service;
        iterator = initRecordIterator(blobItems);
    }

    protected abstract IndexedRecordIterator initRecordIterator(Iterable<BlobItem> blobItems);

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public IndexedRecord readRecord() {
        return iterator.next();
    }

    public static class BlobFileReaderFactory {

        public static BlobReader getReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
            switch (configuration.format.getValue()) {
            case CSV:
                return new CsvBlobReader(configuration, service, blobItems);
            case AVRO:
                return new AvroBlobReader(configuration, service, blobItems);
            case PARQUET:
                return new ParquetBlobReader(configuration, service, blobItems);
            case JSON:
                return new JsonBlobReader(configuration, service, blobItems);
            default:
                throw new IllegalArgumentException("Unsupported file format"); // shouldn't be here
            }
        }
    }

    protected abstract static class IndexedRecordIterator<T> implements Iterator<IndexedRecord> {

        private Iterator<BlobItem> blobList;

        private BlobItem currentBlob;

        public Iterator<BlobItem> getBlobList() {
            return blobList;
        }

        public BlobItem getCurrentBlob() {
            return currentBlob;
        }

        protected IndexedRecordIterator(Iterable<BlobItem> blobList) {
            this.blobList = blobList.iterator();
        }

        @Override
        public boolean hasNext() {
            return hasNextBlobRecord() || blobList.hasNext();
        }

        @Override
        public IndexedRecord next() {
            T next = nextBlobRecord();

            return next != null ? convertToRecord(next) : null;
        }

        T nextBlobRecord() {
            if (currentBlob == null) {
                return null; // No items exists
            }

            if (hasNextBlobRecord()) {
                return peekNextBlobRecord();
            }

            while (blobList.hasNext()) {
                currentBlob = blobList.next();
                readBlob();
                if (hasNextBlobRecord()) {
                    return peekNextBlobRecord(); // read IndexedRecord from next item
                }
            }
            complete();
            return null;

        }

        protected abstract T peekNextBlobRecord();

        protected abstract boolean hasNextBlobRecord();

        protected abstract IndexedRecord convertToRecord(T next);

        protected abstract void readBlob();

        protected void peekFirstBlob() {
            if (blobList.hasNext()) {
                currentBlob = blobList.next();
                readBlob();
            }
        }

        /**
         * Release all open resources if needed
         */
        protected abstract void complete();
    }
}
