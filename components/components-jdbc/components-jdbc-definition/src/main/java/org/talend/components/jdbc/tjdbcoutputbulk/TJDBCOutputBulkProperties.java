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
package org.talend.components.jdbc.tjdbcoutputbulk;

import org.talend.components.common.BulkFileProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;

public class TJDBCOutputBulkProperties extends BulkFileProperties implements RuntimeSettingProvider {

    public TJDBCOutputBulkProperties(String name) {
        super(name);
    }

    //TODO add more ui parameters

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        //TODO use them later
        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        
        Form refForm = CommonUtils.addForm(this, Form.REFERENCE);
        refForm.addRow(append);
        
        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();
        //TODO check if need it
        setting.setBulkFile(this.bulkFilePath.getValue());
        return setting;
    }

}
