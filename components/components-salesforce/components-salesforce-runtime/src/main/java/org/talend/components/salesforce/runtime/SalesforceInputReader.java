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
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.soql.FieldDescription;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class SalesforceInputReader extends SalesforceReader<IndexedRecord> {

    private final Logger LOG = LoggerFactory.getLogger(SalesforceInputReader.class);

    private transient QueryResult inputResult;

    private transient SObject[] inputRecords;

    private transient int inputRecordsIndex;

    public SalesforceInputReader(RuntimeContainer container, SalesforceSource source, TSalesforceInputProperties props) {
        super(container, source);
        properties = props;
    }

    @Override
    protected Schema getSchema() throws IOException {
        TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
        if (querySchema == null) {
            querySchema = super.getSchema();
            if (inProperties.manualQuery.getValue()) {
                if (AvroUtils.isIncludeAllFields(properties.module.main.schema.getValue())) {
                    List<Schema.Field> copyFieldList = new ArrayList<>();

                    // logic almost the same as it is in GuessSchema (SalesforceSourseOrSink): TDI-48569
                    SoqlQuery query = SoqlQuery.getInstance();
                    query.init(inProperties.query.getValue());
                    for (FieldDescription fieldDescription : query.getFieldDescriptions()) {
                        final String simpleName = fieldDescription.getSimpleName();
                        final Schema.Field schemaField = Optional.ofNullable(querySchema.getField(simpleName))
                                .orElseGet(() -> querySchema.getFields().stream()
                                        .filter(it -> it.name().equalsIgnoreCase(simpleName))
                                        .findAny()
                                        .orElse(null));
                        if (schemaField == null) {
                            continue;
                        }

                        Schema.Field field = new Schema.Field(
                                fieldDescription.getFullName(), schemaField.schema(), schemaField.doc(), schemaField.defaultVal());
                        Map<String, Object> props = schemaField.getObjectProps();
                        for (String propName : props.keySet()) {
                            Object propValue = props.get(propName);
                            if (propValue != null) {
                                field.addProp(propName, propValue);
                            }
                        }
                        copyFieldList.add(field);
                    }
                    Map<String, Object> objectProps = querySchema.getObjectProps();
                    querySchema = Schema.createRecord(querySchema.getName(), querySchema.getDoc(), querySchema.getNamespace(),
                            querySchema.isError());
                    querySchema.getObjectProps().putAll(objectProps);
                    querySchema.setFields(copyFieldList);
                }
            }
            querySchema.addProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER, inProperties.columnNameDelimiter.getStringValue());
            querySchema.addProp(SalesforceSchemaConstants.VALUE_DELIMITER, inProperties.normalizeDelimiter.getStringValue());
        }
        addDateTimeUTCField(querySchema);
        return querySchema;
    }

    @Override
    public boolean start() throws IOException {
        try {
            inputResult = executeSalesforceQuery();
            if (inputResult.getSize() == 0) {
                return false;
            }
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            boolean start = inputRecords.length > 0;
            if (start) {
                dataCount++;
            }
            return start;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        inputRecordsIndex++;

        // Fast return conditions.
        if (inputRecordsIndex < inputRecords.length) {
            dataCount++;
            return true;
        }
        if (inputResult.isDone()) {
            return false;
        }

        try {
            // Get a new result set based on batch size
            inputResult = getConnection().queryMore(inputResult.getQueryLocator());
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            boolean advance = inputRecords != null && inputRecords.length > 0;
            if (advance) {
                // New result set available to retrieve
                dataCount++;
            }
            return advance;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }

    }

    public SObject getCurrentSObject() throws NoSuchElementException {
        return inputRecords[inputRecordsIndex];
    }

    protected QueryResult executeSalesforceQuery() throws IOException, ConnectionException {
        TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
        getConnection().setQueryOptions(inProperties.batchSize.getValue());
        if (inProperties.includeDeleted.getValue()) {
            return getConnection().queryAll(getQueryString(inProperties));
        } else {
            return getConnection().query(getQueryString(inProperties));
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((SObjectAdapterFactory) getFactory()).convertToAvro(getCurrentSObject());
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }
}
