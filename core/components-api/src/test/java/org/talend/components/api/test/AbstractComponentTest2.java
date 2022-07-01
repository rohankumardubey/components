// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import static org.junit.Assert.assertNotNull;

/**
 * base class for generic Integration tests that will perform I18N checks for all definitions and properties as well as image
 * checks.
 */
public abstract class AbstractComponentTest2 {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    // for benchmarking the apis, one suggestion is to use http://openjdk.java.net/projects/code-tools/jmh/.

    public abstract DefinitionRegistryService getDefinitionRegistry();

    @Test
    public void testAlli18n() {
        ComponentTestUtils.assertReturnProperties18nAreSet(getDefinitionRegistry(), errorCollector);
    }

    @Ignore
    @Test
    public void testAllImages() {
        ComponentTestUtils.assertAllComponentImagesAreSet(getDefinitionRegistry());
    }

    public static Map<String, Object> getConsolidatedResults(Result result, Writer writer) {
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writer.getWriteOperation().finalize(results, null);
        return resultMap;
    }

    /** @deprecated  */
    public void assertComponentIsRegistered(String definitionName) {
        Definition definition =
                this.getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(definitionName);
        assertNotNull("Could not find the definition [" + definitionName + "], please check the registered definitions",
                definition);
    }

    public void assertComponentIsRegistered(Class<? extends Definition> requestClass, String definitionName,
            Class<? extends Definition> definitionClass) {
        MatcherAssert.assertThat("Could not find the definition [" + definitionName + ", " + definitionClass + "]",
                this.getDefinitionRegistry().getDefinitionsMapByType(requestClass),
                Matchers.hasEntry(Matchers.is(definitionName), Matchers.instanceOf(definitionClass)));
    }
}
