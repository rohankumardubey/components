package org.talend.components.azure.dlsgen2.blob.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.azure.storage.blob.BlobServiceClient;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureDlsGen2BlobSource implements BoundedSource {

    public static final String SCHEMA_NAME = "AzureDlsGen2Blob";

    public static final String KEY_CONNECTION_PROPERTIES = "connection";

    private static final String SAS_PATTERN = "(http.?)?://(.*)\\.(blob|file|queue|table)(.*)/(.*)";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureDlsGen2BlobSource.class);

    private RuntimeContainer runtimeContainer;

    private AzureDlsGen2BlobInputProperties properties;

    private AzureDlsGen2BlobDatasetProperties dataset;

    private AzureDlsGen2BlobDatastoreProperties datastore;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        this.runtimeContainer = runtimeContainer;
        this.properties = (AzureDlsGen2BlobInputProperties) properties;
        dataset = this.properties.getDatasetProperties();
        datastore = dataset.getDatastoreProperties();
        return validateConnection(this.runtimeContainer);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        return validateConnection(runtimeContainer);
    }

    public ValidationResult validateConnection(RuntimeContainer container) {
        return validateConnection(getUsedConnection(runtimeContainer));
    }

    public static ValidationResult validateConnection(final AzureDlsGen2BlobDatastoreProperties connection) {
        String errorMessage = "";
        if (connection == null) { // check connection failure
            errorMessage = messages.getMessage("error.VacantConnection"); //$NON-NLS-1$
        } else {
            switch (connection.authenticationType.getValue()) {
            case SHAREDKEY:
                if (StringUtils.isEmpty(connection.accountName.getStringValue()) || StringUtils
                        .isEmpty(connection.accountKey.getStringValue())) {
                    errorMessage = messages.getMessage("error.EmptyKey"); //$NON-NLS-1$
                }
                break;
            case SAS:
                if (StringUtils.isEmpty(connection.sharedAccessSignature.getStringValue())) {
                    errorMessage = messages.getMessage("error.EmptySAS"); //$NON-NLS-1$
                }
                break;
            case ACTIVE_DIRECTORY_CLIENT_CREDENTIAL:
                if (StringUtils.isEmpty(connection.accountName.getValue()) || StringUtils
                        .isEmpty(connection.tenantId.getValue()) || StringUtils
                        .isEmpty(connection.clientId.getValue()) || StringUtils
                        .isEmpty(connection.clientSecret.getValue())) {
                    errorMessage = messages.getMessage("error.EmptyADProperties");
                }
                break;
            }
        }
        // Return result
        if (errorMessage.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        return new AzureDlsGen2BlobInputReader(adaptor, this, properties);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> schemas = new ArrayList<>();
        schemas.add(new SimpleNamedThing(SCHEMA_NAME, SCHEMA_NAME));
        return schemas;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return properties.getDatasetProperties().getSchema();
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    public BlobServiceClient getBlobServiceClient(RuntimeContainer runtimeContainer) {
        return getAzureConnection(runtimeContainer).getBlobServiceClient();
    }

    public AzureDlsGen2BlobDatastoreProperties getUsedConnection(RuntimeContainer container) {
        AzureDlsGen2BlobDatastoreProperties props = datastore;
        String refComponentId = props.getReferencedComponentId();
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                AzureDlsGen2BlobDatastoreProperties sharedConn = (AzureDlsGen2BlobDatastoreProperties) container
                        .getComponentData(refComponentId, KEY_CONNECTION_PROPERTIES);
                if (sharedConn != null) {
                    return sharedConn;
                }
            }
            // Design time
            props = datastore.getReferencedConnectionProperties();
        }
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES, props);
        }
        return props;
    }

    public AzureDlsGen2Services getAzureConnection(RuntimeContainer runtimeContainer) {
        AzureDlsGen2BlobDatastoreProperties conn = getUsedConnection(runtimeContainer);
        switch (conn.authenticationType.getValue()) {
        case SHAREDKEY:
            return AzureDlsGen2ServicesWithKey.builder()//
                    .accountName(conn.accountName.getValue())//
                    .accountKey(conn.accountKey.getValue())//
                    .build();
        case SAS:
            return AzureDlsGen2ServicesWithSas.builder()//
                    .accountName(conn.accountName.getValue())//
                    .sasToken(conn.sharedAccessSignature.getValue())//
                    .build();
        case ACTIVE_DIRECTORY_CLIENT_CREDENTIAL:
            return new AzureDlsGen2ServicesWithToken(conn.accountName.getValue(), conn.tenantId
                    .getValue(), conn.clientId.getValue(), conn.clientSecret.getValue());
        }
        throw new IllegalArgumentException("Invalid connection");
    }

}
