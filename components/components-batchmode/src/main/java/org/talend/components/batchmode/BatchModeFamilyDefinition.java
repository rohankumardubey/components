package org.talend.components.batchmode;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;

import aQute.bnd.annotation.component.Component;

@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + BatchModeFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class BatchModeFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "TCOMP"; //$NON-NLS-1$

    public BatchModeFamilyDefinition() {
        super(NAME, new BatchModeDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

}
