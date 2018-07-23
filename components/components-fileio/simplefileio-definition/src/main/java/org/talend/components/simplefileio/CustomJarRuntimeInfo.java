package org.talend.components.simplefileio;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.JarRuntimeInfo;

/**
 * a runtime info to allow to introduce a new jar by maven path directly
 * now we have four types runtime info:
 * 1. simple runtime info, only introduce self's dependency
 * 2. jar runtime info, introduce the runtime and runtime's dependency
 * 3. jdbc runtime info, extend jar runtime info, and add custom jdbc jar dependency by maven path
 * 4. custom jar runtime info, extend jar runtime info, and add customer jar dependency by maven path
 *
 * in fact, 3 and 4 can be single one, TODO
 */
public class CustomJarRuntimeInfo extends JarRuntimeInfo {

    private static final Logger LOG = LoggerFactory.getLogger(CustomJarRuntimeInfo.class);
    
    public CustomJarRuntimeInfo(URL jarUrl, String depTxtPath, String runtimeClassName, boolean reusable) {
        super(jarUrl, depTxtPath, runtimeClassName, reusable);
    }

    public CustomJarRuntimeInfo(URL jarUrl, String depTxtPath, String runtimeClassName) {
        super(jarUrl, depTxtPath, runtimeClassName);
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        List<URL> result = super.getMavenUrlDependencies();
        result.addAll(customDependencies);
        return result;
    }
    
    private List<URL> customDependencies = new ArrayList<URL>();
    
    public void addCustomDependencies(String... mavenpaths) {
        if(mavenpaths == null) {
            return;
        }
        
        for(String mavenpath : mavenpaths) {
            try {
                customDependencies.add(new URL(mavenpath));
            } catch (MalformedURLException e) {
                LOG.debug(e.getMessage());
            }
        }
    }

}
