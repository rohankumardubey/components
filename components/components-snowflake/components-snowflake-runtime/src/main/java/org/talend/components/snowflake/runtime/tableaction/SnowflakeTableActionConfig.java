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
package org.talend.components.snowflake.runtime.tableaction;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.config.jdbc.MappingFileLoader;
import org.talend.components.common.tableaction.TableActionConfig;
import org.talend.components.snowflake.SnowflakeTypes;

import java.io.File;
import java.net.URL;
import java.sql.Types;
import java.util.List;

public class SnowflakeTableActionConfig extends TableActionConfig {

    /**
     * Constant to represent fake SQL type in java - DI_DATE
     * It is used for type mapping during CREATE TABLE query generation
     * ConvertAvroTypeToSQL class will map DI DATE (java.util.Date) type to this constant
     * Then this constant will be used for mapping to type specified by user
     * This is a dirty hack for 7.1.1 quickfix to avoid components-common library modification
     */
    public static final int DI_DATE = 91404;

    private final static String MAPPING_FILE = "mapping_snowflake.xml";

    /**
     * URL of mapping directory, which contains type mapping files (e.g. mapping_Snowflake.xml)
     */
    private final URL mappingFilesDir;

    public SnowflakeTableActionConfig(URL mappingFilesDir, boolean isUpperCase){
        this.SQL_UPPERCASE_IDENTIFIER = isUpperCase;
        this.SQL_ESCAPE_ENABLED = true;
        this.SQL_DROP_TABLE_SUFFIX = " CASCADE";

        this.CONVERT_AVROTYPE_TO_SQLTYPE.put(Schema.Type.FLOAT, Types.FLOAT);
        this.CONVERT_AVROTYPE_TO_SQLTYPE.put(Schema.Type.DOUBLE, Types.FLOAT);

        this.CONVERT_LOGICALTYPE_TO_SQLTYPE.put(LogicalTypes.decimal(32), Types.FLOAT);

        this.CONVERT_JAVATYPE_TO_SQLTYPE.put("java.math.BigDecimal", Types.FLOAT);

        this.CONVERT_SQLTYPE_TO_ANOTHER_SQLTYPE.put(Types.BLOB, Types.BINARY);

        this.DB_TYPES = SnowflakeTypes.all();
        this.mappingFilesDir = mappingFilesDir;
    }

    /**
     * Loads and parses external mapping file.
     * Returns {@link Dbms} instance if mapping file was found and parsed successfully. Otherwise, null
     *
     * @return mapping file representation
     */
    protected Dbms getMapping() {
        File mappingFileFullPath = new File(mappingFilesDir.getFile(), MAPPING_FILE);
        if (!mappingFileFullPath.exists()) {
            System.out.println("mapping file wasn't found"); // TODO replace with logging
            return null;
        }
        MappingFileLoader fileLoader = new MappingFileLoader();
        List<Dbms> dbmsList = fileLoader.load(mappingFileFullPath);
        if (dbmsList == null) {
            return null;
        }
        return dbmsList.get(0);
    }

}
