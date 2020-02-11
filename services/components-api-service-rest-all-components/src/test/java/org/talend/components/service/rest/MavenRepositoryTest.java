package org.talend.components.service.rest;

import org.junit.Test;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenRepositoryTest {

    public static final String REPO_PATH =
            "/home/ivanhonchar/workspace/talend/components/services/components-api-service-rest-all-components/target/components-api-service-rest-all-components-0.29.0-SNAPSHOT/.m2/";

    public static final String SETTINGS_PATH =
            "/home/ivanhonchar/workspace/talend/components/services/components-api-service-rest-all-components/target/components-api-service-rest-all-components-0.29.0-SNAPSHOT/config/settings.xml";

    public static final String VERSION = "0.29.0-SNAPSHOT";

    @Test
    public void testRepository() {
        // workaround to register mvn: protocol
        JarRuntimeInfo runtimeInfo = new JarRuntimeInfo("", "", "");

        // setup pax-url JVM property
        System.setProperty("org.ops4j.pax.url.mvn.localRepository", REPO_PATH);
        System.setProperty("org.ops4j.pax.url.mvn.useFallbackRepositories", "false");
        System.setProperty("org.ops4j.pax.url.mvn.settings", SETTINGS_PATH);

        Map<String, List<URL>> missingDeps = new HashMap<>();
        for (URL jarUrl : getComponentsUrls()) {
            // get all dependencies for specific component
            List<URL> urls = DependenciesReader.extractDependencies(jarUrl);
            for (URL url : urls) {
                try(InputStream stream = url.openConnection().getInputStream()) {
                    // does nothing. This code opens input stream from .jar library in .m2 repository
                } catch (IOException e) {
                    missingDeps.computeIfAbsent(jarUrl.toString(), key -> new ArrayList<>()).add(url);
                }
            }
        }

        // print path to generated file
        System.out.println(new File("missingDeps.txt").getAbsolutePath().toString());
        // print all missing deps to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("missingDeps.txt"))) {
            // print all missing deps
            for (Map.Entry<String, List<URL>> missing : missingDeps.entrySet()) {
                writer.write(missing.getKey());
                writer.newLine();
                missing.getValue().stream().forEach(url -> {
                    try {
                        writer.write("\t" + url.toString());
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * List of all component modules which are used in tcomp-service
     */
    private String[] getComponentsList() {
        return new String[] { "mvn:org.talend.components/kinesis-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/components-jdbc-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/components-jdbc-runtime-beam/" + VERSION + "/jar",
                "mvn:org.talend.components/components-jdbc-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/elasticsearch-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/pubsub-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/localio-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/components-salesforce-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/elasticsearch-runtime_2_4/" + VERSION + "/jar",
                "mvn:org.talend.components/localio-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/simplefileio-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/s3-runtime-di/" + VERSION + "/jar",
                "mvn:org.talend.components/kinesis-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/components-jira/" + VERSION + "/jar",
                "mvn:org.talend.components/bigquery-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/kafka-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/pubsub-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/processing-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/components-marketo-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/bigquery-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/components-salesforce-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/components-marketo-runtime/" + VERSION + "/jar",
                "mvn:org.talend.components/kafka-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/simplefileio-definition/" + VERSION + "/jar",
                "mvn:org.talend.components/processing-runtime/" + VERSION + "/jar" };
    }

    private List<URL> getComponentsUrls() {
        List<URL> urls = new ArrayList<>();
        try {
            for (String path : getComponentsList()) {
                urls.add(new URL(path));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urls;
    }
}
