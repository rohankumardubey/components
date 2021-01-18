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
package org.talend.components.jdbc.runtime;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.common.runtime.BulkFileWriter;
import org.talend.components.jdbc.tjdbcoutputbulk.TJDBCOutputBulkProperties;

/**
 * Prepare Data Files for bulk execution
 */
final class JDBCBulkFileWriter extends BulkFileWriter {

    public JDBCBulkFileWriter(WriteOperation<Result> writeOperation, BulkFileProperties bulkProperties,
            RuntimeContainer adaptor) {
        super(writeOperation, bulkProperties, adaptor);
    }

    @Override
    public boolean needHeader() {
        return false;
    }

    @Override
    public List<String> getValues(Object datum) {
        IndexedRecord input = getFactory(datum).convertToAvro((IndexedRecord) datum);
        List<String> values = new ArrayList<String>();
        for (Field f : input.getSchema().getFields()) {
            if (input.get(f.pos()) == null) {
                //TODO check how to process null
                values.add("");
                //values.add("#N/A");
            } else {
                values.add(String.valueOf(input.get(f.pos())));
            }
        }
        return values;
    }

    protected TJDBCOutputBulkProperties getBulkProperties() {
        return (TJDBCOutputBulkProperties) bulkProperties;
    }
}
