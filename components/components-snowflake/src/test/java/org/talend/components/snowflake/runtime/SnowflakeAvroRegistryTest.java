package org.talend.components.snowflake.runtime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.google.common.collect.ImmutableMap;

/**
 * Unit-tests for {@link SnowflakeAvroRegistry} class
 */
public class SnowflakeAvroRegistryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeAvroRegistryTest.class);

    private static final String TALEND_EXPECTED_DATE_PATTERN = "yyyy-MM-dd";

    private static final String TALEND_EXPECTED_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final String FIELD_NAME = "fieldName";

    private static final String DB_COLUMN_NAME = "dbColumnName";

    private static final String DEFAULT_VALUE = "defaultValue";

    private SnowflakeAvroRegistry snowflakeAvroRegistry;

    private int size;

    private int scale = 5;

    private boolean nullable;

    private Map<Integer, Schema> testPairsForAvroTypes = ImmutableMap
            .of(Types.VARCHAR, AvroUtils._string(), Types.DECIMAL, AvroUtils._decimal(), Types.DOUBLE, AvroUtils._double(), Types.BOOLEAN, AvroUtils._boolean(), Types.JAVA_OBJECT,
                    AvroUtils._string());

    @Before
    public void setUp() throws Exception {
        snowflakeAvroRegistry = SnowflakeAvroRegistry.get();
        size = 10;
        scale = 5;
        nullable = true;
    }

    /**
     * Check if sql types converted properly to Avro types
     */
    @Test
    public void testSqlType2AvroString() {
        for (Map.Entry<Integer, Schema> entry : testPairsForAvroTypes.entrySet()) {
            Schema.Field field = snowflakeAvroRegistry
                    .sqlType2Avro(size, scale, entry.getKey(), nullable, FIELD_NAME, DB_COLUMN_NAME, DEFAULT_VALUE);

            Assert.assertEquals(FIELD_NAME, field.name());
            Assert.assertEquals(-1, field.pos());
            Assert.assertEquals(entry.getKey(), field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
            Assert.assertEquals(DB_COLUMN_NAME, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
            Assert.assertTrue(AvroUtils.isSameType(AvroUtils.unwrapIfNullable(field.schema()), entry.getValue()));
        }
    }

    /**
     * Checks {@link SnowflakeAvroRegistry#sqlType2Avro(int, int, int, boolean, String, String, Object)}
     * returns the {@link org.apache.avro.Schema.Field} with logical DATE type
     */
    @Test
    public void testSqlType2AvroDate() throws Exception {
        final int dbtype = java.sql.Types.DATE;

        Schema.Field field = snowflakeAvroRegistry
                .sqlType2Avro(size, scale, dbtype, nullable, FIELD_NAME, DB_COLUMN_NAME, DEFAULT_VALUE);

        LOGGER.debug("field: " + field.toString());

        Assert.assertEquals(FIELD_NAME, field.name());
        Assert.assertEquals(-1, field.pos());
        Assert.assertEquals(TALEND_EXPECTED_DATE_PATTERN, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        Assert.assertEquals(java.sql.Types.DATE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        Assert.assertEquals(DB_COLUMN_NAME, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        Assert.assertEquals(DEFAULT_VALUE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        LOGGER.debug(field.getObjectProps().toString());

    }

    /**
     * Checks {@link SnowflakeAvroRegistry#sqlType2Avro(int, int, int, boolean, String, String, Object)}
     * returns the {@link org.apache.avro.Schema.Field} with logical TIMESTAMP type
     */
    @Test
    public void testSqlType2AvroTimestamp() throws Exception {
        final int dbtype = java.sql.Types.TIMESTAMP;

        Schema.Field field = snowflakeAvroRegistry
                .sqlType2Avro(size, scale, dbtype, nullable, FIELD_NAME, DB_COLUMN_NAME, DEFAULT_VALUE);

        LOGGER.debug("field: " + field.toString());

        Assert.assertEquals(FIELD_NAME, field.name());
        Assert.assertEquals(-1, field.pos());
        Assert.assertEquals(TALEND_EXPECTED_TIMESTAMP_PATTERN, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        Assert.assertEquals(java.sql.Types.TIMESTAMP, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        Assert.assertEquals(DB_COLUMN_NAME, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        Assert.assertEquals(DEFAULT_VALUE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        LOGGER.debug(field.getObjectProps().toString());

    }

    /**
     * This test shows, that we can use {@link Field#defaultVal()} even if it was set to null, or real data.
     */
    @Test
    public void testWrap() {
        Schema schema = SchemaBuilder.builder().record("record").fields().requiredString("column1").endRecord();

        Field field = snowflakeAvroRegistry.wrap("nullableRecord", schema, true, null);
        Assert.assertEquals("nullableRecord", field.name());
        Assert.assertNull(field.defaultVal());

        field = snowflakeAvroRegistry.wrap("nullableRecord", schema, false, null);
        Assert.assertNull(field.defaultVal());

        field = snowflakeAvroRegistry.wrap("nullableRecord", schema, true, "");
        Assert.assertEquals("", field.defaultVal());

        field = snowflakeAvroRegistry.wrap("nullableRecord", schema, false, 10);
        Assert.assertEquals(10, field.defaultVal());

    }

    @Test
    public void testGetConverterForDate() throws SQLException {
        Integer dayIntValue = 17_331;
        ResultSet rs = Mockito.mock(ResultSet.class);

    }
}