package org.talend.components.azurestorage.queue.runtime;

import com.azure.storage.queue.models.QueueStorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeuedelete.TAzureStorageQueueDeleteProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;


public class AzureStorageQueueDeleteRuntime extends AzureStorageQueueRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 2804185569829620114L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueDeleteRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueDeleteRuntime.class);

    private boolean dieOnError;

    public AzureStorageQueueService queueService;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult vr = super.initialize(runtimeContainer, properties);
        if (!ValidationResult.OK.getStatus().equals(vr.getStatus())) {
            return vr;
        }
        this.dieOnError = ((TAzureStorageQueueDeleteProperties) properties).dieOnError.getValue();
        queueService = new AzureStorageQueueService(getAzureConnection(runtimeContainer));

        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        deleteAzureQueue(container);
        setReturnValues(container);
    }

    private boolean deleteAzureQueue(RuntimeContainer container) {
        Boolean deleteResult = false;
        try {
            queueService.deleteQueueIfExists(queueName);
            deleteResult = true;
            LOGGER.debug(messages.getMessage("debug.QueueDeleted", queueName));
        } catch (QueueStorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError) {
                throw new ComponentException(e);
            }
        }
        return deleteResult;
    }

    private void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        String returnQueueName = AzureStorageUtils
                .getStudioNameFromProperty(AzureStorageQueueDefinition.RETURN_QUEUE_NAME);
        container.setComponentData(componentId, returnQueueName, queueName);
    }

}
