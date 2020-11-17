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

import java.util.Iterator;
import java.util.List;

import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueItem;

public class DummyQueueItemIterator implements Iterator<QueueItem> {

    private Iterator<QueueItem> it;

    public DummyQueueItemIterator(List<QueueItem> list) {
        super();
        this.it = list.iterator();
    }

    @Override
    public boolean hasNext() {

        return it.hasNext();
    }

    @Override
    public QueueItem next() {
        return it.next();
    }
}
