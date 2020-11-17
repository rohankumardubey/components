// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azurestorage.blob;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collections;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class encapsulate and provide azure storage blob services
 */
public class AzureStorageBlobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageBlobService.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageBlobService.class);

    private AzureConnection connection;

    /**
     * @param connection
     */
    public AzureStorageBlobService(final AzureConnection connection) {
        super();
        this.connection = connection;
    }

    private BlobContainerClient getContainerReference(String container) {
        return connection.getBlobServiceClient().getBlobContainerClient(container);
    }

    /**
     * This method create an azure container if it doesn't exist and set it access policy
     *
     * @param containerName : the name of the container to be created
     *
     * @return true if the container was created, false otherwise
     */
    public boolean createContainerIfNotExist(final String containerName, final PublicAccessType accessType)
            throws BlobStorageException {
        final BlobContainerClient container = getContainerReference(containerName);

        boolean containerCreated;
        try {
            container.createWithResponse(Collections.emptyMap(), accessType, null, AzureStorageUtils
                    .getTalendOperationContext());
            containerCreated = true;
        } catch (BlobStorageException e) {
            if (!e.getErrorCode().equals(BlobErrorCode.CONTAINER_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn(messages.getMessage("error.CONTAINER_BEING_DELETED", containerName));
            // wait 40 seconds (min is 30s) before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-container
            try {
                Thread.sleep(40000);
            } catch (InterruptedException eint) {
                LOGGER.error(messages.getMessage("error.InterruptedException"));
                throw new ComponentException(eint);
            }
            try {
                container.createWithResponse(Collections.emptyMap(), accessType, null, AzureStorageUtils
                        .getTalendOperationContext());
                containerCreated = true;
                LOGGER.debug(messages.getMessage("debug.ContainerCreated", containerName));
            } catch (BlobStorageException exception) {
                throw (exception);
            }
        }

        return containerCreated;
    }

    /**
     * This method delete the container if exist
     */
    public boolean deleteContainerIfExist(final String containerName) throws BlobStorageException {
        final BlobContainerClient BlobContainerItem = getContainerReference(containerName);
        BlobContainerItem.delete();
        //FIXME
        return true;
    }

    /**
     * @return true if the a container exist with the given name, false otherwise
     */
    public boolean containerExist(final String containerName) throws BlobStorageException {
        final BlobContainerClient container = getContainerReference(containerName);
        return container.existsWithResponse(Duration.ofSeconds(10), AzureStorageUtils.getTalendOperationContext())
                .getValue();
    }

    public Iterable<BlobContainerItem> listContainers() {
        final BlobServiceClient blobClient = connection.getBlobServiceClient();
        return Iterable.class.cast(blobClient.listBlobContainers().stream().iterator());
    }

    public Iterable<BlobItem> listBlobs(final String containerName, final String prefix, final boolean useFlatBlobListing)
            throws BlobStorageException {
        final BlobContainerClient container = getContainerReference(containerName);
        // FIXME
        // return container.listBlobs(prefix, useFlatBlobListing, EnumSet.noneOf(BlobListingDetails.class), null,                                      AzureStorageUtils.getTalendOperationContext());
        return container.listBlobs();


    }

    public boolean deleteBlobBlockIfExist(final BlobItem block) throws BlobStorageException {
        //final BlobContainerClient container = getContainerReference(block.getProperties().getconontainerName);
        // FIXME
        //  return container.                 block.deleteIfExists(DeleteSnapshotsOption.NONE, null, null, AzureStorageUtils.getTalendOperationContext());
        return true;
    }

    public void download(final BlobItem blob, final OutputStream outStream) throws BlobStorageException {
        // FIXME
        // blob.download(outStream, null, null, AzureStorageUtils.getTalendOperationContext());
    }

    public void upload(final String containerName, final String blobName, final InputStream sourceStream, final long length)
            throws BlobStorageException {
        final BlobContainerClient container = getContainerReference(containerName);
        // FIXME
        //  BlockBlobItem blob = container.getBlockBlobReference(blobName);blob.upload(sourceStream, length, null, null, AzureStorageUtils.getTalendOperationContext());
    }

}
