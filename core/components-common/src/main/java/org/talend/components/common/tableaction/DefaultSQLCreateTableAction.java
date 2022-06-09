//============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.common.tableaction;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.config.jdbc.DbmsType;
import org.talend.components.common.config.jdbc.MappingType;
import org.talend.components.common.config.jdbc.TalendType;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class DefaultSQLCreateTableAction extends TableAction {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultSQLCreateTableAction.class);

    private String[] fullTableName;

    private Schema schema;

    private boolean drop;

    private boolean createIfNotExists;
    private boolean dropIfExists;

    public DefaultSQLCreateTableAction(final String[] fullTableName, final Schema schema, boolean createIfNotExists, boolean drop,
        boolean dropIfExists) {
        if (fullTableName == null || fullTableName.length < 1) {
            throw new InvalidParameterException("Table name can't be null or empty");
        }

        this.fullTableName = fullTableName;
        this.schema = schema;
        this.createIfNotExists = createIfNotExists;

        this.drop = drop || dropIfExists;
        this.dropIfExists = dropIfExists;
    }

    @Override
    public List<String> getQueries() throws Exception {
        List<String> queries = new ArrayList<>();

        if (drop) {
            queries.add(getDropTableQuery());
        }

        queries.add(getCreateTableQuery());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated SQL queries for create fullTableName:");
            for (String q : queries) {
                LOG.debug(q);
            }
        }

        return queries;
    }

    private String getDropTableQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getConfig().SQL_DROP_TABLE_PREFIX);
        sb.append(this.getConfig().SQL_DROP_TABLE);
        sb.append(" ");
        if(dropIfExists){
            sb.append(this.getConfig().SQL_DROP_TABLE_IF_EXISITS);
            sb.append(" ");
        }
        sb.append(buildFullTableName(fullTableName, this.getConfig().SQL_FULL_NAME_SEGMENT_SEP, true));
        sb.append(this.getConfig().SQL_DROP_TABLE_SUFFIX);

        return sb.toString();
    }

    private String getCreateTableQuery() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getConfig().SQL_CREATE_TABLE_PREFIX);
        sb.append(this.getConfig().SQL_CREATE_TABLE);
        sb.append(" ");

        if(createIfNotExists){
            sb.append(this.getConfig().SQL_CREATE_TABLE_IF_NOT_EXISTS);
            sb.append(" ");
        }

        sb.append(buildFullTableName(fullTableName, this.getConfig().SQL_FULL_NAME_SEGMENT_SEP, true));
        sb.append(" ");
        sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_ENCLOSURE_START);
        sb.append(buildColumns());
        sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_ENCLOSURE_END);
        sb.append(this.getConfig().SQL_CREATE_TABLE_SUFFIX);

        return sb.toString();
    }

    private final static String TALEND_TYPE = "di.column.talendType";
    private static final String LOGICAL_TIME_TYPE_AS = "LOGICAL_TIME_TYPE_AS";
    private static final String AS_TALEND_DATE = "TALEND_DATE";

    private StringBuilder buildColumns() {
        ConvertAvroTypeToSQL convertAvroToSQL = new ConvertAvroTypeToSQL(this.getConfig());
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        List<Schema.Field> fields = schema.getFields();
        List<String> keys = new ArrayList<>();
        for (Schema.Field f : fields) {
            if (!first) {
                sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_SEP);
            }

            String sDBName = f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            String sDBDefault = f.getProp(SchemaConstants.TALEND_COLUMN_DEFAULT);
            boolean sDBIsKey = Boolean.valueOf(f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY)).booleanValue();
            boolean sDBNullable = isNullable(f.schema());

            String name = sDBName == null ? f.name() : sDBName;
            if (sDBIsKey) {
                keys.add(name);
            }
            sb.append(escape(updateCaseIdentifier(name)));
            sb.append(" ");

            String sDBType = null;

            DbmsType dbmsType = null;

            if(this.getDbms()!=null) {
                //see studio MetadataToolAvroHelper.convertFromAvro method, here only follow it
                final Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());
                final LogicalType logicalType = LogicalTypes.fromSchemaIgnoreInvalid(basicSchema);

                String talendTypeName = f.getProp(TALEND_TYPE);
                if(talendTypeName==null || talendTypeName.isEmpty()) {
                    TalendType talendType = null;

                    if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {
                        talendType = TalendType.BOOLEAN;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {
                        talendType = TalendType.BYTE;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._bytes())) {
                        talendType = TalendType.BYTES;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._character())) {
                        talendType = TalendType.CHARACTER;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {
                        talendType = TalendType.DATE;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal())) {
                        talendType = TalendType.BIG_DECIMAL;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._double())) {
                        talendType = TalendType.DOUBLE;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._float())) {
                        talendType = TalendType.FLOAT;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int())) {
                        if (logicalType == LogicalTypes.date()) {
                            talendType = TalendType.DATE;
                        } else if (logicalType == LogicalTypes.timeMillis()) {
                            String logical_time_type_as = f.getProp(LOGICAL_TIME_TYPE_AS);
                            if (AS_TALEND_DATE.equals(logical_time_type_as)) {
                                talendType = TalendType.DATE;
                            } else {
                                talendType = TalendType.INTEGER;
                            }
                        } else {
                            // The logical type time maps to this as well
                            talendType = TalendType.INTEGER;
                        }
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
                        if (logicalType == LogicalTypes.timestampMillis()) {
                            talendType = TalendType.DATE;
                        } else {
                            talendType = TalendType.LONG;
                        }
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._short())) {
                        talendType = TalendType.SHORT;
                    } else if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
                        talendType = TalendType.STRING;
                    } else if (basicSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG) != null) {
                        talendType = TalendType.OBJECT;
                    }

                    talendTypeName = talendType.getName();
                }

                if(talendTypeName!=null && !talendTypeName.isEmpty()) {
                    MappingType<TalendType, DbmsType> mt = this.getDbms().getTalendMapping(talendTypeName);

                    dbmsType = mt.getDefaultType();

                    if(dbmsType!=null) {
                        sDBType = dbmsType.getName();
                    }

                    //TODO support default length, default precision, preBeforeLength in future, now only snowflake mapping use this, but not set default length or precision, or preBeforeLength,
                    //also here need to consider align with other db component, so not do them now
                }
            } else {
                //get db type from the component setting : "Custom DB Type"
                sDBType = this.getDbTypeMap().get(f.name());
            }

            //else get it from component schema
            if(isNullOrEmpty(sDBType)){
                sDBType = f.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE);
                //though this is a common class, but now only snowflake component use it
                //and we fill all snowflake types to the DB_TYPES in snowflake's implement : SnowflakeTableActionConfig
                //so if not find the mapping below, mean the db type name not exists in snowflake, is wrong, so not use it, see TDI-48045
                if (!isNullOrEmpty(sDBType)) {
                    if (getConfig().DB_TYPES.get(sDBType) == null) {
                        sDBType = null;
                    }
                }
            }

            if (isNullOrEmpty(sDBType)) {
                // If DB type not set, try to guess it
                sDBType = convertAvroToSQL.convertToSQLTypeString(f.schema());
            }
            sb.append(updateCaseIdentifier(sDBType));

            buildLengthPrecision(sb, f, sDBType, dbmsType);

            if(!sDBNullable){
                sb.append(this.getConfig().SQL_CREATE_TABLE_NOT_NULL);
            }

            if (this.getConfig().SQL_CREATE_TABLE_DEFAULT_ENABLED && !isNullOrEmpty(sDBDefault)) {
                sb.append(" ");
                sb.append(this.getConfig().SQL_CREATE_TABLE_DEFAULT);
                sb.append(" ");
                sb.append(sDBDefault);
            }

            first = false;
        }

        if (this.getConfig().SQL_CREATE_TABLE_CONSTRAINT_ENABLED && keys.size() > 0) {
            sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_SEP);
            sb.append(this.getConfig().SQL_CREATE_TABLE_CONSTRAINT);
            sb.append(" ");
            sb.append(escape(
                                this.getConfig().SQL_CREATE_TABLE_PRIMARY_KEY_PREFIX +
                                        buildFullTableName(fullTableName, this.getConfig().SQL_PRIMARY_KEY_FULL_NAME_SEGMENT_SEP, false)
                            )
                    );
            sb.append(" ");
            sb.append(this.getConfig().SQL_CREATE_TABLE_PRIMARY_KEY);
            sb.append(" ");
            sb.append(this.getConfig().SQL_CREATE_TABLE_PRIMARY_KEY_ENCLOSURE_START);

            first = true;
            for (String k : keys) {
                if (!first) {
                    sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_SEP);
                }
                sb.append(escape(updateCaseIdentifier(k)));

                first = false;
            }
            sb.append(this.getConfig().SQL_CREATE_TABLE_PRIMARY_KEY_ENCLOSURE_END);
        }

        return sb;
    }

    /**
     * Appends type length and precision value in SQL query builder
     * Component schema "length" parameter is used as length parameter for String types and as precision for numeric types
     * Component schema "precision" parameter is used as scale parameter for numeric types. Usually, "precision" is
     * ignored for String types
     *
     * @param sb StringBuilder, which constructs query
     * @param field Schema field
     * @param dbType Snowflake database type to be used
     */
    private void buildLengthPrecision(StringBuilder sb, Schema.Field field, String dbType, DbmsType dbmsTypeFromDBMappingFile) {
        String length = field.getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH);
        String precision = field.getProp(SchemaConstants.TALEND_COLUMN_PRECISION);

        final DbmsType dbmsType;
        if(dbmsTypeFromDBMappingFile == null) {
            //use static java code db mapping
            dbmsType = getConfig().DB_TYPES.get(dbType);
        } else {
            //use studio db mapping file
            dbmsType = dbmsTypeFromDBMappingFile;
        }

        boolean ignoreLength = false;
        boolean ignorePrecision = false;

        if (dbmsType != null) {
            ignoreLength = dbmsType.isIgnoreLength();
            ignorePrecision = dbmsType.isIgnorePrecision();
        }

        if (getConfig().SQL_CREATE_TABLE_LENGTH_ENABLED && !ignoreLength && !isNullOrEmpty(length)) {
            sb.append(getConfig().SQL_CREATE_TABLE_LENGTH_START);
            sb.append(length);
            if (getConfig().SQL_CREATE_TABLE_PRECISION_ENABLED && !ignorePrecision && !isNullOrEmpty(precision)) {
                sb.append(getConfig().SQL_CREATE_TABLE_PRECISION_SEP);
                sb.append(precision);
            }
            sb.append(getConfig().SQL_CREATE_TABLE_LENGTH_END);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        if (s == null) {
            return true;
        }

        return s.trim().isEmpty();
    }

    private boolean isNullable(Schema schema){
        Schema.Type type = schema.getType();
        if (type == Schema.Type.UNION) {
            for (Schema s : schema.getTypes()) {
                if (s.getType() == Schema.Type.NULL) {
                    return true;
                }
            }
        }
        return false;
    }
}
