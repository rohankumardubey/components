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
package org.talend.components.jira.runtime.writer;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.jira.connection.JiraResponse;
import org.talend.components.jira.runtime.JiraWriteOperation;

/**
 * {@link JiraWriter} which deletes incoming data from Jira server
 */
public class JiraDeleteWriter extends JiraWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JiraDeleteWriter.class);

    /**
     * Stores http parameters which are shared between requests
     */
    private final Map<String, Object> sharedParameters;

    /**
     * Schema retrieved from incoming data
     */
    private Schema dataSchema;

    /**
     * Position of id field in record schema
     */
    private int idPos;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation Jira {@link WriteOperation} instance
     */
    public JiraDeleteWriter(JiraWriteOperation writeOperation) {
        super(writeOperation);
        this.sharedParameters = createSharedParameters();
    }

    /**
     * Removes input data resources from Jira server <br>
     * Method should be called only after {@link JiraDeleteWriter#open(String)}
     * 
     * @param datum input data
     */
    @Override
    public void write(Object datum) throws IOException {
        if (!opened) {
            throw new IOException(MESSAGES.getMessage("error.writerNotOpened"));
        }
        result.totalCount++;
        if (datum == null) {
            return;
        }
        IndexedRecord record = getFactory(datum).convertToAvro(datum);
        if (dataSchema == null) {
            dataSchema = record.getSchema();
            Field idField = dataSchema.getField("id");
            if (idField == null) {
                throw new IOException(MESSAGES.getMessage("error.schemaNotContainId"));
            }
            idPos = idField.pos();
        }

        String id = (String) record.get(idPos);
        String resourceToDelete = resource + "/" + id;
        JiraResponse response = getConnection().delete(resourceToDelete, sharedParameters);
        handleResponse(response, id, record);
    }

    /**
     * Handles response according status code See Jira REST documentation for details
     * 
     * @param response Jira response, which contains status code and body
     * @param resourceToDelete path of resource to be deleted
     * @param record current {@link IndexedRecord}
     * @throws IOException IOException in case of status code is not 200 OK or 204 NO CONTENT
     */
    private void handleResponse(JiraResponse response, String resourceToDelete, IndexedRecord record) throws IOException {
        int statusCode = response.getStatusCode();
        String responseError = response.getBody();
        switch (statusCode) {
        case SC_OK:
        case SC_NO_CONTENT: {
            LOG.debug("Successfully removed {}", resourceToDelete);
            result.successCount++;
            break;
        }
        case SC_BAD_REQUEST: {
            LOG.debug("Error occured during deletion {}", resourceToDelete);
            throw createRejectException("error.duringDeletion", resourceToDelete, responseError);
        }
        case SC_UNAUTHORIZED: {
            LOG.debug("User is not authenticated. {} wasn't deleted", resourceToDelete);
            throw createRejectException("error.unauthorizedDelete", resourceToDelete, responseError);
        }
        case SC_FORBIDDEN: {
            LOG.debug("User does not have permission to delete {}", resourceToDelete);
            throw createRejectException("error.forbiddenDelete", resourceToDelete, responseError);
        }
        case SC_NOT_FOUND: {
            LOG.debug("{} wasn't deleted, because it doesn't exist", resourceToDelete);
            throw createRejectException("error.notFoundDelete", resourceToDelete, responseError);
        }
        default: {
            LOG.debug("Unexpected status code");
        }
        }
    }

    /**
     * Creates and returns map with shared http query parameters
     * 
     * @return shared http parameters
     */
    private Map<String, Object> createSharedParameters() {
        Map<String, Object> sharedParameters = new HashMap<>();
        boolean deleteSubtasks = getWriteOperation().getSink().doDeleteSubtasks();
        if (deleteSubtasks) {
            sharedParameters.put("deleteSubtasks", true);
        }
        return sharedParameters;
    }

}
