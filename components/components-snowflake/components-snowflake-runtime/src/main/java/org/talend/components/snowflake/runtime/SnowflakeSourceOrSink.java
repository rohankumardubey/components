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
/**
 *
 */
package org.talend.components.snowflake.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCTableMetadata;
import org.talend.components.common.tableaction.TableAction;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.SnowflakeGuessSchemaProperties;
import org.talend.components.snowflake.SnowflakeOauthConnectionProperties.GrantType;
import org.talend.components.snowflake.SnowflakeProvideConnectionProperties;
import org.talend.components.snowflake.SnowflakeRuntimeSourceOrSink;
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.tsnowflakeconnection.AuthenticationType;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class SnowflakeSourceOrSink extends SnowflakeRuntime implements SourceOrSink, SnowflakeRuntimeSourceOrSink {

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeSourceOrSink.class);

    private static final long serialVersionUID = 1L;

    private transient static final Logger LOG = LoggerFactory.getLogger(SnowflakeSourceOrSink.class);

    protected SnowflakeProvideConnectionProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SnowflakeProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            createConnection(container);
        } catch (IllegalArgumentException e) {
            ValidationResultMutable vr = new ValidationResultMutable();
            vr.setMessage(e.getCause() instanceof SQLException
                            ? e.getMessage().concat(SnowflakeConstants.INCORRECT_SNOWFLAKE_ACCOUNT_MESSAGE)
                            : e.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
        ValidationResultMutable vr = new ValidationResultMutable();
        vr.setStatus(Result.OK);
        vr.setMessage(SnowflakeConstants.CONNECTION_SUCCESSFUL_MESSAGE);
        return vr;
    }

    public static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResultMutable vr = new ValidationResultMutable();
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    @Override
    public ValidationResult validateConnection(SnowflakeProvideConnectionProperties properties) {
        // check if every required properties was specified
        ValidationResultMutable vr = validateConnectionProperties(properties);
        if (vr.getStatus() == Result.OK) {
            try {
                // Make sure we can get the schema names, as that tests that all of the connection parameters are really
                // OK
                getSchemaNames(null);
            } catch (Exception ex) {
                return exceptionToValidationResult(ex);
            }
            vr.setStatus(Result.OK);
            vr.setMessage(SnowflakeConstants.CONNECTION_SUCCESSFUL_MESSAGE);
        }
        return vr;
    }

    protected static ValidationResultMutable validateConnectionProperties(SnowflakeProvideConnectionProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable();
        vr.setStatus(Result.OK);
        SnowflakeConnectionProperties connectionProperties = properties.getConnectionProperties();
        StringBuilder missingProperties = new StringBuilder();
        if (StringUtils.isEmpty(connectionProperties.account.getValue())) {
            missingProperties.append("'Account', ");
        }
        if (AuthenticationType.BASIC == connectionProperties.authenticationType.getValue()
                && StringUtils.isEmpty(connectionProperties.userPassword.password.getValue())) {
            missingProperties.append("'Password', ");
        }
        if (AuthenticationType.OAUTH != connectionProperties.authenticationType.getValue()
                && StringUtils.isEmpty(connectionProperties.userPassword.userId.getValue())) {
            missingProperties.append("'UserID', ");
        }
        if (AuthenticationType.OAUTH == connectionProperties.authenticationType.getValue()
                && StringUtils.isEmpty(connectionProperties.oauthProperties.oauthTokenEndpoint.getValue())) {
            missingProperties.append("'OAuthTokenEndpoint', ");
        }
        if (AuthenticationType.OAUTH == connectionProperties.authenticationType.getValue()
                && StringUtils.isEmpty(connectionProperties.oauthProperties.clientId.getValue())) {
            missingProperties.append("'ClientID', ");
        }
        if (AuthenticationType.OAUTH == connectionProperties.authenticationType.getValue()
                && StringUtils.isEmpty(connectionProperties.oauthProperties.clientSecret.getValue())) {
            missingProperties.append("'ClientSecret', ");
        }
        if (AuthenticationType.OAUTH == connectionProperties.authenticationType.getValue()
                && GrantType.PASSWORD == connectionProperties.oauthProperties.grantType.getValue()
                && StringUtils.isEmpty(connectionProperties.oauthProperties.oauthUserName.getValue())) {
            missingProperties.append("'OAuth username', ");
        }
        if (AuthenticationType.OAUTH == connectionProperties.authenticationType.getValue()
                && GrantType.PASSWORD == connectionProperties.oauthProperties.grantType.getValue()
                && StringUtils.isEmpty(connectionProperties.oauthProperties.oauthPassword.getValue())) {
            missingProperties.append("'OAuth password', ");
        }
        if (StringUtils.isEmpty(connectionProperties.schemaName.getValue())) {
            missingProperties.append("'Schema', ");
        }
        if (StringUtils.isEmpty(connectionProperties.db.getValue())) {
            missingProperties.append("'Database', ");
        }
        if (connectionProperties.useCustomRegion.getValue()
                && StringUtils.isEmpty(connectionProperties.customRegionID.getValue())) {
            missingProperties.append("'CustomSnowflakeRegionID', ");
        }
        if (!missingProperties.toString().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(i18nMessages.getMessage("error.requiredPropertyIsEmpty",
                    missingProperties.toString().substring(0, missingProperties.length() - 2)));
        }
        return vr;
    }

    public SnowflakeConnectionProperties getEffectiveConnectionProperties(RuntimeContainer container) {
        SnowflakeConnectionProperties connProps = properties.getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                return (SnowflakeConnectionProperties) container.getComponentData(refComponentId,
                        SnowflakeRuntime.KEY_CONNECTION_PROPERTIES);
            }
            // Design time
            return connProps.getReferencedConnectionProperties();
        }
        return connProps;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        try (Connection conn = createNewConnection(container)) {
            return getSchemaNames(container, conn);
        } catch (SQLException sqle) {
            throw new IOException(sqle);
        }
    }

    protected String getCatalog(SnowflakeConnectionProperties connProps) {
        return connProps.db.getStringValue();
    }

    protected String getDbSchema(SnowflakeConnectionProperties connProps) {
        return connProps.schemaName.getStringValue();
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container, Connection connection) throws IOException {
        // Returns the list with a table names (for the wh, db and schema)
        List<NamedThing> returnList = new ArrayList<>();
        SnowflakeConnectionProperties connProps = getEffectiveConnectionProperties(container);
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            // Fetch all tables in the db and schema provided
            String[] types = { "TABLE", "VIEW" };
            ResultSet resultIter = metaData.getTables(getCatalog(connProps), getDbSchema(connProps), null, types);
            String tableName = null;
            while (resultIter.next()) {
                tableName = resultIter.getString("TABLE_NAME");
                returnList.add(new SimpleNamedThing(tableName, tableName));
            }
        } catch (SQLException se) {
            throw new IOException(i18nMessages.getMessage("error.searchingTable", getCatalog(connProps), getDbSchema(connProps),
                    se.getMessage()), se);
        }
        return returnList;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        try (Connection conn = createNewConnection(container)) {
            return getSchema(container, conn, schemaName);
        } catch (SQLException sqle) {
            throw new IOException(sqle);
        }
    }

    protected Schema getRuntimeSchema(SchemaResolver resolver) throws IOException {
        return getRuntimeSchema(resolver, TableAction.TableActionEnum.NONE);
    }

    protected Schema getRuntimeSchema(SchemaResolver resolver, TableAction.TableActionEnum tableAction) throws IOException {
        SnowflakeConnectionTableProperties connectionTableProperties = ((SnowflakeConnectionTableProperties) properties);
        Schema schema = connectionTableProperties.getSchema();

        // Don't retrieve schema from database if there is a table action that will create the table
        if (AvroUtils.isIncludeAllFields(schema) && tableAction == TableAction.TableActionEnum.NONE) {
            schema = resolver.getSchema();
        }
        return schema;
    }

    public static Schema getSchemaFromQuery(RuntimeContainer container, SnowflakeProvideConnectionProperties properties)
            throws IOException {
        SnowflakeSourceOrSink ss = new SnowflakeSourceOrSink();
        ss.initialize(container, (ComponentProperties) properties);
        return ss.getSchemaFromQuery(container);
    }

    @Override
    public Schema getSchemaFromQuery(RuntimeContainer container) throws IOException {
        ResultSetMetaData metadata = null;
        try (Connection connection = createNewConnection(container);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(((SnowflakeGuessSchemaProperties)properties).getQuery())) {
            metadata = rs.getMetaData();
            return getSnowflakeAvroRegistry().inferSchema(metadata);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    protected SnowflakeAvroRegistry getSnowflakeAvroRegistry() {
        return SnowflakeAvroRegistry.get();
    }

    @Override
    public Schema getSchema(RuntimeContainer container, Connection connection, String tableName) throws IOException {
        Schema tableSchema = null;

        SnowflakeConnectionProperties connProps = getEffectiveConnectionProperties(container);
        try {
            JDBCTableMetadata tableMetadata = new JDBCTableMetadata();
            tableMetadata.setDatabaseMetaData(connection.getMetaData()).setCatalog(getCatalog(connProps))
                    .setDbSchema(escapeUnderscores(getDbSchema(connProps))).setTablename(escapeUnderscores(tableName));
            tableSchema = getSnowflakeAvroRegistry().inferSchema(tableMetadata);
            if (tableSchema == null) {
                throw new IOException(i18nMessages.getMessage("error.tableNotFound", tableName));
            }
        } catch (SQLException se) {
            throw new IOException(se);
        }

        return tableSchema;

    }

    private String escapeUnderscores(String value) {
        return value.replace("_", "\\_");
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

}
