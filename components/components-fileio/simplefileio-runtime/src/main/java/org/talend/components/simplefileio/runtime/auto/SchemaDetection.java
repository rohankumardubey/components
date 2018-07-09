package org.talend.components.simplefileio.runtime.auto;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.net.MediaType;

/**
 * the bridge for the api in daikon-ee which we can't depend on it on compiler time, in fact, in my opinion, should move format-detection-core to daikon(se) from daikon-ee as
 * it's only the interface 
 *
 */
public class SchemaDetection {

    private Object schemaDetectionImpl;
    
    public SchemaDetection() {
        try {
            schemaDetectionImpl = Class.forName("org.talend.daikon.schema.SchemaDetection");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found : " + e.getMessage());
        }
    }

    public MediaType detect(byte[] inputFirstBytes) throws IOException {
        try {
            return (MediaType) schemaDetectionImpl.getClass().getMethod("detect", byte[].class).invoke(schemaDetectionImpl, inputFirstBytes);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    public MediaType detect(InputStream input) throws IOException {
        try {
            return (MediaType) schemaDetectionImpl.getClass().getMethod("detect", InputStream.class).invoke(schemaDetectionImpl, input);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    //register the csv and excel implement
    public void registerDetectors() {
        try {
            Object cd = Class.forName("org.talend.daikon.schema.csv.CsvDetector");
            Object ed = Class.forName("org.talend.daikon.schema.xls.ExcelDetector");
            schemaDetectionImpl.getClass().getMethod("registerDetector", cd.getClass()).invoke(schemaDetectionImpl, cd);
            schemaDetectionImpl.getClass().getMethod("registerDetector", ed.getClass()).invoke(schemaDetectionImpl, ed);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //register the csv and excel implement
    public void registerParsers() {
        try {
            Object cpf = Class.forName("org.talend.daikon.schema.csv.parser.CsvParserFactory");
            Object epf = Class.forName("org.talend.daikon.schema.xls.parser.ExcelParserFactory");
            schemaDetectionImpl.getClass().getMethod("registerParser", cpf.getClass()).invoke(schemaDetectionImpl, cpf);
            schemaDetectionImpl.getClass().getMethod("registerParser", epf.getClass()).invoke(schemaDetectionImpl, epf);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> parse(InputStream content, MediaType type) throws IOException {
        List<String> result = new ArrayList<>();
        try {
            Object parser = schemaDetectionImpl.getClass().getMethod("parse", InputStream.class, MediaType.class).invoke(schemaDetectionImpl, content, type);
            if(parser == null) {
                return result;
            }
            
            Object schema = parser.getClass().getMethod("getSchema").invoke(parser);
            if(schema == null) {
                return result;
            }
            
            List<Object> fields = (List<Object>) parser.getClass().getMethod("getColumnsMetadata").invoke(schema);
            if(fields == null) {
                return result;
            }
            
            for(Object field : fields) {
                String fieldName = (String)field.getClass().getMethod("getName").invoke(field);
                result.add(fieldName);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
}
