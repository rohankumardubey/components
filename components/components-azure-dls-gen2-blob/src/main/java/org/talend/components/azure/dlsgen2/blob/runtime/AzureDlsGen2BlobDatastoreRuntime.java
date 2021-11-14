package org.talend.components.azure.dlsgen2.blob.runtime;

import static org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobSource.validateConnection;

import java.util.Collections;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.properties.ValidationResult;

public class AzureDlsGen2BlobDatastoreRuntime implements DatastoreRuntime<AzureDlsGen2BlobDatastoreProperties> {

    protected AzureDlsGen2BlobDatastoreProperties datastore;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        AzureDlsGen2BlobSource source = new AzureDlsGen2BlobSource();
        AzureDlsGen2BlobInputProperties properties = new AzureDlsGen2BlobInputProperties("health");
        AzureDlsGen2BlobDatasetProperties dataset = new AzureDlsGen2BlobDatasetProperties("dataprep");
        dataset.setDatastoreProperties(datastore);
        properties.setDatasetProperties(dataset);
        source.initialize(container, properties);

        return Collections.singletonList(source.validate(container));
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, AzureDlsGen2BlobDatastoreProperties properties) {
        datastore = properties;
        return validateConnection(datastore);
    }
}
