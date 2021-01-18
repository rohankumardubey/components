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
package org.talend.components.jdbc.tjdbcbulkexec;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.JDBCTableSelectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TJDBCBulkExecProperties extends FixedConnectorsComponentProperties implements RuntimeSettingProvider {
	
	// main
    public ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TJDBCConnectionDefinition.COMPONENT_NAME);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");
    
    public JDBCTableSelectionModule tableSelection = new JDBCTableSelectionModule("tableSelection");

    public Property<String> bulkFilePath = newProperty("bulkFilePath");
    
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");
    
    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
        	//TODO remove it
            //updateOutputSchemas();
        }

    };

    public TJDBCBulkExecProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(tableSelection.getForm(Form.REFERENCE));
        
        mainForm.addRow(widget(bulkFilePath).setWidgetType(Widget.FILE_WIDGET_TYPE));

        //TODO
        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        //TODO
     }

    @Override
    public void setupProperties() {
        super.setupProperties();

        tableSelection.setConnection(this);

        connection.setNotRequired();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
    	return Collections.emptySet();
    }

    //TODO
    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        CommonUtils.setReferenceInfoAndConnectionInfo(setting, referencedComponent, connection);

        setting.setTablename(this.tableSelection.tablename.getValue());
        
        setting.setSchema(main.schema.getValue());
        
        setting.setBulkFile(this.bulkFilePath.getValue());

        return setting;
    }
}
