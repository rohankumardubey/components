// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.tableaction;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.config.jdbc.DbmsType;
import org.talend.components.common.config.jdbc.MappingType;
import org.talend.components.common.config.jdbc.TalendType;
import org.talend.daikon.avro.SchemaConstants;

public class DefaultSQLCreateTableAction extends TableAction {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultSQLCreateTableAction.class);

    private String[] fullTableName;

    private Schema schema;

    private boolean drop;

    private boolean createIfNotExists;
    private boolean dropIfExists;

    /**
     * Cached DB types mapping.
     * Some databases doesn't have/doesn't support mapping file, so this field may be not present.
     * null value for this field means that mapping file wasn't requested yet and should be requested.
     */
    private Optional<Dbms> dbms;

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

    private StringBuilder buildColumns() {

        StringBuilder sb = new StringBuilder();

        boolean first = true;
        List<Schema.Field> fields = schema.getFields();
        List<String> keys = new ArrayList<>();
        for (Schema.Field f : fields) {
            if (!first) {
                sb.append(this.getConfig().SQL_CREATE_TABLE_FIELD_SEP);
            }


            String sDBLength = f.getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH);
            String sDBName = f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            String sDBType = getDbType(f);
            String sDBDefault = f.getProp(SchemaConstants.TALEND_COLUMN_DEFAULT);
            String sDBPrecision = f.getProp(SchemaConstants.TALEND_COLUMN_PRECISION);
            boolean sDBIsKey = Boolean.valueOf(f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY)).booleanValue();
            boolean sDBNullable = isNullable(f.schema());

            String name = sDBName == null ? f.name() : sDBName;
            if (sDBIsKey) {
                keys.add(name);
            }
            sb.append(escape(updateCaseIdentifier(name)));
            sb.append(" ");


            String sDBType = this.getDbTypeMap().get(f.name());

            if(isNullOrEmpty(sDBType)){
                sDBType = f.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE);
            }

            if (isNullOrEmpty(sDBType)) {
                // If DB type not set, try to guess it
                sDBType = convertAvroToSQL.convertToSQLTypeString(f.schema());
            }
            sb.append(updateCaseIdentifier(sDBType));


            buildLengthPrecision(sb, f, sDBType);

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

    private String getDbType(Schema.Field field) {
        ConvertAvroTypeToSQL convertAvroToSQL = new ConvertAvroTypeToSQL(this.getConfig());
        // 1st priority is to use db type stored in schema
        // it doesn't work at the moment as schema editor for Snowflake doesn't have db type column
        String dbType = field.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE);

        // 2nd priority is to use mapping table in advanced settings (it works similarly to db type column in schema)
        if(isNullOrEmpty(dbType)){
            // if SchemaConstants.TALEND_COLUMN_DB_TYPE not set, use given map
            dbType = this.getDbTypeMap().get(field.name());
        }

        // 3rd priority is to use default type from mapping file
        if (getMapping().isPresent()) {
            Dbms mapping = getMapping().get();
            String typeName = field.getProp("di.column.talendType"); // TODO add this key to constants
            MappingType<TalendType, DbmsType> mappingType = mapping.getTalendMapping(typeName);
            dbType = mappingType.getDefaultType().getName();
        }

        // 4th priority is to use default hardcoded mapping
        if (isNullOrEmpty(dbType)) {
            // If DB type not set, try to guess it
            dbType = convertAvroToSQL.convertToSQLTypeString(field.schema());
        }
        return dbType;
    }

    /**
     * Parses DB types (xml) mapping file for the 1st time.
     * For next calls returns cached instance
     *
     * @return DB types mapping
     */
    private Optional<Dbms> getMapping() {
        // if dbms wasn't requested yet, request it
        if (dbms == null) {
            dbms = Optional.ofNullable(getConfig().getMapping());
        }
        // return cached dbms
        return dbms;
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
    private void buildLengthPrecision(StringBuilder sb, Schema.Field field, String dbType) {
        String length = field.getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH);
        String precision = field.getProp(SchemaConstants.TALEND_COLUMN_PRECISION);

        DbmsType dbmsType = getConfig().DB_TYPES.get(dbType);

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
