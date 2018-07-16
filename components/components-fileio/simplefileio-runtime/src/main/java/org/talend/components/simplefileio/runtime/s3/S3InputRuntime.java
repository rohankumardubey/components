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
package org.talend.components.simplefileio.runtime.s3;

import java.io.InputStream;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.SimpleFileIOAvroRegistry;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatAvroIO;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatBase;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatCsvIO;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatExcelIO;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatParquetIO;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool.DetectResult;
import org.talend.components.simplefileio.runtime.auto.FileFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.input.S3InputProperties;
import org.talend.daikon.properties.ValidationResult;

public class S3InputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<S3InputProperties> {

    private static final Log LOG = LogFactory.getLog(S3InputRuntime.class);
  
    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private S3InputProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, S3InputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        // The UGI does not control security for S3.
        UgiDoAs doAs = UgiDoAs.ofNone();
        String path = S3Connection.getUriPath(properties.getDatasetProperties());
        boolean overwrite = false; // overwrite is ignored for reads.
        int limit = properties.limit.getValue();
        boolean mergeOutput = false; // mergeOutput is ignored for reads.

        SimpleRecordFormatBase rf = null;
        
        S3DatasetProperties datasetProperties = properties.getDatasetProperties();
        
        boolean isAVRO = datasetProperties.format.getValue() == SimpleFileIOFormat.AVRO;
        boolean isPARQUET = datasetProperties.format.getValue() == SimpleFileIOFormat.PARQUET;
        boolean isCSV = datasetProperties.format.getValue() == SimpleFileIOFormat.CSV;
        boolean isEXCEL = datasetProperties.format.getValue() == SimpleFileIOFormat.EXCEL;
        
        boolean isAUTO = datasetProperties.format.getValue() == SimpleFileIOFormat.AUTO_DETECT;
        DetectResult result = new DetectResult(null);
        if(isAUTO) {
            //need to consider the source is a dir case for hdfs, for s3, not sure.
            Path p = new Path(S3Connection.getUriPath(properties.getDatasetProperties()));
  
            // Add the AWS configuration to the Hadoop filesystem.
            ExtraHadoopConfiguration awsConf = new ExtraHadoopConfiguration();
            S3Connection.setS3Configuration(awsConf, properties.getDatasetProperties());
            Configuration hadoopConf = new Configuration();
            awsConf.addTo(hadoopConf);
            
            try {
                AutoDetectTool adt = new AutoDetectTool();
                FileSystem fs = p.getFileSystem(hadoopConf);
                try(InputStream is = fs.open(p)) {
                    result = adt.detect(is);
                }
                isCSV = result.getFormatType() == FileFormat.CSV;
                isEXCEL = result.getFormatType() == FileFormat.EXCEL2007 || result.getFormatType() == FileFormat.EXCEL97;
            } catch (Exception e) {
                throw new RuntimeException("some error appear when auto detect the file format : " + e.getMessage());
            }
        }
        
        if (isAVRO) {
            rf = new SimpleRecordFormatAvroIO(doAs, path, overwrite, limit, mergeOutput);
        } else if (isPARQUET) {
            rf = new SimpleRecordFormatParquetIO(doAs, path, overwrite, limit, mergeOutput);
        } else if (isCSV) {
            if(isAUTO) {
                rf = new SimpleRecordFormatCsvIO(doAs, path, limit, result.getCSV_record_separator(),
                    "" + result.getCSV_delimiter(), result.getCSV_charset().toString(), 
                    result.getCSV_header_present() ? 1l : 0l, "" + result.getCSV_text_enclosure_char(), "" + result.getCSV_escape_char());
            } else {
                S3DatasetProperties dataset = properties.getDatasetProperties();
                rf = new SimpleRecordFormatCsvIO(doAs, path, limit, dataset.getRecordDelimiter(),
                    dataset.getFieldDelimiter(), dataset.getEncoding(), 
                    dataset.getHeaderLine(), dataset.getTextEnclosureCharacter(), dataset.getEscapeCharacter());
            }
        } else if (isEXCEL) {
            if(isAUTO) {
                //TODO use result.getExcel_number_of_columns, is it necessary?
                rf = new SimpleRecordFormatExcelIO(doAs, path, overwrite, limit, mergeOutput, "UTF-8"/*only useful for excel html which is not support now for auto detect, so set a fake one here*/, 
                    result.getExcel_sheet(), result.getExcel_header_size(), 0l, result.getFormatType() == FileFormat.EXCEL2007 ? ExcelFormat.EXCEL2007 : ExcelFormat.EXCEL97);
            } else {
                S3DatasetProperties ds = properties.getDatasetProperties();
                rf = new SimpleRecordFormatExcelIO(doAs, path, overwrite, limit, mergeOutput, ds.getEncoding(), ds.getSheetName(), ds.getHeaderLine(), ds.getFooterLine(), ds.getExcelFormat());
            }
        }

        if (rf == null) {
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }

        S3Connection.setS3Configuration(rf.getExtraHadoopConfiguration(), properties.getDatasetProperties());
        return rf.read(in);
    }
}
