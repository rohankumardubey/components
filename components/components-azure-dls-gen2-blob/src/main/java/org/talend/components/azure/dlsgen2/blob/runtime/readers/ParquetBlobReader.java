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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;


public class ParquetBlobReader extends BlobReader {

    public ParquetBlobReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
        super(configuration, service, blobItems);
    }

    @Override
    protected IndexedRecordIterator initRecordIterator(Iterable<BlobItem> blobItems) {
        return new ParquetRecordIterator(blobItems);
    }

    private class ParquetRecordIterator extends IndexedRecordIterator<GenericRecord> {

        private ParquetReader<GenericRecord> reader;

        private GenericRecord currentRecord;

        private ParquetRecordIterator(Iterable<BlobItem> blobItemsList) {
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
            InputStream input = null;
            try {
                File tmp = File.createTempFile("talend-adls-gen2-tmp", ".parquet");
                tmp.deleteOnExit();
                BlobClient blobClient = service.getBlobClient(getCurrentBlob().getName());
                input = blobClient.getBlockBlobClient().openInputStream();
                Files.copy(input, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                reader =  AvroParquetReader.<GenericRecord>builder(new Path(tmp.getPath())).build();
                currentRecord = reader.read();
            } catch (IOException e) {
                throw new ComponentException(e);
            } finally {
                IOUtils.closeQuietly(input);
            }
        }

        @Override
        protected boolean hasNextBlobRecord() {
            return currentRecord != null;
        }

        @Override
        protected GenericRecord peekNextBlobRecord() {
            GenericRecord currentRecord = this.currentRecord;
            try {
                this.currentRecord = reader.read();
            } catch (IOException e) {
                LOG.error("Can't read IndexedRecord from file " + getCurrentBlob().getName(), e);
            }

            return currentRecord;
        }

        @Override
        protected void complete() {
            closePreviousInputStream();
        }

        private void closePreviousInputStream() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Can't close stream: {}.", e.getMessage());
                }
            }
        }
    }

}
