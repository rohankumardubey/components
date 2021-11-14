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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;

public class CsvBlobReader extends BlobReader {

    CsvBlobReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
        super(configuration, service, blobItems);
    }

    @Override
    protected IndexedRecordIterator initRecordIterator(Iterable<BlobItem> blobItems) {
        return new CSVFileRecordIterator(blobItems, service);
    }

    @Override
    public IndexedRecord readRecord() {
        return super.readRecord();
    }

    private class CSVFileRecordIterator extends IndexedRecordIterator<CSVRecord> {

        private InputStream currentItemInputStream;

        private Iterator<CSVRecord> csvRecordIterator;

        private CSVFormat format;

        private CsvConverter converter;

        private BlobContainerClient service;

        private CSVParser parser;

        private String encoding;

        private CSVFileRecordIterator(Iterable<BlobItem> blobList, BlobContainerClient service) {
            super(blobList);
            this.service = service;
            encoding = configuration.encoding.getEncoding();
            peekFirstBlob();
        }

        @Override
        protected IndexedRecord convertToRecord(CSVRecord next) {
            return converter.toRecord(next);
        }

        @Override
        protected void readBlob() {
            initMetadataIfNeeded();
            closePreviousInputStream();
            try {
                BlobClient blobClient = service.getBlobClient(getCurrentBlob().getName());
                currentItemInputStream = blobClient.getBlockBlobClient().openInputStream();
                InputStreamReader inr = new InputStreamReader(currentItemInputStream, Charset.forName(encoding));
                parser = new CSVParser(inr, format);
                converter.setRuntimeHeaders(parser.getHeaderMap());
                csvRecordIterator = parser.iterator();
            } catch (Exception e) {
                throw new ComponentException(e);
            }
        }

        @Override
        protected boolean hasNextBlobRecord() {
            return csvRecordIterator.hasNext();
        }

        @Override
        protected CSVRecord peekNextBlobRecord() {
            return csvRecordIterator.next();
        }

        @Override
        protected void complete() {
            closePreviousInputStream();
        }

        private void initMetadataIfNeeded() {
            if (converter == null) {
                converter = new CsvConverter(configuration);
            }

            if (format == null) {
                format = converter.getCsvFormat();
            }
        }

        private void closePreviousInputStream() {
            if (currentItemInputStream != null) {
                try {
                    currentItemInputStream.close();
                    parser.close();
                } catch (IOException e) {
                    LOG.error("Can't close stream: {}.", e.getMessage());
                }
            }
        }
    }
}
