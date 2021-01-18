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
package org.talend.components.jdbc.tjdbcoutputbulkexec;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.VirtualComponentProperties;
import org.talend.components.jdbc.tjdbcbulkexec.TJDBCBulkExecProperties;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcoutputbulk.TJDBCOutputBulkProperties;
import org.talend.daikon.properties.presentation.Form;

public class TJDBCOutputBulkExecProperties extends TJDBCBulkExecProperties implements VirtualComponentProperties {

    public TJDBCOutputBulkProperties outputBulkProperties =
            new TJDBCOutputBulkProperties("outputBulkProperties");
    
    public TJDBCOutputBulkExecProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputBulkProperties.getForm(Form.REFERENCE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    public ComponentProperties getInputComponentProperties() {
        outputBulkProperties.init();

        outputBulkProperties.schema.schema.setStoredValue(main.schema.getStoredValue());
        outputBulkProperties.schema.schema.setValueEvaluator(main.schema.getValueEvaluator());

        outputBulkProperties.bulkFilePath.setStoredValue(bulkFilePath.getStoredValue());
        outputBulkProperties.bulkFilePath.copyTaggedValues(bulkFilePath);
        outputBulkProperties.bulkFilePath.setValueEvaluator(bulkFilePath.getValueEvaluator());

        // we need to pass also the possible values, only way from the studio to know it comes from a combo box (need to
        // add quotes for generation)
        //TODO
        for (Form form : outputBulkProperties.getForms()) {
            outputBulkProperties.refreshLayout(form);
        }
        return outputBulkProperties;
    }

    @Override
    public ComponentProperties getOutputComponentProperties() {
        TJDBCBulkExecProperties bulkExecProperties = new TJDBCBulkExecProperties("bulkExecProperties");

        bulkExecProperties.init();
        bulkExecProperties.copyValuesFrom(this, true, true);

        // we need to pass also the possible values, only way from the studio to know it comes from a combo box (need to
        // add quotes for generation)
        //TODO

        // Seems that properties copy can't copy the reference properties
        String refComponentIdValue = this.referencedComponent.componentInstanceId.getStringValue();
        boolean isUseExistConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TJDBCConnectionDefinition.COMPONENT_NAME);
        if (isUseExistConnection) {
            bulkExecProperties.referencedComponent
                    .setReference(this.referencedComponent.getReference());
        }

        for (Form form : bulkExecProperties.getForms()) {
            bulkExecProperties.refreshLayout(form);
        }
        return bulkExecProperties;
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return Collections.singleton(MAIN_CONNECTOR);
    }
}
