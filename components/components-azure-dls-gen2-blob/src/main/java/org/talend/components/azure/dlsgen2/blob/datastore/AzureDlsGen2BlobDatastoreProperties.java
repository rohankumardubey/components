// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azure.dlsgen2.blob.datastore;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.azure.dlsgen2.blob.runtime.AzureDlsGen2BlobSource;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.service.Repository;

public class AzureDlsGen2BlobDatastoreProperties extends ComponentPropertiesImpl implements DatastoreProperties, AzureDlsGen2ProvideConnectionProperties {

    private static final long serialVersionUID = 5588521568261191377L;

    // Only for the wizard use
    public Property<String> name = newString("name").setRequired();

    public static final String FORM_WIZARD = "Wizard";

    private String repositoryLocation;

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureDlsGen2BlobDatastoreProperties.class);
    //

    public enum AuthType {
        SHAREDKEY,
        SAS,
        ACTIVE_DIRECTORY_CLIENT_CREDENTIAL
    }

    /**
     * accountKey - The Azure DLS Gen2 Account Key.
     */
    public Property<String> accountKey = newString("accountKey").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<AuthType> authenticationType = newEnum("authenticationType", AuthType.class).setValue(AuthType.SHAREDKEY);

    /**
     * accountName - The Azure DLS Gen2 Account Name.
     */
    public Property<String> accountName = PropertyFactory.newString("accountName"); //$NON-NLS-1$

    public Property<String> tenantId = newString("tenantId").setRequired();

    public Property<String> clientId = newString("clientId").setRequired();

    public Property<String> clientSecret = newString("clientSecret")
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING)).setRequired();

    public Property<String> sharedAccessSignature = PropertyFactory.newString("sharedAccessSignature");//$NON-NLS-1$

    public ComponentReferenceProperties<AzureDlsGen2BlobDatastoreProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", AzureDlsGen2BlobDatastoreDefinition.NAME);

    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");


    public AzureDlsGen2BlobDatastoreProperties(String name) {
        super(name);
    }


    @Override
    public void setupProperties() {
        super.setupProperties();
        authenticationType.setValue(AuthType.SHAREDKEY);
        accountName.setValue("");
        accountKey.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(accountName);
        mainForm.addRow(authenticationType);
        mainForm.addRow(widget(accountKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(sharedAccessSignature);
        mainForm.addRow(tenantId);
        mainForm.addRow(clientId);
        mainForm.addColumn(widget(clientSecret).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));

        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(accountName);
        wizardForm.addRow(authenticationType);
        wizardForm.addRow(widget(accountKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addRow(sharedAccessSignature);
        wizardForm.addRow(tenantId);
        wizardForm.addRow(clientId);
        wizardForm.addColumn(widget(clientSecret).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = getReferencedComponentId();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(AzureDlsGen2BlobDatastoreDefinition.NAME);
        boolean isSharedKeyAuthUsed = authenticationType.getValue() == AuthType.SHAREDKEY;
        boolean isSASAuthUsed = authenticationType.getValue() == AuthType.SAS;
        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            form.getWidget(accountName).setHidden(useOtherConnection);
            form.getWidget(authenticationType).setHidden(useOtherConnection);
            form.getWidget(tenantId).setHidden(true);
            form.getWidget(clientId).setHidden(true);
            form.getWidget(clientSecret).setHidden(true);
            form.getWidget(accountKey).setHidden(true);
            form.getWidget(sharedAccessSignature.getName()).setHidden(true);
            if (form.getWidget(authenticationType).isVisible()){
                switch (authenticationType.getValue()){
                case SHAREDKEY:
                    form.getWidget(accountKey).setVisible(true);
                    break;
                case SAS:
                    form.getWidget(sharedAccessSignature.getName()).setVisible(true);
                    break;
                case ACTIVE_DIRECTORY_CLIENT_CREDENTIAL:
                    form.getWidget(tenantId).setVisible(true);
                    form.getWidget(clientId).setVisible(true);
                    form.getWidget(clientSecret).setVisible(true);
                    break;
                }
            }
        }
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public void afterUseSharedAccessSignature() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
        refreshLayout(getForm(FORM_WIZARD));
    }

    public void afterAuthenticationType() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
        refreshLayout(getForm(FORM_WIZARD));
    }

    public ValidationResult validateTestConnection() throws Exception {
        ValidationResult vr = AzureDlsGen2BlobSource.validateConnection(this);
        if (ValidationResult.Result.OK != vr.getStatus()) {
            getForm(FORM_WIZARD).setAllowForward(false);
            return vr;
        }
        getForm(FORM_WIZARD).setAllowForward(true);
        getForm(FORM_WIZARD).setAllowFinish(true);
        return new ValidationResult(Result.OK, i18nMessages.getMessage("message.success"));
    }

    public ValidationResult afterFormFinishWizard(Repository<Properties> repo) throws Exception {
        ValidationResult vr = AzureDlsGen2BlobSource.validateConnection(this);
        if (vr.getStatus() != ValidationResult.Result.OK) {
            return vr;
        }

        return ValidationResult.OK;
    }

    @Override
    public AzureDlsGen2BlobDatastoreProperties getConnectionProperties() {
        return this;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }

    public AzureDlsGen2BlobDatastoreProperties getReferencedConnectionProperties() {
        AzureDlsGen2BlobDatastoreProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps;
        }
        return null;
    }

    public AzureDlsGen2BlobDatastoreProperties setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        return this;
    }

}
