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
 *
 */
package org.talend.components.azure.dlsgen2.blob.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_UTF_8;

import java.util.Locale;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.CsvFieldDelimiter;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.CsvRecordSeparator;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties.ValueFormat;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.presentation.Form;

public class AzureDlsGen2BlobDatasetPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private AzureDlsGen2BlobDatasetProperties properties;

    @Before
    public void setup() {
        properties = new AzureDlsGen2BlobDatasetProperties("test");
        properties.init();
        properties.setDatastoreProperties(new AzureDlsGen2BlobDatastoreProperties("test"));
        properties.setupProperties();
        properties.setupLayout();
        properties.refreshLayout(properties.getForms().get(0));
    }

    @Test
    public void testI18N() {
        final AzureDlsGen2BlobDatasetProperties props = new AzureDlsGen2BlobDatasetProperties("test");
        props.init();
        ComponentTestUtils.checkAllI18N(props, errorCollector);
    }

    @Test
    public void testLabelProperties() {
        Locale.setDefault(new Locale("en", "US"));
        final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
                .getI18nMessages(AzureDlsGen2BlobDatasetProperties.class);
        assertEquals("Container", messages.getMessage(properties.container.getDisplayName()));
        assertEquals("Blob path", messages.getMessage(properties.path.getDisplayName()));
        assertEquals("Format", messages.getMessage(properties.format.getDisplayName()));
        assertEquals("Field delimiter", messages.getMessage(properties.delimiter.getDisplayName()));
        assertEquals("Record separator", messages.getMessage(properties.separator.getDisplayName()));
        assertEquals("Enclosure char", messages.getMessage(properties.enclosure.getDisplayName()));
        assertEquals("Escape char", messages.getMessage(properties.escape.getDisplayName()));
        assertEquals("File encoding", messages.getMessage(properties.encoding.getDisplayName()));
        assertEquals("Header", messages.getMessage(properties.header.getDisplayName()));
    }

    @Test
    public void testSetupProperties() {
        assertEquals("", properties.container.getValue());
        assertEquals("", properties.path.getValue());
        assertEquals(ValueFormat.AVRO, properties.format.getValue());
        assertEquals(CsvFieldDelimiter.SEMICOLON, properties.delimiter.getValue());
        assertEquals(CsvRecordSeparator.CRLF, properties.separator.getValue());
        assertEquals("", properties.enclosure.getValue());
        assertEquals("", properties.escape.getValue());
        assertEquals(ENCODING_TYPE_UTF_8, properties.encoding.getEncoding());
        assertFalse(properties.header.getValue());
    }

    @Test
    public void testSetupLayout() {
        Form form = properties.getForms().get(0);
        assertTrue(form.getWidget("container").isVisible());
        assertTrue(form.getWidget("path").isVisible());
        assertTrue(form.getWidget("format").isVisible());
        assertFalse(form.getWidget("delimiter").isVisible());
        assertFalse(form.getWidget("separator").isVisible());
        assertFalse(form.getWidget("enclosure").isVisible());
        assertFalse(form.getWidget("escape").isVisible());
        assertFalse(form.getWidget("encoding").isVisible());
        assertFalse(form.getWidget("header").isVisible());
        properties.format.setValue(ValueFormat.CSV);
        properties.refreshLayout(form);
        assertTrue(form.getWidget("container").isVisible());
        assertTrue(form.getWidget("path").isVisible());
        assertTrue(form.getWidget("format").isVisible());
        assertTrue(form.getWidget("delimiter").isVisible());
        assertTrue(form.getWidget("separator").isVisible());
        assertTrue(form.getWidget("enclosure").isVisible());
        assertTrue(form.getWidget("escape").isVisible());
        assertTrue(form.getWidget("encoding").isVisible());
        assertTrue(form.getWidget("header").isVisible());
    }

    @Test
    public void testGetDatastoreProperties() {
        assertNotNull(properties.getDatastoreProperties());
    }

    @Test
    public void testAfterFormat() {
        Form form = properties.getForms().get(0);
        assertTrue(form.getWidget("container").isVisible());
        assertTrue(form.getWidget("path").isVisible());
        assertTrue(form.getWidget("format").isVisible());
        assertFalse(form.getWidget("delimiter").isVisible());
        assertFalse(form.getWidget("separator").isVisible());
        assertFalse(form.getWidget("enclosure").isVisible());
        assertFalse(form.getWidget("escape").isVisible());
        assertFalse(form.getWidget("encoding").isVisible());
        assertFalse(form.getWidget("header").isVisible());
        properties.format.setValue(ValueFormat.CSV);
        properties.afterFormat();
        assertTrue(form.getWidget("delimiter").isVisible());
        assertTrue(form.getWidget("separator").isVisible());
        assertTrue(form.getWidget("enclosure").isVisible());
        assertTrue(form.getWidget("escape").isVisible());
        assertTrue(form.getWidget("encoding").isVisible());
        assertTrue(form.getWidget("header").isVisible());
    }

    @Test
    public void testGetSchema() {
        Schema schema = properties.getSchema();
        assertEquals(0, schema.getFields().size());
        assertEquals("org.talend.avro.schema", schema.getNamespace());
        assertEquals(Type.RECORD, schema.getType());
    }

}