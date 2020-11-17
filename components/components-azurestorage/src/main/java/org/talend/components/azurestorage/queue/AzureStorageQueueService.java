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
package org.talend.components.azurestorage.queue;

import java.time.Duration;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class encapsulate and provide azure storage blob services
 */
public class AzureStorageQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueService.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueService.class);

    private AzureConnection connection;

    /**
     * @param connection
     */
    public AzureStorageQueueService(final AzureConnection connection) {
        super();
        this.connection = connection;
    }

    private QueueClient getQueueClient(final String queue) {
        return connection.getQueueServiceClient().getQueueClient(queue);
    }

    /**
     * This method create a queue if it doesn't exist
     */
    public boolean createQueueIfNotExists(String queueName) throws QueueStorageException {
        final QueueClient queueRef = getQueueClient(queueName);
        boolean creationResult;
        try {
            queueRef.create();
            creationResult = true;
        } catch (QueueStorageException e) {
            if (!e.getErrorCode().equals(QueueErrorCode.QUEUE_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn(messages.getMessage("error.QueueDeleted", queueName));
            // Documentation doesn't specify how many seconds at least to wait.
            // 40 seconds before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-queue3
            try {
                Thread.sleep(40000);
            } catch (InterruptedException eint) {
                throw new RuntimeException(messages.getMessage("error.InterruptedException"));
            }
            queueRef.createWithResponse(null, Duration.ofSeconds(30), Context.NONE);
            creationResult = true;
            LOGGER.debug(messages.getMessage("debug.QueueCreated", queueName));
        }

        return creationResult;
    }

    public void deleteQueueIfExists(String queueName) throws QueueStorageException {
        connection.getQueueServiceClient().deleteQueue(queueName);
    }

    public Iterable<PeekedMessageItem> peekMessages(String queueName, int numberOfMessages) throws QueueStorageException {
        return getQueueClient(queueName)
                .peekMessages(numberOfMessages, null, AzureStorageUtils.getTalendOperationContext());
    }

    public Iterable<QueueMessageItem> retrieveMessages(String queueName, int numberOfMessages) throws QueueStorageException {
        return retrieveMessages(queueName, numberOfMessages, 30);
    }

    public Iterable<QueueMessageItem> retrieveMessages(String queueName, int numberOfMessages, int visibilityTimeoutInSeconds) throws QueueStorageException {
        return getQueueClient(queueName)
                .receiveMessages(numberOfMessages, Duration
                        .ofSeconds(visibilityTimeoutInSeconds), null, AzureStorageUtils
                                         .getTalendOperationContext());
    }

    public void deleteMessage(String queueName, QueueMessageItem message) throws QueueStorageException {
        getQueueClient(queueName).deleteMessage(message.getMessageId(), message.getPopReceipt());
    }

    public Iterable<QueueItem> listQueues() throws QueueStorageException {
        return connection.getQueueServiceClient().listQueues();
    }

    public long getApproximateMessageCount(String queueName) throws QueueStorageException {
        return getQueueClient(queueName).getProperties().getApproximateMessagesCount();
    }

    public void clear(String queueName) throws QueueStorageException {
        getQueueClient(queueName)
                .clearMessagesWithResponse(Duration.ofSeconds(30), AzureStorageUtils.getTalendOperationContext());
    }

}
