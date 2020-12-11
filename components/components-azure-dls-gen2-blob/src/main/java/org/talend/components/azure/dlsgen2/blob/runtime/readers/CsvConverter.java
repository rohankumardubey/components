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


import static org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2Services.normalizeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;

public class CsvConverter {

    private static final String NAMESPACE = "org.talend.avro.schema.infer.csv";

    public static final String RECORD_NAME = "SchemalessCsvToIndexedRecord";

    private CSVFormat csvFormat;

    private Schema schema;

    private Map<String, Integer> runtimeHeaders;

    public CsvConverter(final AzureDlsGen2BlobDatasetProperties configuration) {
        csvFormat = formatWithConfiguration(configuration);
        schema = schemaWithConfiguration(configuration);
    }

    private Schema schemaWithConfiguration(final AzureDlsGen2BlobDatasetProperties configuration) {
        if (configuration.getSchema().getFields().isEmpty()) {
            // will infer schema on runtime
            return null;
        }
        return configuration.getSchema();
    }

    private CSVFormat formatWithConfiguration(final AzureDlsGen2BlobDatasetProperties configuration) {
        char delimiter = configuration.delimiter.getValue().getDelimiter();
        String separator = configuration.separator.getValue().getSeparator();
        String escape = configuration.escape.getValue();
        String enclosure = configuration.enclosure.getValue();
        CSVFormat format = CSVFormat.DEFAULT;
        // delimiter
        format = format.withDelimiter(delimiter);
        // record separator
        if (StringUtils.isNotEmpty(separator)) {
            format = format.withRecordSeparator(separator);
        }
        // escape character
        if (StringUtils.isNotEmpty(escape) && escape.length() == 1) {
            format = format.withEscape(escape.charAt(0));
        }
        // text enclosure
        if (StringUtils.isNotEmpty(enclosure) && enclosure.length() == 1) {
            format = format.withQuote(enclosure.charAt(0));
            format = format.withQuoteMode(QuoteMode.ALL);
        } else {
            // CSVFormat.DEFAULT has quotes defined
            format = format.withQuote(null);
        }
        // first line is header
        if (configuration.header.getValue()) {
            format = format.withFirstRecordAsHeader();
        }

        return format;
    }

    public CSVFormat getCsvFormat() {
        return csvFormat;
    }

    public Schema inferSchema(CSVRecord record) {
        final Schema schema = Schema.createRecord(RECORD_NAME, "", NAMESPACE, false);
        final Schema nullableString = SchemaBuilder.nullable().stringType();
        List<Schema.Field> fields = new ArrayList<>();
        if (runtimeHeaders != null) {
            for (Entry<String, Integer> f : runtimeHeaders.entrySet()) {
                Schema.Field field = new Schema.Field(normalizeName(f.getKey()), nullableString, "", null);
                fields.add(field);
            }
        } else {
            for (int i = 0; i < record.size(); i++) {
                Schema.Field field = new Schema.Field("newColumn" + i, nullableString, "", null);
                fields.add(field);
            }
        }
        schema.setFields(fields);
        return schema;
    }

    public void setRuntimeHeaders(final Map<String, Integer> runtimeHeaders) {
        this.runtimeHeaders = runtimeHeaders;
    }

    public IndexedRecord toRecord(CSVRecord csvRecord) {
        if (schema == null) {
            schema = inferSchema(csvRecord);
        }
        GenericData.Record record = new GenericData.Record(schema);
        for (Field f : schema.getFields()) {
            String value;
            try {
                value = csvRecord.get(f.pos()).isEmpty() ? null : csvRecord.get(f.pos());
            } catch (ArrayIndexOutOfBoundsException e) {
                value = null;
            }
            record.put(f.pos(), value);
        }

        return record;
    }

}
