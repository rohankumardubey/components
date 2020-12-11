package org.talend.components.azure.dlsgen2.blob.source;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;

/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */public class AzureDlsGen2BlobInputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private AzureDlsGen2BlobInputProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new AzureDlsGen2BlobInputProperties("test");
        properties.init();
        properties.setupProperties();
        properties.setupLayout();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }
}