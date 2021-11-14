// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azure.dlsgen2.blob.dataset;

import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_CUSTOM;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_UTF_8;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AzureDlsGen2BlobDatasetProperties extends ComponentPropertiesImpl implements DatasetProperties<AzureDlsGen2BlobDatastoreProperties> {

    public final transient ReferenceProperties<AzureDlsGen2BlobDatastoreProperties> datastoreRef = new ReferenceProperties<>(
            "datastoreRef", AzureDlsGen2BlobDatastoreDefinition.NAME);

    /**
     * container - the AzureStorage remote container/fs name.
     */
    public Property<String> container = PropertyFactory.newString("container").setRequired(true);

    /**
     * path - the AzureStorage remote folder
     */
    public Property<String> path = PropertyFactory.newString("path").setRequired(true);

    public EnumProperty<ValueFormat> format = PropertyFactory.newEnum("format", ValueFormat.class);

    public EnumProperty<CsvFieldDelimiter> delimiter = PropertyFactory.newEnum("delimiter", CsvFieldDelimiter.class);

    public EnumProperty<CsvRecordSeparator> separator = PropertyFactory.newEnum("separator", CsvRecordSeparator.class);

    public Property<Boolean> header = PropertyFactory.newBoolean("header");

    public Property<String> enclosure = PropertyFactory.newString("enclosure");

    public Property<String> escape = PropertyFactory.newString("escape");

    public EncodingTypeProperties encoding = new EncodingTypeProperties("encoding");

    public SchemaProperties main = new SchemaProperties("main");

    public AzureDlsGen2BlobDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        setupSchema();
        container.setValue("");
        path.setValue("");
        format.setValue(ValueFormat.AVRO);
        delimiter.setValue(CsvFieldDelimiter.SEMICOLON);
        separator.setValue(CsvRecordSeparator.CRLF);
        enclosure.setValue("");
        escape.setValue("");
        encoding.encodingType
                .setPossibleValues(Arrays.asList(ENCODING_TYPE_UTF_8, "UTF-16", "UTF-16LE", "UTF-16BE", "UTF-7",
                                                 "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
                                                 "ISO-8859-9", "ISO-8859-10", ENCODING_TYPE_ISO_8859_15, "windows-1252", "BIG5", "GB18030", "GB2312", "EUC_CN",
                                                 ENCODING_TYPE_CUSTOM));
        encoding.encodingType.setValue(ENCODING_TYPE_UTF_8);
    }

    @Override
    public void setupLayout() {
        Form main = new Form(this, Form.MAIN);
        main.addRow(container);
        main.addRow(path);
        main.addRow(format);
        main.addRow(delimiter);
        main.addColumn(separator);
        main.addRow(enclosure);
        main.addColumn(escape);
        main.addRow(header);
        main.addRow(encoding);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            boolean isCSV = format.getValue() == ValueFormat.CSV;
            form.getWidget(delimiter.getName()).setVisible(isCSV);
            form.getWidget(separator.getName()).setVisible(isCSV);
            form.getWidget(escape.getName()).setVisible(isCSV);
            form.getWidget(enclosure.getName()).setVisible(isCSV);
            form.getWidget(header.getName()).setVisible(isCSV);
            form.getWidget(encoding.getName()).setVisible(isCSV);
        }
    }

    @Override
    public AzureDlsGen2BlobDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(AzureDlsGen2BlobDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    public void afterFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    public Schema getSchema() {
        return main.schema.getValue();
    }

    public void setupSchema() {
        main.schema.setValue(SchemaBuilder.builder("org.talend.avro.schema").record("empty").fields().endRecord());
    }

    public enum ValueFormat {
        CSV,
        AVRO,
        PARQUET,
        JSON
    }

    public enum CsvFieldDelimiter {
        SEMICOLON(';'),
        COMMA(','),
        TABULATION('\t'),
        SPACE(' '),
        OTHER((char) 0);

        private char delimiter;

        CsvFieldDelimiter(char delimiter) {
            this.delimiter = delimiter;
        }

        public char getDelimiter() {
            return delimiter;
        }
    }

    public enum CsvRecordSeparator {
        LF("\n"),
        CR("\r"),
        CRLF("\r\n"),
        OTHER("");

        private final String separator;

        CsvRecordSeparator(final String separator) {
            this.separator = separator;
        }

        public String getSeparator() {
            return separator;
        }
    }

}
