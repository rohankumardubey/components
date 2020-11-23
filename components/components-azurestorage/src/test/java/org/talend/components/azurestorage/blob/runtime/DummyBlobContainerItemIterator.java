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
package org.talend.components.azurestorage.blob.runtime;

import java.util.Iterator;
import java.util.List;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.models.BlobContainerItem;

public class DummyBlobContainerItemIterator implements Iterator<BlobContainerItem> {

    private Iterator<BlobContainerItem> it;

    public DummyBlobContainerItemIterator(List<BlobContainerItem> list) {
        super();
        this.it = list.stream().iterator();
    }

    @Override
    public boolean hasNext() {

        return it.hasNext();
    }

    @Override
    public BlobContainerItem next() {
        return it.next();
    }

}
