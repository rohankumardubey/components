package org.talend.components.azure.dlsgen2.blob.runtime;

import static org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobSource.validateConnection;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class AzureDlsGen2BlobDatasetRuntime implements DatasetRuntime<AzureDlsGen2BlobDatasetProperties> {

    private AzureDlsGen2BlobDatasetProperties dataset;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, AzureDlsGen2BlobDatasetProperties properties) {
        this.container = container;
        dataset = properties;
        return validateConnection(dataset.getDatastoreProperties());
    }

    @Override
    public Schema getSchema() {
        final Schema[] schema = new Schema[1];
        getSample(1, new Consumer<IndexedRecord>() {
            @Override
            public void accept(IndexedRecord in) {
                schema[0] = in.getSchema();
            }
        });
        return schema[0];
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        AzureDlsGen2BlobInputProperties properties = new AzureDlsGen2BlobInputProperties("sample");
        properties.setDatasetProperties(dataset);
        AzureDlsGen2BlobInputReader reader = (AzureDlsGen2BlobInputReader) createDataSource(properties)
                .createReader(container);
        reader.setLimit(limit);
        ReaderDataProvider<IndexedRecord> provider = new ReaderDataProvider<IndexedRecord>(reader, limit, consumer);
        provider.retrieveData();
    }

    public AzureDlsGen2BlobSource createDataSource(AzureDlsGen2BlobInputProperties properties) {
        AzureDlsGen2BlobSource ds = new AzureDlsGen2BlobSource();
        ds.initialize(container, properties);
        ds.validate(container);
        return ds;
    }
}
