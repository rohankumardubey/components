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
package org.talend.components.jdbc.module;

import org.talend.components.jdbc.CommonUtils;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * common advanced JDBC bulk information properties
 *
 */
public class BulkModule extends PropertiesImpl {

    //parameters which both have meaning for outputbulk and bulk exec components
    public Property<String> rowSeparator = PropertyFactory.newProperty("rowSeparator").setRequired();
    public Property<String> fieldSeparator = PropertyFactory.newProperty("fieldSeparator").setRequired();
    
    public Property<Boolean> setEscapeChar = PropertyFactory.newBoolean("setEscapeChar");
    public Property<String> escapeChar = PropertyFactory.newString("escapeChar");
    
    public Property<Boolean> setTextEnclosure = PropertyFactory.newBoolean("setTextEnclosure");
    public Property<String> textEnclosure = PropertyFactory.newString("textEnclosure");
    
    public Property<Boolean> setNullValue = PropertyFactory.newBoolean("setNullValue");
    public Property<String> nullValue = PropertyFactory.newString("nullValue");
    
    public BulkModule(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        
        //this is also the value which show in ui, but we use javajet to pass the value back to tcompv0, the value need to respect the java string rule
        rowSeparator.setValue("\\n");
        fieldSeparator.setValue(";");
        
        escapeChar.setValue("\"");
        textEnclosure.setValue("\"");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form form = CommonUtils.addForm(this, Form.MAIN);
        form.addRow(rowSeparator);
        form.addColumn(fieldSeparator);
        
        form.addRow(setEscapeChar);
        form.addColumn(escapeChar);
        
        form.addRow(setTextEnclosure);
        form.addColumn(textEnclosure);
        
        form.addRow(setNullValue);
        form.addColumn(nullValue);
    }
    
    @Override
    public void refreshLayout(Form form) {
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(escapeChar.getName()).setHidden(!setEscapeChar.getValue());
            form.getWidget(textEnclosure.getName()).setHidden(!setTextEnclosure.getValue());
            form.getWidget(nullValue.getName()).setHidden(!setNullValue.getValue());
        }
    }

    public void afterSetEscapeChar() {
        refreshLayout(getForm(Form.MAIN));
    }
    
    public void afterSetTextEnclosure() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSetNullValue() {
        refreshLayout(getForm(Form.MAIN));
    }

}
