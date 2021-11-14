package org.talend.components.azure.dlsgen2.blob.runtime;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.components.azure.dlsgen2.blob.runtime.readers.BlobReader;
import org.talend.components.azure.dlsgen2.blob.runtime.readers.BlobReader.BlobFileReaderFactory;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureDlsGen2BlobInputReader extends AbstractBoundedReader<IndexedRecord> {

    private final String path;

    private final AzureDlsGen2BlobDatasetProperties dataset;

    private int dataCount;

    private int limit = 10;

    private BlobContainerClient blobClient;

    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(80);

    private static final Logger LOG = LoggerFactory.getLogger(AzureDlsGen2BlobInputReader.class);

    private boolean startable;

    private boolean advanceable;

    private BlobReader reader;

    public AzureDlsGen2BlobInputReader(RuntimeContainer runtimeContainer, AzureDlsGen2BlobSource source,
                                       AzureDlsGen2BlobInputProperties properties) {
        super(source);
        dataset = properties.dataset;
        path = properties.dataset.path.getValue();
        blobClient = source.getBlobServiceClient(runtimeContainer).getBlobContainerClient(dataset.container.getValue());
    }

    @Override
    public boolean start() {
        initReader();
        startable = reader.hasNext();
        if (startable) {
            dataCount++;
        }
        return startable;
    }

    @Override
    public boolean advance() {
        advanceable = reader.hasNext();
        if (advanceable) {
            dataCount++;
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!(startable || advanceable)) {
            throw new NoSuchElementException();
        }
        return reader.readRecord();
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.successCount = dataCount;
        result.totalCount = dataCount;
        return result.toMap();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void initReader() {
        final PagedIterable<BlobItem> blobList = listBlobs(path);
        List<BlobItem> blobs = new ArrayList<>();
        try {
            for (BlobItem blob : blobList) {
                if (!isDirectoryBlob(blob)) {
                    blobs.add(blob);
                }
            }
            startable = !blobs.isEmpty();
        } catch (BlobStorageException e) {
            String msg = AzureDlsGen2Services.getMessage(e);
            LOG.error(msg);
            throw new ComponentException(new ValidationResult(ValidationResult.Result.ERROR, msg));
        }
        reader = BlobFileReaderFactory.getReader(dataset, blobClient, blobs);
    }

    public PagedIterable<BlobItem> listBlobs(final String prefix) throws BlobStorageException {
        ListBlobsOptions options = new ListBlobsOptions()
                .setPrefix(prefix)
                .setDetails(new BlobListDetails().setRetrieveMetadata(true));
        return blobClient.listBlobs(options, OPERATION_TIMEOUT);
    }

    public static boolean isDirectoryBlob(final BlobItem blob) {
        final Map<String, String> meta = Optional.ofNullable(blob.getMetadata()).orElse(Collections.emptyMap());
        return Boolean.valueOf(meta.getOrDefault("hdi_isfolder", "false"));
    }

}
