// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.tjdbcoutputbulk;

import org.talend.components.common.BulkFileProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.BulkModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCOutputBulkProperties extends BulkFileProperties implements RuntimeSettingProvider {

    public BulkModule bulkModule = new BulkModule("bulkModule");

    public Property<Boolean> includeHeader = PropertyFactory.newBoolean("includeHeader");
    
    public TJDBCOutputBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form refForm = CommonUtils.addForm(this, Form.REFERENCE);
        refForm.addRow(append);
        
        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(bulkModule.getForm(Form.MAIN));
        advancedForm.addRow(includeHeader);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        
        if(form.getName().equals(Form.ADVANCED)) {
            Form bulkModuleForm = form.getChildForm(bulkModule.getName());
            if (bulkModuleForm != null) {
                bulkModuleForm.getWidget(bulkModule.escapeChar.getName()).setHidden(!bulkModule.setEscapeChar.getValue());
                bulkModuleForm.getWidget(bulkModule.textEnclosure.getName()).setHidden(!bulkModule.setTextEnclosure.getValue());
                bulkModuleForm.getWidget(bulkModule.nullValue.getName()).setHidden(!bulkModule.setNullValue.getValue());
            }
        }
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();
        //TODO check if need it
        setting.setBulkFile(this.bulkFilePath.getValue());
        return setting;
    }

}
