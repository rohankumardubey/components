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
package org.talend.components.simplefileio.runtime;

import java.io.InputStream;
import java.security.PrivilegedExceptionAction;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool.DetectResult;
import org.talend.components.simplefileio.runtime.auto.FileFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.ugi.UgiExceptionHandler;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIOInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<SimpleFileIOInputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private SimpleFileIOInputProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIOInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        // Controls the access security on the cluster.
        UgiDoAs doAs = SimpleFileIODatasetRuntime.getReadWriteUgiDoAs(properties.getDatasetProperties(),
                UgiExceptionHandler.AccessType.Read);
        String path = properties.getDatasetProperties().path.getValue();
        boolean overwrite = false; // overwrite is ignored for reads.
        int limit = properties.limit.getValue();
        boolean mergeOutput = false; // mergeOutput is ignored for reads.

        SimpleRecordFormat rf = null;
        
        SimpleFileIODatasetProperties datasetProperties = properties.getDatasetProperties();
        
        boolean isAVRO = datasetProperties.format.getValue() == SimpleFileIOFormat.AVRO;
        boolean isPARQUET = datasetProperties.format.getValue() == SimpleFileIOFormat.PARQUET;
        boolean isCSV = datasetProperties.format.getValue() == SimpleFileIOFormat.CSV;
        boolean isEXCEL = datasetProperties.format.getValue() == SimpleFileIOFormat.EXCEL;
        
        boolean isAUTO = datasetProperties.format.getValue() == SimpleFileIOFormat.AUTO_DETECT;
        DetectResult result = new DetectResult(null);
        
        if(isAUTO) {
            try {
                result = doAs.doAs(new PrivilegedExceptionAction<DetectResult>() {
      
                    @Override
                    public DetectResult run() throws Exception {
                        DetectResult result = new DetectResult(null);
                        
                        Path p = new Path(properties.getDatasetProperties().path.getValue());
                        FileSystem fs = p.getFileSystem(new Configuration());
                        AutoDetectTool adt = new AutoDetectTool();
                        try(InputStream is = fs.open(p)) {
                            result = adt.detect(is);
                        }
                        
                        return result;
                    }
                  
                });
            } catch (Exception e) {
                throw new RuntimeException("some error appear when auto detect the file format : " + e.getMessage());
            }
            
            isCSV = result.getFormatType() == FileFormat.CSV;
            isEXCEL = result.getFormatType() == FileFormat.EXCEL2007 || result.getFormatType() == FileFormat.EXCEL97;
        }
        
        if(isAVRO) {
            rf = new SimpleRecordFormatAvroIO(doAs, path, overwrite, limit, mergeOutput);
        }

        if(isCSV) {
            if(isAUTO) {
                rf = new SimpleRecordFormatCsvIO(doAs, path, limit, result.getCSV_record_separator(),
                    "" + result.getCSV_delimiter(), result.getCSV_charset().toString(), 
                    result.getCSV_header_present() ? 1l : 0l, "" + result.getCSV_text_enclosure_char(), "" + result.getCSV_escape_char());
            } else {
                SimpleFileIODatasetProperties dataset = properties.getDatasetProperties();
                rf = new SimpleRecordFormatCsvIO(doAs, path, limit, dataset.getRecordDelimiter(),
                    dataset.getFieldDelimiter(), dataset.getEncoding(), 
                    dataset.getHeaderLine(), dataset.getTextEnclosureCharacter(), dataset.getEscapeCharacter());
            }
        }
        
        if(isPARQUET) {
            rf = new SimpleRecordFormatParquetIO(doAs, path, overwrite, limit, mergeOutput);
        }
        
        if(isEXCEL) {
            if(isAUTO) {
                rf = new SimpleRecordFormatExcelIO(doAs, path, overwrite, limit, mergeOutput, "UTF-8", result.getExcel_sheet(), result.getExcel_header_size(), 0l, result.getFormatType() == FileFormat.EXCEL2007 ? ExcelFormat.EXCEL2007 : ExcelFormat.EXCEL97);
            } else {
                SimpleFileIODatasetProperties ds = properties.getDatasetProperties();
                rf = new SimpleRecordFormatExcelIO(doAs, path, overwrite, limit, mergeOutput, ds.getEncoding(), ds.getSheetName(), ds.getHeaderLine(), ds.getFooterLine(), ds.getExcelFormat());
            }
        }

        if (rf == null) {
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }

        return rf.read(in);
    }
    
}
