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
package org.talend.components.api.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.junit.rules.ErrorCollector;
import org.talend.components.api.DaikonLegacyAssertions;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeUtil;

// import static org.hamcrest.Matchers.*;

public class ComponentTestUtils {

    public static Properties checkSerialize(Properties props, ErrorCollector errorCollector) {
        return DaikonLegacyAssertions.checkSerialize(props, errorCollector);
    }

    /**
     * check all properties of a component for i18n, check form i18n, check ComponentProperties title is i18n
     * 
     * @param componentService where to get all the components
     * @param errorCollector used to collect all errors at once. @see
     *            <a href="http://junit.org/apidocs/org/junit/rules/ErrorCollector.html">ErrorCollector</a>
     * @deprecated use {@link DaikonLegacyAssertions#assertAlli18nAreSetup(DefinitionRegistryService, ErrorCollector)} and
     *             {@link #assertReturnProperties18nAreSet(DefinitionRegistryService, ErrorCollector)}
     */
    @Deprecated
    static public void testAlli18n(ComponentService componentService, ErrorCollector errorCollector) {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = (ComponentProperties) PropertiesImpl.createNewInstance(cd.getPropertiesClass(), "root")
                    .init();
            // check all properties
            if (props != null) {
                checkAllI18N(props, errorCollector);
            } else {
                System.out.println("No properties to check fo I18n for :" + cd.getName());
            }
            // check component definition title
            errorCollector.checkThat(
                    "missing I18n property [" + cd.getTitle() + "] for definition [" + cd.getClass().getName() + "]",
                    cd.getTitle().contains("component."), is(false));
            // check return properties i18n
            checkAllPropertyI18n(cd.getReturnProperties(), cd, errorCollector);
        }
    }

    /**
     * check all properties of a component for i18n, check form i18n, check ComponentProperties title is i18n
     * 
     * @param componentService where to get all the components
     * @param errorCollector used to collect all errors at once. @see
     *            <a href="http://junit.org/apidocs/org/junit/rules/ErrorCollector.html">ErrorCollector</a>
     */
    static public void assertReturnProperties18nAreSet(DefinitionRegistryService definitionRegistry,
            ErrorCollector errorCollector) {
        Collection<ComponentDefinition> allComponents = definitionRegistry.getDefinitionsMapByType(ComponentDefinition.class)
                .values();
        for (ComponentDefinition cd : allComponents) {
            // check return properties i18n
            checkAllPropertyI18n(cd.getReturnProperties(), cd, errorCollector);
        }
    }

    public static void checkAllPropertyI18n(Property<?>[] propertyArray, Object parent, ErrorCollector errorCollector) {
        if (propertyArray != null) {
            for (Property<?> prop : propertyArray) {
                DaikonLegacyAssertions.chekProperty(errorCollector, prop, parent);
            }
        } // else no property to check so ignore.
    }

    static public void checkAllI18N(Properties checkedProps, ErrorCollector errorCollector) {
        DaikonLegacyAssertions.checkAllI18N(checkedProps, errorCollector);
    }

    /**
     * check that all Components and Wizards have theirs images properly set.
     * 
     * @param componentService service to get the components to be checked.
     * 
     *            @deprecated, use {@link #assertAllComponentImagesAreSet(DefinitionRegistryService)}
     */
    @Deprecated
    public static void testAllImages(ComponentService componentService) {
        // check components
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition compDef : allComponents) {
            assertComponentImagesAreSet(compDef);
        }
        // check wizards
        Set<ComponentWizardDefinition> allWizards = componentService.getTopLevelComponentWizards();
        for (ComponentWizardDefinition wizDef : allWizards) {
            assertWizardImagesAreSet(wizDef);
        }
    }

    /**
     * check that all Components and Wizards have theirs images properly set.
     * 
     * @param componentService service to get the components to be checked.
     * 
     */
    public static void assertAllComponentImagesAreSet(DefinitionRegistryService definitionRegistry) {
        // check components
        Collection<ComponentDefinition> allComponents = definitionRegistry.getDefinitionsMapByType(ComponentDefinition.class)
                .values();
        for (ComponentDefinition compDef : allComponents) {
            assertComponentImagesAreSet(compDef);
        }
        // check wizards
        Collection<ComponentWizardDefinition> allWizards = definitionRegistry
                .getDefinitionsMapByType(ComponentWizardDefinition.class).values();
        for (ComponentWizardDefinition wizDef : allWizards) {
            assertWizardImagesAreSet(wizDef);
        }
    }

    public static void assertWizardImagesAreSet(ComponentWizardDefinition wizDef) {
        for (WizardImageType wizIT : WizardImageType.values()) {
            String pngImagePath = wizDef.getPngImagePath(wizIT);
            assertNotNull("the wizard [" + wizDef.getName() + "] must return an image path for type [" + wizIT + "]",
                    pngImagePath);
            InputStream resourceAsStream = wizDef.getClass().getResourceAsStream(pngImagePath);
            assertNotNull("Failed to find the image for path [" + pngImagePath + "] for the component:type [" + wizDef.getName()
                    + ":" + wizIT + "].\nIt should be located at [" + wizDef.getClass().getPackage().getName().replace('.', '/')
                    + "/" + pngImagePath + "]", resourceAsStream);
        }
    }

    public static void assertComponentImagesAreSet(ComponentDefinition compDef) {
        for (ComponentImageType compIT : ComponentImageType.values()) {
            String pngImagePath = compDef.getPngImagePath(compIT);
            assertNotNull("the component [" + compDef.getName() + "] must return an image path for type [" + compIT + "]",
                    pngImagePath);
            InputStream resourceAsStream = compDef.getClass().getResourceAsStream(pngImagePath);
            assertNotNull(
                    "Failed to find the image for path [" + pngImagePath + "] for the component:type [" + compDef.getName() + ":"
                            + compIT + "].\nIt should be located at ["
                            + compDef.getClass().getPackage().getName().replace('.', '/') + "/" + pngImagePath + "]",
                    resourceAsStream);
        }
    }

    /**
     * this will setup the mvn URL handler if not already setup and use any maven local repo if it exists
     * @Deprecated use {@link RuntimeUtil#registerMavenUrlHandler()}
     */
    public static void setupMavenUrlHandler() {
        RuntimeUtil.registerMavenUrlHandler();
    }

}
