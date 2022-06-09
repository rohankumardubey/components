//============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.config.jdbc.MappingFileLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class DBMappingUtils {

    private static Logger LOG = LoggerFactory.getLogger(DBMappingUtils.class);

    public static Dbms getMapping(final RuntimeContainer container, final String mappingFileSuffix) {
        Dbms dbms = null;
        if(container!=null) {
            try {
                URL mappingFileDir = (URL) container.getComponentData(container.getCurrentComponentId(),
                        ComponentConstants.MAPPING_URL_SUFFIX);
                dbms = DBMappingUtils.getMapping(mappingFileDir, mappingFileSuffix);
            } catch(Exception e) {
                //ignore any exception
                LOG.warn("fail to load db mapping info : " + e.getMessage());
            }
        }
        return dbms;
    }

    public static Dbms getMapping(final String mappingFilesDir, final String mappingFileSuffix) {
        try {
            return getMapping(new URL(mappingFilesDir), mappingFileSuffix);
        } catch (MalformedURLException e) {
            throw new RuntimeException("can't find the mapping file dir : " + mappingFilesDir);
        }
    }

    public static Dbms getMapping(final URL mappingFilesDir, final String mappingFileSuffix) {
        final MappingFileLoader fileLoader = new MappingFileLoader();
        Dbms dbms = null;

        try {
            dbms = loadFromStream(fileLoader, mappingFilesDir, mappingFileSuffix);
        } catch(Exception e) {
            //Fallback to old solution
            LOG.warn("Couldn't load mapping from stream. Trying to read as File.", e);
            dbms = loadFromFile(fileLoader, mappingFilesDir, mappingFileSuffix);
        }

        return dbms;
    }

    private static Dbms loadFromFile(final MappingFileLoader fileLoader, final URL mappingFilesDir, final String mappingFileSuffix) {
        File mappingFileFullPath = new File(mappingFilesDir.getFile(), "mapping_" + mappingFileSuffix + ".xml");
        if (!mappingFileFullPath.exists()) {
            mappingFileFullPath =
                    new File(mappingFilesDir.getFile(), "mapping_" + mappingFileSuffix.toLowerCase() + ".xml");
        }
        return fileLoader.load(mappingFileFullPath).get(0);
    }

    private static Dbms loadFromStream(final MappingFileLoader fileLoader, final URL mappingFilesDir, final String mappingFileSuffix)
            throws IOException, URISyntaxException {
        Dbms dbms = null;
        InputStream mappingStream = null;
        try {
            mappingStream = getStream(mappingFilesDir, mappingFileSuffix);
            dbms = fileLoader.load(mappingStream).get(0);
        } finally {
            if (mappingStream != null) {
                try {
                    mappingStream.close();
                } catch (IOException e) {
                    LOG.warn("Couldn't close stream.", e);
                }
            }
        }
        return dbms;
    }

    private static InputStream getStream(final URL mappingFileDir, final String mappingFileSuffix)
            throws IOException, URISyntaxException {
        if(mappingFileDir == null) {
            throw new IllegalArgumentException("Mapping file directory URL cannot be null!");
        }
        URL mappingFileDirUrl = mappingFileDir;
        InputStream mappingStream = null;
        if(!mappingFileDirUrl.toString().endsWith("/")) {
            mappingFileDirUrl = new URL(mappingFileDirUrl.toString() + "/");
        }
        try {
            URL mappingFileFullUrl = mappingFileDirUrl.toURI().resolve("mapping_" + mappingFileSuffix + ".xml").toURL();
            mappingStream = mappingFileFullUrl.openStream();
        } catch (URISyntaxException | IOException e) {
            URL mappingFileFullUrl = mappingFileDirUrl.toURI().resolve("mapping_" + mappingFileSuffix.toLowerCase() + ".xml").toURL();
            mappingStream = mappingFileFullUrl.openStream();
        }
        return mappingStream;
    }

}
