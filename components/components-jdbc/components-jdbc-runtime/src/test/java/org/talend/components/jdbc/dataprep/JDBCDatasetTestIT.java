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
package org.talend.components.jdbc.dataprep;

import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatastoreRuntime;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class JDBCDatasetTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.createTestTable(conn, tablename);
            DBTestUtils.createAllTypesTable(conn, tablename_all_type);
            DBTestUtils.createTableWithSpecialName(conn,special_table_name);
            DBTestUtils.loadTestData(conn, tablename);
            DBTestUtils.loadAllTypesData(conn, tablename_all_type);
        }
    }

    private static final String tablename = "JDBCDATASET";

    private static final String tablename_all_type = "JDBCDATASETALLTYPE";
    
    private static final String special_table_name = "JDBCDATASETSPECIAL";

    @AfterClass
    public static void afterClass() throws Exception {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.dropTestTable(conn, tablename);
            DBTestUtils.dropAllTypesTable(conn, tablename_all_type);
            DBTestUtils.dropTestTable(conn, special_table_name);
        } finally {
            DBTestUtils.shutdownDBIfNecessary();
        }
    }

    @Test
    public void testUpdateSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(true, tablename);

        Schema schema = dataset.main.schema.getValue();

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata(schema.getFields(), true);
    }

    @Test
    public void testGetSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(false, tablename);

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        Schema schema = runtime.getSchema();

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata(schema.getFields(), true);
    }
    
    @Test
    public void testGetSchema4SpecialName() {
        JDBCDatasetProperties dataset = createDatasetProperties(false, special_table_name);

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        Schema schema = runtime.getSchema();

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata4SpecialName(schema.getFields());
    }

    @Test
    public void testGetSchemaFromTable() throws IOException {
        JDBCDatasetProperties dataset = createDatasetProperties(false, tablename);

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);

        JDBCSourceOrSink jss = new JDBCSourceOrSink();
        jss.initialize(null, dataset);
        Schema schema = jss.getEndpointSchema(null, tablename);

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata(schema.getFields(), true);

        jss.getEndpointSchema(null, tablename_all_type);
        // TODO assert the result
    }

    @Test
    public void testGetSampleWithValidDesignSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(true, tablename);
        getSampleAction(dataset);
    }

    @Test
    public void testGetSampleWithoutDesignSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(false, tablename);
        getSampleAction(dataset);
    }

    @Test
    public void testHive() {
        JDBCDatastoreDefinition def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore") {

            @Override
            public AllSetting getRuntimeSetting() {
                AllSetting setting = new AllSetting();

                //setting.setDriverPaths(getCurrentDriverPaths());
                setting.setDriverClass("org.apache.hive.jdbc.HiveDriver");
                setting.setJdbcUrl(jdbcUrl.getValue());

                setting.setUsername(userId.getValue());
                setting.setPassword(password.getValue());

                return setting;
            }

        };

        datastore.init();

        datastore.jdbcUrl.setValue("");//please get it from jira or supporter
        datastore.userId.setValue("");//please get it from jira or supporter


        JDBCDatasetProperties dataset = new JDBCDatasetProperties("dataset") {

            @Override
            public AllSetting getRuntimeSetting() {
                AllSetting setting = new AllSetting();

                JDBCDatastoreProperties datastoreProperties = this.getDatastoreProperties();
                //setting.setDriverPaths(datastoreProperties.getCurrentDriverPaths());
                setting.setDriverClass("org.apache.hive.jdbc.HiveDriver");
                setting.setJdbcUrl(datastoreProperties.jdbcUrl.getValue());

                setting.setUsername(datastoreProperties.userId.getValue());
                setting.setPassword(datastoreProperties.password.getValue());

                setting.setSchema(main.schema.getValue());

                setting.setSql(getSql());
                if (sourceType.getValue() == SourceType.TABLE_NAME) {
                    setting.setTablename(tableName.getValue());
                }

                return setting;
            }

        };
        dataset.init();
        dataset.setDatastoreProperties(datastore);

        dataset.sourceType.setValue(JDBCDatasetProperties.SourceType.QUERY);
        dataset.sql.setValue("select * from tab1");

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        final IndexedRecord[] record = new IndexedRecord[1];
        Consumer<IndexedRecord> storeTheRecords = new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord data) {
                record[0] = data;

            }
        };

        runtime.getSample(1, storeTheRecords);
        Assert.assertEquals("valeur1", record[0].get(0));
        Assert.assertEquals("valeur2", record[0].get(1));

        List<Schema.Field> fields = record[0].getSchema().getFields();
        Assert.assertEquals("tab1_name", fields.get(0).name());
        Assert.assertEquals("tab1_surname", fields.get(1).name());

        Assert.assertEquals("tab1.name", fields.get(0).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        Assert.assertEquals("tab1.surname", fields.get(1).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
    }

    @Test
    public void testDoHealthChecks() {
        JDBCDatasetProperties dataset = createDatasetProperties(true, tablename);
        JDBCDatastoreRuntime runtime = new JDBCDatastoreRuntime();
        runtime.initialize(null, dataset.getDatastoreProperties());
        Iterable<ValidationResult> result = runtime.doHealthChecks(null);
        Assert.assertNotNull(result);
        Iterator<ValidationResult> iterator = result.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(ValidationResult.Result.OK, iterator.next().getStatus());
    }

    private void getSampleAction(JDBCDatasetProperties dataset) {
        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        final IndexedRecord[] record = new IndexedRecord[1];
        Consumer<IndexedRecord> storeTheRecords = new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord data) {
                record[0] = data;

            }
        };

        runtime.getSample(1, storeTheRecords);
        Assert.assertEquals("1", record[0].get(0));
        Assert.assertEquals("wangwei", record[0].get(1));
    }

    private JDBCDatasetProperties createDatasetProperties(boolean updateSchema, String tablename) {
        JDBCDatastoreDefinition def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore");
        datastore.init();

        datastore.dbTypes.setValue("DERBY");
        datastore.afterDbTypes();

        datastore.jdbcUrl.setValue(allSetting.getJdbcUrl());
        datastore.userId.setValue(allSetting.getUsername());
        datastore.password.setValue(allSetting.getPassword());

        JDBCDatasetProperties dataset = (JDBCDatasetProperties) def.createDatasetProperties(datastore);
        dataset.sql.setValue("select ID as \"JDBCDATASET.ID\", NAME as \"JDBCDATASET.NAME\" from JDBCDATASET");

        if (updateSchema) {
            updateSchema(dataset);
        }
        return dataset;
    }

    protected void updateSchema(JDBCDatasetProperties dataset) {
        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        dataset.main.schema.setValue(runtime.getSchema());
    }

}
