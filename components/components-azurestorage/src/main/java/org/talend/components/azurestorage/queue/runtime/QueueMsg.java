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
package org.talend.components.azurestorage.queue.runtime;

import java.time.OffsetDateTime;

import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;

public class QueueMsg {

    private QueueMessageItem msg;

    private PeekedMessageItem peekedMsg;

    private int timeToLiveInSeconds;

    private int initialVisibilityDelayInSeconds;

    /**
     * @param msg
     * @param timeToLiveInSeconds
     * @param initialVisibilityDelayInSeconds
     */
    public QueueMsg(QueueMessageItem msg, int timeToLiveInSeconds, int initialVisibilityDelayInSeconds) {
        super();
        this.msg = msg;
        this.timeToLiveInSeconds = timeToLiveInSeconds;
        this.initialVisibilityDelayInSeconds = initialVisibilityDelayInSeconds;
    }

    public QueueMsg(QueueMessageItem msg) {
        super();
        this.msg = msg;
    }

    public QueueMsg(PeekedMessageItem msg) {
        super();
        this.peekedMsg = msg;
    }

    public String getMessageId() {
        return msg != null ? msg.getMessageText() : peekedMsg.getMessageId();
    }

    public String getMessageText() {
        return msg != null ? msg.getMessageText() : peekedMsg.getMessageText();
    }

    public long getDequeueCount() {
        return msg != null ? msg.getDequeueCount() : peekedMsg.getDequeueCount();
    }

    public OffsetDateTime getExpirationTime() {
        return msg != null ? msg.getExpirationTime() : peekedMsg.getExpirationTime();
    }

    public OffsetDateTime getInsertionTime() {
        return msg != null ? msg.getInsertionTime() : peekedMsg.getInsertionTime();
    }

    public String getPopReceipt() {
        return msg != null ? msg.getPopReceipt() : null;
    }

    public OffsetDateTime getTimeNextVisible() {
        return msg != null ? msg.getTimeNextVisible() : null;
    }

    public QueueMessageItem getMsg() {
        return msg;
    }

    public int getTimeToLiveInSeconds() {
        return timeToLiveInSeconds;
    }

    public int getInitialVisibilityDelayInSeconds() {
        return initialVisibilityDelayInSeconds;
    }

}
